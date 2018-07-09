package com.guanghui.admonitor.catalog_ui.msgclientchannel.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guanghui.admonitor.catalog_ui.msgclientchannel.calfeaturemsg.CalTaskMsg;
import com.guanghui.admonitor.catalog_ui.msgclientchannel.calfeaturemsg.CalingTasks;
import com.guanghui.admonitor.catalog_ui.msgclientchannel.calfeaturemsg.MsgId;
import com.guanghui.admonitor.catalog_ui.msgservchannel.msgs.CommonResponse;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by cuiju on 2017/1/19. 17 01
 */
public class ClientSocket {
    public int connectTocCalService(String ip, int port){
        if (isConnect){
            if (this.ip.equals(ip) && this.port == port){
                return 0;
            }
            try {
                socket.close();
            } catch (IOException e) {
                logger.error(ExceptionUtils.getStackTrace(e));
                return -1;
            }
        }
        try {
            socket = new Socket(ip, port);
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            return -1;
        }
        this.setIp(ip);
        this.port = port;
        isConnect = true;
        return 0;
    }

    public CalingTasks getCalingTask(){
        if(sendRequst(null, MsgId.GET_CALING_TASKS_ID) != 0)  {
            return null;
        }
        byte[] responseBa = recvResponse();
        if (responseBa == null){
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(responseBa, CalingTasks.class);
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    public CommonResponse addCalTask2CalService(CalTaskMsg calTaskMsg){
         return taskCmd(calTaskMsg, MsgId.START_CAL_TASK_MSG_ID);
    }

    public CommonResponse stopCalTask(CalTaskMsg calTaskMsg){
        return taskCmd(calTaskMsg, MsgId.STOP_CAL_TASK_MSG_ID);
    }

    public CommonResponse changeTaskMask(CalTaskMsg calTaskMsg){
        return taskCmd(calTaskMsg, MsgId.CHANGE_MASK_MSG_ID);
    }

    private CommonResponse taskCmd(CalTaskMsg calTaskMsg, int dataid){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            byte[] calTaskMsgBa = objectMapper.writeValueAsBytes(calTaskMsg);
            if (sendRequst(calTaskMsgBa, dataid) != 0) {
                return null;
            }
            byte[] responseMsg = recvResponse();
            if (responseMsg == null){
                return null;
            }
            return objectMapper.readValue(responseMsg, CommonResponse.class);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    private byte[] recvResponse(){
        byte[] msg;
        try {
            DataInputStream inFServ = new DataInputStream(socket.getInputStream());
            byte[] lenBa = new byte[4];
            ByteBuffer lenBb = ByteBuffer.allocate(4);
            int rn = inFServ.read(lenBa);
            if (rn != 4){
                logger.error("read {} bytes actual read {} bytes", 4, rn);
                return null;
            }
            lenBb.put(lenBa);
            lenBb.flip();
            int msglen = lenBb.getInt();
            msg = new byte[msglen];
            rn = inFServ.read(msg);
            if (rn != msglen){
                logger.error("read {} bytes actual read {} bytes", msglen, rn);
                return null;
            }
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            return null;
        }
        return msg;
    }

    private int sendRequst(byte[] msgBa, int dataid){
        if (!isConnect){
            connectTocCalService(ip, port);
            if (!isConnect){
                return -1;
            }
        }
        ByteBuffer dataidBb = ByteBuffer.allocate(4);
        dataidBb.putInt(dataid);
        dataidBb.flip();
        byte[] dataidBa = new byte[4];
        dataidBb.get(dataidBa);
        int msglen = 4 + msgBa.length;
        ByteBuffer lenBb = ByteBuffer.allocate(4);
        lenBb.putInt(msglen);
        lenBb.flip();
        byte[] lenBa = new byte[4];
        lenBb.get(lenBa);
        try {
            DataOutputStream out2Serv = new DataOutputStream(socket.getOutputStream()) ;
            out2Serv.write(lenBa);
            out2Serv.write(dataidBa);
            //noinspection ConstantConditions
            if (msgBa != null) {
                out2Serv.write(msgBa);
            }
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            return -1;
        }
        return 0;
    }

    private String ip;
    private int port;

    private boolean isConnect = false;
    private Socket socket;
    private Logger logger = LoggerFactory.getLogger("ClientSocket");
    private void setIp(String ip) {
        this.ip = ip;
    }
}
