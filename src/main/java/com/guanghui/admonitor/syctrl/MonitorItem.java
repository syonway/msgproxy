package com.guanghui.admonitor.syctrl;

public class MonitorItem{
    public int monitorid;
    private String monitortype;
    private int taskinf;
    private String ip;
    private String task_on;
    private String task_plan;
    private String lasttime;
    private String starttime;

    public MonitorItem(int mid){
        this.monitorid = mid;
    }

    public int getMonitorid(){
        return this.monitorid;
    }
    
    public void setaskinf(int inf){
        this.taskinf = inf;
    }

    public int gettaskinf(){ return this.taskinf; }

    public void settype(String type){
        this.monitortype = type;
    }

    public String gettype(){
        return this.monitortype;
    }

    public void setIp(String ip){
        this.ip = ip;
    }

    public String getIp(){
        return this.ip;
    }

    public void setTaskon(String task_on){
        this.task_on = task_on;
    }

    public String getTaskon(){
        return this.task_on;
    }

    public void setTaskplan(String task_plan){
        this.task_plan = task_plan;
    }

    public String getTask_plan(){
        return this.task_plan;
    }

    public void setStarttime(String starttime){ this.starttime = starttime; }

    public String getStarttime(){ return this.starttime; }

    public void setLasttime(String lasttime){ this.lasttime = lasttime; }

    public String getLasttime(){ return this.lasttime; }
}