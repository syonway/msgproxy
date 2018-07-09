package com.guanghui.admonitor.catalog_ui.msgclientchannel;

import com.guanghui.admonitor.catalog_ui.msgclientchannel.calfeaturemsg.CalTaskMsg;
import com.guanghui.admonitor.catalog_ui.msgclientchannel.calfeaturemsg.CalingTasks;
import com.guanghui.admonitor.catalog_ui.msgclientchannel.impl.ClientSocket;
import com.guanghui.admonitor.catalog_ui.msgservchannel.msgs.CommonResponse;

/**
 * Created by cuiju on 2017/1/19. 16 22
 */
public class MsgChannelClient {

    public int conn2CalService(String ip, int port) {
        return sock.connectTocCalService(ip, port);
    }

    public CommonResponse addCalTask(CalTaskMsg msg){
        return sock.addCalTask2CalService(msg);
    }

    public CommonResponse stopCalTask(CalTaskMsg msg){
        return sock.stopCalTask(msg);
    }

    public CommonResponse changeCalTaskMask(CalTaskMsg msg){
        return sock.changeTaskMask(msg);
    }

    public CalingTasks getCalingTask(){
        return sock.getCalingTask();
    }

    private ClientSocket sock = new ClientSocket();
    private String ip;
    private int port;

    public String getIp() {
        return ip;
    }

    private void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    private void setPort(int port) {
        this.port = port;
    }
}
