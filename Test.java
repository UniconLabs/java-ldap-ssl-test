import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

public class Test extends Formatter {
  private static final Logger theLogger = Logger.getLogger(Test.class.getName());

  public static void main(final String[] args) throws IOException {

    final Properties props = new Properties();
    props.load(Test.class.getResourceAsStream("Test.properties"));

    try {
      theLogger.setUseParentHandlers(false);
      final ConsoleHandler handler = new ConsoleHandler();
      handler.setFormatter(new Test());
      theLogger.addHandler(handler);

      connect(props);
    } catch(final IllegalArgumentException e) {
      theLogger.severe(e.getMessage());
    } catch (final Exception e) {
      theLogger.severe(e.getMessage());
      e.printStackTrace();
    }
  }

  private static Pair<String, DirContext> getContext(final Properties props) {
    for (int i = 0; i<5; i++) {
        String ldapUrl = props.getProperty("ldap.url" + (i + 1));

        if (ldapUrl != null && !ldapUrl.isEmpty()) {
            ldapUrl = ldapUrl.trim();

            final Hashtable<String, String> env = new Hashtable<String, String>(6);
            env.put(Context.INITIAL_CONTEXT_FACTORY, props.getProperty("ldap.factory"));
            env.put(Context.PROVIDER_URL, ldapUrl.trim());
            env.put(Context.SECURITY_AUTHENTICATION, props.getProperty("ldap.authentication"));
            env.put(Context.SECURITY_PRINCIPAL, props.getProperty("ldap.userId"));
            env.put(Context.SECURITY_CREDENTIALS, props.getProperty("ldap.password"));
            env.put("com.sun.jndi.ldap.connect.timeout", props.getProperty("ldap.timeout"));

            printConfig(env);
            try {
                return new Pair<String, DirContext>(ldapUrl, new InitialDirContext(env));
            } catch (Exception e) {
                theLogger.info("Failed to connect to ldap instance [" + ldapUrl.trim() + "]. Trying next...\n");
            }
        }
    }
    return null;
  }

  private static void connect(final Properties props) throws Exception {
    final String[] attrIDs = props.getProperty("ldap.attributes").split(",");

    final SearchControls ctls = new SearchControls();
    ctls.setDerefLinkFlag(true);
    ctls.setTimeLimit(new Integer(props.getProperty("ldap.timeout")));
    ctls.setReturningAttributes(attrIDs);
    ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

    final Pair<String, DirContext> pair = getContext(props);
    if (pair == null) {
        throw new IllegalArgumentException("Could not connect to any of the provided ldap urls based on the given credentials.");
    }

    DirContext ctx = null;

    try {
      ctx = pair.getSecond();

      String log = "Successfully connected to the ldap url [" + pair.getFirst().trim() + "] ";
      if (ctx.getNameInNamespace() != null && !ctx.getNameInNamespace().isEmpty()) {
        log += "with namespace [" + ctx.getNameInNamespace() + "].";
      }
      log += "\n";
      theLogger.info(log);

      theLogger.info("******* Ldap Search *******");
      theLogger.info("Ldap filter: " + props.getProperty("ldap.filter"));
      theLogger.info("Ldap search base: " + props.getProperty("ldap.baseDn"));
      theLogger.info("Returning attributes: " + Arrays.toString(attrIDs));
      theLogger.info("***************************\n");

      final NamingEnumeration<SearchResult> answer = ctx.search(props.getProperty("ldap.baseDn"), props.getProperty("ldap.filter"), ctls);
      if (answer.hasMoreElements()) {
        theLogger.info("******* Ldap Search Results *******");
        while (answer.hasMoreElements()) {
          final SearchResult result = answer.nextElement();
          theLogger.info("User name: " + result.getName());
          theLogger.info("User full name: " + result.getNameInNamespace());

          String authnPsw = props.getProperty("ldap.authn.password");
          if (authnPsw != null) {
            theLogger.info("Attempting to authenticate " + result.getName() + " with password " + authnPsw);

            final Hashtable<String, String> env = new Hashtable<String, String>(6);
            env.put(Context.INITIAL_CONTEXT_FACTORY, props.getProperty("ldap.factory"));
            env.put(Context.PROVIDER_URL, pair.getFirst().trim());
            env.put(Context.SECURITY_AUTHENTICATION, props.getProperty("ldap.authentication"));
            env.put(Context.SECURITY_PRINCIPAL, result.getNameInNamespace());
            env.put(Context.SECURITY_CREDENTIALS, authnPsw);
            env.put("com.sun.jndi.ldap.connect.timeout", props.getProperty("ldap.timeout"));
            DirContext userCtx = new InitialDirContext(env);
            theLogger.info("Successfully authenticated " + result.getName() + " with password " + authnPsw + " at " + pair.getFirst());
          }
          final NamingEnumeration<String> attrs = result.getAttributes().getIDs();

          while (attrs.hasMoreElements()) {
            final String id = attrs.nextElement();
            theLogger.info(id + " => " + result.getAttributes().get(id));
          }
        }
        theLogger.info("************************************\n");
      } else {
        theLogger.info("No search results could be found. \n");
      }

      theLogger.info("Ldap search completed successfully. \n");
    } finally {
      if (ctx != null)
        ctx.close();

    }
  }

  private static void printConfig(final Hashtable<String, String> table) {
    theLogger.info("******* Ldap Configuration *******");

    final Enumeration<String> names = table.keys();
    while (names.hasMoreElements()) {
      final String str = names.nextElement();
      theLogger.info(str + ": " + table.get(str));
    }
    theLogger.info("**********************************\n");
  }

  @Override
  public String format(final LogRecord record) {
    final StringBuffer sb = new StringBuffer();

    sb.append("[");
    sb.append(record.getLevel().getName());
    sb.append("]\t");

    sb.append(formatMessage(record));
    sb.append("\n");

    return sb.toString();
  }

  private static class Pair<F,S> {
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
