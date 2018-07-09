package com.guanghui.admonitor.backservice.ctrlcntmsg.recv;

/**
 * Created by cuiju on 2017/3/8. 15 01
 */
public class ReportCalTaskStatus {
    public String calPath;
    public String nasIp;
    public String caledFrameTime;

    public long caledFrameTimeLong;
    public void str2long(){
        caledFrameTimeLong = Long.parseLong(caledFrameTime);
    }

    public String toString(){
        return nasIp + "/" + calPath + ", caledFrameTime:" + caledFrameTime;
    }
}
