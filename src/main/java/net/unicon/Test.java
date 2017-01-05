package net.unicon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

public class Test implements CommandLineRunner {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Test.class, args);
    }

    public void run(final String... args) throws Exception {
        final Properties props = new Properties();
        props.load(Test.class.getResourceAsStream("/Test.properties"));

        try {
            connect(props);
        } catch (final IllegalArgumentException e) {
            logger.error(e.getMessage());
        } catch (final Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private Pair<String, DirContext> getContext(final Properties props) {
        for (int i = 0; i < 5; i++) {
            String ldapUrl = props.getProperty("ldap.url" + (i + 1));

            if (ldapUrl != null && !ldapUrl.isEmpty()) {
                logger.info("\nAttempting connect to LDAP instance #" + (i + 1) + ": [" + ldapUrl.trim() + "].\n");
                ldapUrl = ldapUrl.trim();
                final Hashtable<String, String> env = new Hashtable<>(6);
                env.put(Context.INITIAL_CONTEXT_FACTORY, props.getProperty("ldap.factory"));
                env.put(Context.PROVIDER_URL, ldapUrl.trim());
                env.put(Context.SECURITY_AUTHENTICATION, props.getProperty("ldap.authentication"));
                env.put(Context.SECURITY_PRINCIPAL, props.getProperty("ldap.userId"));
                env.put(Context.SECURITY_CREDENTIALS, props.getProperty("ldap.password"));
                env.put("com.sun.jndi.ldap.connect.timeout", props.getProperty("ldap.timeout"));

                printConfig(env);
                try {
                    return new Pair<>(ldapUrl, new InitialDirContext(env));
                } catch (Exception e) {
                    logger.info("\nFailed to connect to ldap instance #" + (i + 1) + ": [" + ldapUrl.trim() + "].\n");
                }
            }
        }
        return null;
    }

    private void connect(final Properties props) throws Exception {
        final String[] attrIDs = props.getProperty("ldap.attributes").split(",");

        final SearchControls ctls = new SearchControls();
        ctls.setDerefLinkFlag(true);
        ctls.setTimeLimit(new Integer(props.getProperty("ldap.timeout")));
        ctls.setReturningAttributes(attrIDs);
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        final Pair<String, DirContext> pair = getContext(props);
        if (pair == null) {
            throw new IllegalArgumentException("\nCould not connect to any of the provided LDAP urls based on the given credentials.");
        }

        DirContext ctx = null;

        try {
            ctx = pair.getSecond();

            String log = "Successfully connected to the LDAP url [" + pair.getFirst().trim() + "] ";
            if (ctx.getNameInNamespace() != null && !ctx.getNameInNamespace().isEmpty()) {
                log += "with namespace [" + ctx.getNameInNamespace() + "].";
            }
            log += "\n";
            logger.info(log);

            logger.info("******* Ldap Search *******");
            logger.info("Ldap filter: " + props.getProperty("ldap.filter"));
            logger.info("Ldap search base: " + props.getProperty("ldap.baseDn"));
            logger.info("Returning attributes: " + Arrays.toString(attrIDs));
            logger.info("***************************\n");

            final NamingEnumeration<SearchResult> answer = ctx.search(props.getProperty("ldap.baseDn"), props.getProperty("ldap.filter"), ctls);
            if (answer.hasMoreElements()) {
                logger.info("******* Ldap Search Results *******");
                while (answer.hasMoreElements()) {
                    final SearchResult result = answer.nextElement();
                    logger.info("User name: " + result.getName());
                    logger.info("User full name: " + result.getNameInNamespace());

                    String authnPsw = props.getProperty("ldap.authn.password");
                    if (authnPsw != null) {
                        logger.info("Attempting to authenticate " + result.getName() + " with password " + authnPsw);

                        final Hashtable<String, String> env = new Hashtable<>(6);
                        env.put(Context.INITIAL_CONTEXT_FACTORY, props.getProperty("ldap.factory"));
                        env.put(Context.PROVIDER_URL, pair.getFirst().trim());
                        env.put(Context.SECURITY_AUTHENTICATION, props.getProperty("ldap.authentication"));
                        env.put(Context.SECURITY_PRINCIPAL, result.getNameInNamespace());
                        env.put(Context.SECURITY_CREDENTIALS, authnPsw);
                        env.put("com.sun.jndi.ldap.connect.timeout", props.getProperty("ldap.timeout"));
                        new InitialDirContext(env);
                        logger.info("Successfully authenticated " + result.getName() + " with password " + authnPsw + " at " + pair.getFirst());
                    }
                    final NamingEnumeration<String> attrs = result.getAttributes().getIDs();

                    while (attrs.hasMoreElements()) {
                        final String id = attrs.nextElement();
                        logger.info(id + " => " + result.getAttributes().get(id));
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

    private void printConfig(final Hashtable<String, String> table) {
        logger.info("******* LDAP Instance Configuration *******");
        final Enumeration<String> names = table.keys();
        while (names.hasMoreElements()) {
            final String str = names.nextElement();
            logger.info(str + ": " + table.get(str));
        }
        logger.info("********************************************\n");
    }

    private static class Pair<F, S> {
        private F first;
        private S second;

        public Pair(F f, S s) {
            this.first = f;
            this.second = s;
        }

        public F getFirst() {
            return this.first;
        }

        public S getSecond() {
            return this.second;
        }
    }
}
