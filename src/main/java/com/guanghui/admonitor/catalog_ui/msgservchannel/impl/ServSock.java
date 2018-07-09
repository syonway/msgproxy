package com.guanghui.admonitor.catalog_ui.msgservchannel.impl;

import com.guanghui.admonitor.catalog_ui.msgservchannel.MsgProcessSurface;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by cuiju on 2015/10/12.
 *
 */
 public class ServSock implements Runnable{
     public ServSock(MsgProcessSurface processSurface){
         this.processSurface = processSurface;
         Thread thread = new Thread(tokenManager);
         thread.setDaemon(true);
         thread.start();
     }
    @Override
    public void run() {
        isWorking.set(true);
        while (isWorking.get()){
            try {
                selector.select(ISockLibConstants.FIVE_MS); //有channel准备好注册的事件，select()返回，返回值为可以操作的channel的个数
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                //SelectedKey是channel与Selector绑定的标记，每
                //将一个channel注册到一个selector就会产生一个SelectedKey，并将这个SelectedKey放入到Selected的key set
                while (it.hasNext()){
                    SelectionKey key = it.next();
                    eventPrc(key);
                    it.remove();
                }
            } catch (IOException e) {
                logger.error(ExceptionUtils.getStackTrace(e));
            }
        }
    }

    final public boolean startListen(String addr, int port){
        if (ssc != null) {
            return false;
        }
        if (logger == null) {
            return false;
        }
        boolean res = false;
        try{
            selector = Selector.open(); //获得一个通道管理器
            ssc = ServerSocketChannel.open(); //获得一个ServerSocket通道,监听新进来的TCP连接的通道
            ss = ssc.socket(); //监听特定端口,负责接收客户连接请求
            ss.bind(new InetSocketAddress(addr, port));
            ssc.configureBlocking(false);//设置通道为非阻塞
            ssc.register(selector, SelectionKey.OP_ACCEPT, this);
            /*将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_ACCEPT事件，注册

        *该事件后，当该事件到达时，selector.select()会返回，如果该事件没到达selector.select()

        *会一直阻塞。

        */
            res = true;

        }catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }finally {
            if (!res) {
                try {
                    if (ss != null) {
                        ss.close();
                    }
                    if (ssc != null) {
                        ssc.close();
                    }
                }catch (Exception e) {
                    logger.error(ExceptionUtils.getStackTrace(e));
                }
            }
        }
        return res;
    }

    public void stopListen(){
        isWorking.set(false);
    }

    /****************************************************/
    private int acceptNewSock(SocketChannel sc){
         AppServSock appServSock = new AppServSock(sc, tokenManager, processSurface);
         Thread appT = new Thread(appServSock);
         appT.setDaemon(true);
         appT.start();
        return 0;
    }
    private int eventPrc(SelectionKey key){
        int res = 0;
        if (!key.isAcceptable() || ssc == null) {
            logger.error("selector state error, conn't acceptable");
            return -1;
        }
        try {
            SocketChannel sc = ssc.accept();
            sc.configureBlocking(false);
            res = acceptNewSock(sc); //这里是接收状态
        }catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return res;
    }

    private AtomicBoolean isWorking = new AtomicBoolean(); //原子类
    private MsgProcessSurface processSurface;
    private TokenManager tokenManager = new TokenManager();
    private Logger logger = LoggerFactory.getLogger("ServSock");
    private ServerSocketChannel ssc;
    private ServerSocket ss;
    private Selector selector;
}
