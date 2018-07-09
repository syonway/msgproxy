package com.guanghui.admonitor.catalog_ui.msgservchannel;

import com.guanghui.admonitor.catalog_ui.msgservchannel.impl.ServSock;

/**
 * Created by cuiju on 2017/1/12. 14 28
 */
public class MsgChannelServer {
    public MsgChannelServer(String bindAddr, int listenPort, MsgProcessSurface processSurface){
        this.bindAddr = bindAddr;
        this.listenPort = listenPort;
        this.processSurface = processSurface;
    }

    public boolean startWork(){
        servSock = new ServSock(processSurface);
        if(servSock.startListen(bindAddr, listenPort)){
            Thread sthread = new Thread(servSock);
            sthread.setDaemon(true);//守护线程
            sthread.start();
            return true;
        }
        return false;
    }

    public void stopWork(){
        servSock.stopListen();
    }

    private MsgProcessSurface processSurface;
    private ServSock servSock;
    private String bindAddr = "0.0.0.0";
    private int listenPort = 10080;
}
