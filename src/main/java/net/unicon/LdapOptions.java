package net.unicon;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.naming.spi.InitialContextFactory;

// Component ensures Spring will manage a bean of this class
// and ConfigurationProperties is an easy way to map all the properties under "ldap.*" to type strict values
@Component
@ConfigurationProperties("ldap")
public class LdapOptions {

    private String[] urls;
    private String userId;
    private String password;
    private String baseDn;
    private String filter;
    private String authnPassword;
    private String[] attributes;
    private Class<? extends InitialContextFactory> factory;
    private String authentication;
    private Integer timeout;

    public String[] getUrls() {
        return urls;
    }

    public void setUrls(String[] urls) {
        this.urls = urls;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBaseDn() {
        return baseDn;
    }

    public void setBaseDn(String baseDn) {
        this.baseDn = baseDn;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getAuthnPassword() {
        return authnPassword;
    }

    public void setAuthnPassword(String authnPassword) {
        this.authnPassword = authnPassword;
    }

    public String[] getAttributes() {
        return attributes;
    }

    public void setAttributes(String[] attributes) {
        this.attributes = attributes;
    }

    public Class<? extends InitialContextFactory> getFactory() {
        return factory;
    }

    public void setFactory(Class<? extends InitialContextFactory> factory) {
        this.factory = factory;
    }

    public String getAuthentication() {
        return authentication;
    }

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
}
