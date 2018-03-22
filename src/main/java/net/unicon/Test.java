package net.unicon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;

@SpringBootApplication
public class Test implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(Test.class, args);
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LdapOptions ldap;

    @Autowired
    public Test(LdapOptions ldap){
        this.ldap=ldap;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            connect();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private SimpleEntry<String, DirContext> getContext() {

        String[] urls = ldap.getUrls();
        for (int i = 0; i < urls.length; i++) {
            String ldapUrl = urls[i];
            if(ldapUrl==null || ldapUrl.trim().isEmpty()) {
                continue;
            }
            ldapUrl = ldapUrl.trim();
            logger.info("\nAttempting connect to LDAP instance {}: [{}].\n", (i + 1), ldapUrl);
            final Hashtable<String, String> env = buildEnv(ldapUrl, ldap.getUserId(), ldap.getPassword());
            printConfig(env);
            try {
                return new SimpleEntry<>(ldapUrl, new InitialDirContext(env));
            } catch (Exception e) {
                logger.info("\nFailed to connect to ldap instance #{}: [{}].\n", (i + 1), ldapUrl);
            }
        }
        return null;
    }

    private void connect() throws NamingException {
        final SearchControls ctls = new SearchControls();
        ctls.setDerefLinkFlag(true);
        ctls.setTimeLimit(ldap.getTimeout());
        ctls.setReturningAttributes(ldap.getAttributes());
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        final SimpleEntry<String, DirContext> pair = getContext();
        if (pair == null) {
            throw new IllegalArgumentException("\nCould not connect to any of the provided LDAP urls based on the given credentials.");
        }

        DirContext ctx = null;

        try {
            ctx = pair.getValue();

            String log = "Successfully connected to the LDAP url [{}] ";
            if (ctx.getNameInNamespace() != null && !ctx.getNameInNamespace().isEmpty()) {
                log += "with namespace [{}].";
            }
            log += "\n";
            logger.info(log, pair.getKey(), ctx.getNameInNamespace());

            logger.info("******* Ldap Search *******");
            logger.info("Ldap filter: {}", ldap.getFilter());
            logger.info("Ldap search base: {}", ldap.getBaseDn());
            logger.info("Returning attributes: {}", Arrays.toString(ldap.getAttributes()));
            logger.info("***************************\n");

            final NamingEnumeration<SearchResult> answer = ctx.search(ldap.getBaseDn(), ldap.getFilter(), ctls);
            if (answer.hasMoreElements()) {
                logger.info("******* Ldap Search Results *******");
                while (answer.hasMoreElements()) {
                    final SearchResult result = answer.nextElement();
                    logger.info("User name: {}", result.getName());
                    logger.info("User full name: {}", result.getNameInNamespace());

                    String authnPsw = ldap.getAuthnPassword();
                    if (authnPsw != null) {
                        logger.info("Attempting to authenticate {} with password {}",result.getName(), authnPsw);

                        final Hashtable<String, String> env = buildEnv(pair.getKey(), result.getNameInNamespace(), authnPsw);
                        new InitialDirContext(env);
                        logger.info("Successfully authenticated {} with password {} at {}", result.getName(), authnPsw, pair.getKey());
                    }
                    final NamingEnumeration<String> attrs = result.getAttributes().getIDs();

                    while (attrs.hasMoreElements()) {
                        final String id = attrs.nextElement();
                        logger.info("{} => {} ", id, result.getAttributes().get(id));
                    }
                }
                logger.info("************************************\n");
            } else {
                logger.info("No search results could be found. \n");
            }

            logger.info("Ldap search completed successfully. \n");
        } finally {
            if (ctx != null)
                ctx.close();

        }
    }

    private Hashtable<String,String> buildEnv(String url, String principal, String credentials) {
        final Hashtable<String, String> env = new Hashtable<>(6);
        env.put(Context.INITIAL_CONTEXT_FACTORY, ldap.getFactory().getName());
        env.put(Context.PROVIDER_URL, url);
        env.put(Context.SECURITY_AUTHENTICATION, ldap.getAuthentication());
        env.put(Context.SECURITY_PRINCIPAL, principal);
        env.put(Context.SECURITY_CREDENTIALS, credentials);
        env.put("com.sun.jndi.ldap.connect.timeout", ldap.getTimeout().toString());
        return env;
    }

    private void printConfig(final Hashtable<String, String> table) {
        logger.info("******* LDAP Instance Configuration *******");
        final Enumeration<String> names = table.keys();
        while (names.hasMoreElements()) {
            final String str = names.nextElement();
            logger.info("{}: {}", str, table.get(str));
        }
        logger.info("********************************************\n");
    }

}
