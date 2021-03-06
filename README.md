Java LDAP Test
====================

This is a small test utility that attempts to connect to an LDAP instance,
authenticate a given credential and retrieve attributes. It is very helpful
for testing secure connections, LDAPS and certificate configuration.

## Configuration

See [`Test.properties`](https://github.com/mmoayyed/java-ldap-ssl-test/blob/master/src/main/resources/Test.properties).

## Build

```bash
mvn clean package 
```

## Usage

- Download the JAR from [here](https://github.com/UniconLabs/java-ldap-ssl-test/releases)
- Run:

```
java -jar <jar-file-name>
```

##Sample output

The log below demonstrates a sample of the program output configured to hit 5 ldap urls. 

```
[INFO]  ******* Ldap Configuration *******
[INFO]	com.sun.jndi.ldap.connect.timeout: 3000
[INFO]	java.naming.factory.initial: com.sun.jndi.ldap.LdapCtxFactory
[INFO]	java.naming.security.principal: casadmin@somewhere.local
[INFO]	java.naming.provider.url: ldap://10.1.54.56
[INFO]	java.naming.security.authentication: simple
[INFO]	java.naming.security.credentials: helloWorld!
[INFO]	**********************************

[INFO]	Failed to connect to ldap instance [ldap://10.1.54.56]. Trying next...

[INFO]	******* Ldap Configuration *******
[INFO]	com.sun.jndi.ldap.connect.timeout: 3000
[INFO]	java.naming.factory.initial: com.sun.jndi.ldap.LdapCtxFactory
[INFO]	java.naming.security.principal: casadmin@somewhere.local
[INFO]	java.naming.provider.url: ldap://10.1.1.3
[INFO]	java.naming.security.authentication: simple
[INFO]	java.naming.security.credentials: helloWorld!
[INFO]	**********************************

[INFO]	Failed to connext to ldap instance [ldap://10.1.1.3]. Trying next...

[INFO]	******* Ldap Configuration *******
[INFO]	com.sun.jndi.ldap.connect.timeout: 3000
[INFO]	java.naming.factory.initial: com.sun.jndi.ldap.LdapCtxFactory
[INFO]	java.naming.security.principal: casadmin@somewhere.local
[INFO]	java.naming.provider.url: ldap://10.1.1.12
[INFO]	java.naming.security.authentication: simple
[INFO]	java.naming.security.credentials: helloWorld!
[INFO]	**********************************

[INFO]	Failed to connext to ldap instance [ldap://10.1.1.12]. Trying next...

[INFO]	******* Ldap Configuration *******
[INFO]	com.sun.jndi.ldap.connect.timeout: 3000
[INFO]	java.naming.factory.initial: com.sun.jndi.ldap.LdapCtxFactory
[INFO]	java.naming.security.principal: casadmin@somewhere.local
[INFO]	java.naming.provider.url: ldap://10.1.1.1
[INFO]	java.naming.security.authentication: simple
[INFO]	java.naming.security.credentials: helloWorld!
[INFO]	**********************************

[INFO]	Failed to connext to ldap instance [ldap://10.1.1.1]. Trying next...

[INFO]	******* Ldap Configuration *******
[INFO]	com.sun.jndi.ldap.connect.timeout: 3000
[INFO]	java.naming.factory.initial: com.sun.jndi.ldap.LdapCtxFactory
[INFO]	java.naming.security.principal: casadmin@somewhere.local
[INFO]	java.naming.provider.url: ldap://10.1.12.33
[INFO]	java.naming.security.authentication: simple
[INFO]	java.naming.security.credentials: helloWorld!
[INFO]	**********************************

[INFO]	Successfully connected to the ldap url [ldap://10.1.12.33] 

[INFO]	******* Ldap Search *******
[INFO]	Ldap filter: (&(objectClass=*) (sAMAccountName=casadmin))
[INFO]	Ldap search base: DC=somewhere,DC=local
[INFO]	Returning attributes: [cn, givenName]
[INFO]	***************************

[INFO]	******* Ldap Search Results *******
[INFO]	User name: CN=CAS Admin,OU=Other,OU=Users2
[INFO]	User full name: CN=CAS Admin,OU=Other,OU=Users2,DC=somewhere,DC=local
[INFO]	givenName => givenName: CAS
[INFO]	cn => cn: CAS Admin
[INFO]	************************************

[INFO]	Ldap search completed successfully. 
```
