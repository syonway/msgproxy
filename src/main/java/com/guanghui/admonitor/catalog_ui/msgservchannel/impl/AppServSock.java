package com.guanghui.admonitor.catalog_ui.msgservchannel.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guanghui.admonitor.DemoMain;
import com.guanghui.admonitor.catalog_ui.msgservchannel.MsgProcessSurface;
import com.guanghui.admonitor.catalog_ui.msgservchannel.msgs.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by cuiju on 2015/10/12.
 *
 */

 public class AppServSock implements Runnable{
    AppServSock(SocketChannel sc, TokenManager tokenManager, MsgProcessSurface msgProcesser){
        this.sc = sc;
        this.tokenManager = tokenManager;
        this.msgProcesser = msgProcesser;
        try {
            sc.configureBlocking(true);
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }
    private final int REQ_USER_LOGIN_MSG = 101;
    private final int REQ_USER_TASK_INFO = 102;

    private final int REQ_AD_CLASSFICATION_MSG = 300;
    @Override
    public void run() {
        final int INT_LEN = 4;
        state = AppServSockState.OK;
        while(state == AppServSockState.OK){
            int len;
            int dataid;
            ByteBuffer lenBuf = ByteBuffer.allocate(INT_LEN);
            ByteBuffer dataidBuf = ByteBuffer.allocate(INT_LEN);
            try {
                int rNr = sc.read(lenBuf);
                if (rNr == -1){
                    logger.error("peer close the socket.");
                    System.out.println("peer close the socket.");
                    break;
                }
                if(lenBuf.limit() != INT_LEN){
                    logger.error("read 4 bytes len failed.");
                    System.out.println("read 4 bytes len failed.");
                    break;
                }
                lenBuf.flip();
                len = lenBuf.getInt();
                logger.info("get msglen = {}", len);
                sc.read(dataidBuf);
                if (dataidBuf.limit() != INT_LEN){
                    logger.error("read 4 bytes len failed");
                    System.out.println("read 4 bytes len failed");
                    break;
                }
                dataidBuf.flip();
                dataid = dataidBuf.getInt();
                logger.info("get dataid = {}", dataid);
                System.out.println("dataid: "+dataid);
                int msglen = len - INT_LEN;
                byte[] token = null;
                if (dataid != REQ_USER_LOGIN_MSG){
                     int tokelen;
                     ByteBuffer tokeLenBuf = ByteBuffer.allocate(INT_LEN);
                     sc.read(tokeLenBuf);
                     tokeLenBuf.flip();
                     tokelen = tokeLenBuf.getInt();
                     msglen = msglen - INT_LEN - tokelen;
                     ByteBuffer tokeBuf = ByteBuffer.allocate(tokelen) ;
                     sc.read(tokeBuf);
                     tokeBuf.flip();
                     token = new byte[tokelen];
                     tokeBuf.get(token);
                }
                byte[] msg = null;
                if(dataid != REQ_AD_CLASSFICATION_MSG && dataid != REQ_USER_TASK_INFO) {
                    ByteBuffer msgBuf = ByteBuffer.allocate(msglen);
                    sc.read(msgBuf);
                    if (msgBuf.limit() < msglen) {
                        logger.error("read {} bytes, but read {} bytes", msglen, msgBuf.limit());
                        break;
                    }
                    msgBuf.flip();
                    msg = new byte[msglen];
                    msgBuf.get(msg);
                }

                byte[] responseMsg;
                responseMsg = msgproc(dataid, token, msg);
                if (responseMsg == null){
                    logger.error("msgproc get null response");
                    break;
                }
                len = responseMsg.length;
                logger.info("msgproc return msg, length = {}", len);
                ByteBuffer sendLenBuf = ByteBuffer.allocate(INT_LEN);//容量len字节
                sendLenBuf.putInt(len);
                sendLenBuf.flip();
                sc.write(sendLenBuf);
                ByteBuffer sendMsgBuf = ByteBuffer.allocate(len);
                sendMsgBuf.put(responseMsg);
                sendMsgBuf.flip();
                //flip()方法用来将缓冲区准备为数据传出状态,执行以上方法后,输出通道会从数据的开头而不是末尾开始，准备写入而不是读取.
                sc.write(sendMsgBuf);
            } catch (IOException e) {
                logger.error(ExceptionUtils.getStackTrace(e));
                break;
            }
        }
        try {
            sc.close();
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    private byte[] msgproc(int dataid, byte[] token, byte[] reqmsg){
        final int REQ_CHANGE_USER_INFO_MSG = 103;
        final int REQ_CHANNEL_AD_MATCH_MSG = 203;
        final int REQ_ADD_NEW_AD_CLIP_INFO = 202;
        final int REQ_PRODUCTS_OWENER_MSG = 210;
        final int REQ_AD_OWNERS_MSG = 400;
        final int REQ_ADD_AD_RECORD_MSG = 204;
        byte[] res = null;
        switch (dataid){
            case REQ_AD_CLASSFICATION_MSG:
                res = getAdClassfication(token);
                break;
            case REQ_ADD_NEW_AD_CLIP_INFO:
                res = addAdClipInfo(reqmsg, token);
                break;
            case REQ_CHANGE_USER_INFO_MSG:
                res = changeUserInfo(reqmsg, token);
                break;
            case REQ_CHANNEL_AD_MATCH_MSG:
                res = getChannelMatch(reqmsg, token);
                break;
            case REQ_ADD_AD_RECORD_MSG:
                res = addAdRecordInfo(reqmsg);
                break;
            case REQ_USER_LOGIN_MSG:
                res = login(reqmsg);
                break;
            case REQ_USER_TASK_INFO:
                res = getUserTaskInfo(token);
                break;
            case REQ_PRODUCTS_OWENER_MSG:
                res = getUserProducts(reqmsg,token);
                break;
            case REQ_AD_OWNERS_MSG:
                res = getAdOwners(reqmsg,token);
                break;

        }
        return res;
    }


    private byte[] getCommResponse(int code, String info){
        CommonResponse res = new CommonResponse();
        res.res.code = code;
        res.res.info = info;
        byte[] resByte = null;
        if (code < 0){
            state = AppServSockState.ERROR;
        }
        try {
            resByte = objectMapper.writeValueAsBytes(res);
        } catch (JsonProcessingException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return  resByte;
    }

    private byte[] getLoginFirstResponse(){
         return getCommResponse(-1, "not login or session timeou");
    }

    private byte[] getReqMsgFormaterror(){
        return getCommResponse(-2, "msg format error");
    }


    private byte[] getChannelMatch(byte[] reqmsg, byte[] token){
        String name = tokenManager.getNameFromToken(token);
        if (name == null){
            return getLoginFirstResponse();
        }
        try {
            ReqChannelMatchMsg reqChannelMatchMsg = objectMapper.readValue(reqmsg, ReqChannelMatchMsg.class);
            ChannelMatchMsg matchMsg = msgProcesser.getChannelMatch(reqChannelMatchMsg, name);
            return objectMapper.writeValueAsBytes(matchMsg);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    private byte[] getAdClassfication(byte[] token){
        String name = tokenManager.getNameFromToken(token);
        if (name == null){
            return getLoginFirstResponse();
        }
        AdClassfication classfication = msgProcesser.getAdClassfication(name);
        byte[] res;
        try {
            res = objectMapper.writeValueAsBytes(classfication);
        } catch (JsonProcessingException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            res = null;
        }
        return res;
    }

    private byte[] addAdRecordInfo(byte[] reqmsg){
        if (reqmsg == null){
            logger.error("add reocrd without AdRecordInfo");
            state = AppServSockState.ERROR;
            return getReqMsgFormaterror();
        }

        CommonResponse response = msgProcesser.addAdRecordInfo();

        return null;
    }

    private byte[] addAdClipInfo(byte[] reqmsg, byte[] token ) {

        if (reqmsg == null){
            logger.error("addAdclipInfo without AdClipInfo");
            state = AppServSockState.ERROR;
            return getReqMsgFormaterror();
        }

        String name = tokenManager.getNameFromToken(token);
        if (name == null){
            return getLoginFirstResponse();
        }
        try {
            System.out.println("adclip.reqmsg--");
            System.out.println(new String(reqmsg));
            System.out.println("adclip.token--");
            System.out.println(new String(token));
            DemoMain dm = DemoMain.getInstance();
            JSONObject json = new JSONObject(new String(reqmsg));
            AdClipInfo clipInfo = new AdClipInfo();
            clipInfo.startTime = json.getLong("starttime");
            clipInfo.frameNr = json.getInt("framenr");
            clipInfo.channelName = json.getString("chlname");
            System.out.println(clipInfo.channelName+clipInfo.startTime);
            CommonResponse response = msgProcesser.addAdClipInfo(clipInfo, name,dm.getSendmsg());
            System.out.println("add clip result---  "+response.res.info);
            return objectMapper.writeValueAsBytes(response);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(ExceptionUtils.getStackTrace(e));
            state = AppServSockState.ERROR;
        }
        return null;
    }

    private byte[] getUserTaskInfo(byte[] token){
        String name = tokenManager.getNameFromToken(token);
        if (name == null){
            return getLoginFirstResponse();
        }
        UserTaskInfo userTaskInfo = msgProcesser.getUserTaskInfo(name);
        byte[] res;
        try {
            res = objectMapper.writeValueAsBytes(userTaskInfo);
        } catch (JsonProcessingException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            res = null;
        }
        return res;
    }

    private byte[] getUserProducts(byte[] reqmsg,byte[] token){
        System.out.println("userproducts.reqmsg--");
        System.out.println(new String(reqmsg));
        System.out.println("userproducts.token.name--");
        System.out.println(tokenManager.getNameFromToken(token));
        int ownerid = (new JSONObject(new String(reqmsg))).getInt("ownerid");
        if(ownerid < -1){
            return getCommResponse(-2,"cannot get users' products without username ");
        }
        UserProductInfo userproductinfo = msgProcesser.getUserProductInfo(ownerid);
        byte[] res;
        try {
            res = objectMapper.writeValueAsBytes(userproductinfo);
        } catch (JsonProcessingException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            res = null;
        }
        return res;
    }

    private byte[] getAdOwners(byte[] reqmsg,byte[] token){
        System.out.println("adowners.reqmsg--");
        System.out.println(new String(reqmsg));
        System.out.println("adowners.token--");
        System.out.println(new String(token));
        AdOwnerInfo adowners = msgProcesser.getAdOwnerInfo();
        byte[] res;
        try {
            res = objectMapper.writeValueAsBytes(adowners);
        } catch (JsonProcessingException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            e.printStackTrace();
            res = null;
        }

        return res;
    }

    private byte[] changeUserInfo(byte[] reqmsg, byte[] token){
        if (reqmsg == null){
            logger.error("login with null msg");
            return getReqMsgFormaterror();
        }
        try {
            UserInfo userInfo = objectMapper.readValue(reqmsg, UserInfo.class);
            System.out.println("changeuserinfo---");
            System.out.println(new String(reqmsg));
            if(userInfo != null){
                String name = tokenManager.getNameFromToken(token);
                if (name != null){
                    UserInfo newUserInfo = msgProcesser.changeUserInfo(userInfo);
                    if (newUserInfo == null){
                        return getCommResponse(-500, "change userinfo failed.");
                    }
                    return objectMapper.writeValueAsBytes(userInfo);
                }else {
                    getLoginFirstResponse();
                }
            }else {
                return getCommResponse(-2, "request format error");
            }
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    private byte[] login(byte[] reqmsg){
        System.out.println("loginmsg--");
        System.out.println(new String(reqmsg));
        if (reqmsg == null){
            logger.error("login with null msg");
            return getReqMsgFormaterror();
        }
        try {
            LoginMsg loginmsg = objectMapper.readValue(reqmsg, LoginMsg.class);
            UserInfo userInfo = msgProcesser.login(loginmsg);
            String token;
            if (userInfo != null){
                 token = tokenManager.newAuthenOk(loginmsg.userName, loginmsg.passWd);
            }else {
                return null;
            }
            logger.info("{} authen ok ", loginmsg.userName);
            byte[] json = objectMapper.writeValueAsBytes(userInfo);
            byte[] tokenByte = token.getBytes("UTF-8");
            int tokenLen = tokenByte.length;
            int msglen = json.length + tokenByte.length + 4;
            ByteBuffer buffer = ByteBuffer.allocate(4);
            int offset = 0;
            buffer.putInt(tokenLen);
            buffer.flip();
            byte[] msgbyte = new byte[msglen] ;
            byte[] tokenLenLen = new byte[4];
            buffer.get(tokenLenLen);
            System.arraycopy(tokenLenLen, 0, msgbyte, offset, tokenLenLen.length);
            offset += 4;
            System.arraycopy(tokenByte, 0, msgbyte, offset, tokenLen);
            offset += tokenLen;
            System.arraycopy(json, 0, msgbyte, offset, json.length);
            return msgbyte;
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    /*..............             ...............*/

    private MsgProcessSurface msgProcesser;
    private TokenManager tokenManager;
    enum AppServSockState {OK,  ERROR}
    private AppServSockState state;
    private SocketChannel sc;
    final private Logger logger = LoggerFactory.getLogger("AppServSock");
    final private ObjectMapper objectMapper = new ObjectMapper();
}



