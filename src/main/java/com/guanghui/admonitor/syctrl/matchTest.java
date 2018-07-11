package com.guanghui.admonitor.syctrl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class matchTest {
    public static  void main(String[] args){
        startMatch m = new startMatch();
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(m ,0, 5, TimeUnit.SECONDS);
        while(true){
            try {
                System.out.println("main");
                Thread.sleep(2000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}

