package com.example.akkaj.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.security.UserGroupInformation;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * Created by yilong on 2018/1/9.
 */
public class KbrUtil {
    private static final Log LOG = LogFactory.getLog(KbrUtil.class);

    private static LoginContext context = null;

    private static LoginContext kerbersInit(String loginConfPath, String kr5Path, String loginKey) {
        try {
            System.setProperty("java.security.auth.login.config", loginConfPath);
            System.setProperty("java.security.krb5.conf", kr5Path);
            System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");

            LoginContext loginCOntext = new LoginContext(loginKey);
            loginCOntext.login();
            return loginCOntext;
        } catch (LoginException e) {
            e.printStackTrace();
            LOG.error("kerbers kinit error:", e);
            return null;
        }
    }

    public static void kinit(String kerberosJassPath, String kerberosKrb5Path, String kerberosJassKey){
        try {
            context = kerbersInit(kerberosJassPath, kerberosKrb5Path, kerberosJassKey);
            context.login();
        }catch (Exception e){
            e.printStackTrace();
            LOG.error("kinit error"+e.getMessage(), e);
        }
    }

    public static LoginContext getContext() {
        return context;
    }

    public static void kerberosInit(final String keytabFile, final String principal,
                                    final Configuration conf, String realm, String kdc, String keytypeFileKey,
                                    String usernameKey)
            throws Exception {
        if (System.getProperty("os.name").contains("Windows") || System.getProperty("os.name").contains("Mac")) {
            System.setProperty("java.security.krb5.realm", realm);
            System.setProperty("java.security.krb5.kdc", kdc);
        }
        conf.set("hadoop.security.authentication", "kerberos");
        UserGroupInformation.setConfiguration(conf);

        conf.set(keytypeFileKey, keytabFile);
        conf.set(usernameKey, principal);
        SecurityUtil.login(conf, keytypeFileKey, usernameKey);
    }
}
