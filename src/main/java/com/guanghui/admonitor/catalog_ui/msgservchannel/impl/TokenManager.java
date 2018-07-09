package com.guanghui.admonitor.catalog_ui.msgservchannel.impl;


import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by cuiju on 2017/1/11. 15 58
 */
public class TokenManager implements Runnable {

    String getNameFromToken(byte[] token){
        String name = null;
        try {
            String tokenStr = new String(token, "UTF-8");
            synchronized (userInfos){
                for (UserInfo userInfo : userInfos) {
                    if (userInfo.token.equals(tokenStr)) {
                        name = userInfo.userName;
                        userInfo.lastAccesTime = System.currentTimeMillis();
                        break;
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return name;
    }

    String newAuthenOk(String user, String passwd) {
        synchronized (userInfos){
            Iterator<UserInfo> it = userInfos.iterator();
            while (it.hasNext()){
                UserInfo userInfo = it.next();
                if (userInfo.userName.equals(user)){
                    it.remove();
                    break;
                }
            }
            UserInfo userInfo = new UserInfo();
            userInfo.userName = user;
            userInfo.lastAccesTime = System.currentTimeMillis();
            userInfo.token = getNewToken(user, passwd);
            userInfos.add(userInfo);
            return userInfo.token;
        }
    }



    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                logger.error(ExceptionUtils.getStackTrace(e));
            }
            long currTime = System.currentTimeMillis();
            synchronized (userInfos) {
                userInfos.removeIf(userInfo -> currTime - userInfo.lastAccesTime > TOKEN_TIMEOUT_MS);
            }
        }
    }

    private class UserInfo {
        String token;
        String userName;
        long lastAccesTime;
    }

    private String getNewToken(String user, String passwd) {
        String str = user + passwd + RandomGenerator.getRandom();
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return Base64.getEncoder().encodeToString(digest.digest(str.getBytes()));
    }

    private final long TOKEN_TIMEOUT_MS = 10 * 60 *1000;//10 minutes
    private final Logger logger = LoggerFactory.getLogger("TokenManager");
    private final LinkedList<UserInfo> userInfos = new LinkedList<>();
}
