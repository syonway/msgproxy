package com.guanghui.admonitor.syctrl;

import com.guanghui.admonitor.DemoLoop;
import com.guanghui.admonitor.backservice.SendMsg;

import java.text.SimpleDateFormat;
import java.util.Date;

public class startMatch implements Runnable{
    @Override
    public void run(){
        DemoLoop dm = DemoLoop.getInstance();
        SendMsg sendms = dm.getSendMsg();
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        System.out.println(dateFormat.format( now ));

    }
}
