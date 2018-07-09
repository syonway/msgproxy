package com.guanghui.admonitor.backservice.ctrlcntmsg.recv;

import com.guanghui.admonitor.backservice.ctrlcntmsg.RecvTask;

/**
 * Created by cuiju on 2017/3/7. 18 14
 */
public class ReportRecordStatus {
    public RecvTask task;
    public String recordFrameTime;
    public long recordFrameTimeLong;
    public void string2long(){
        recordFrameTimeLong = Long.parseLong(recordFrameTime);
    }
    public String toString(){
        return task.toString() + ":" + recordFrameTime;
    }
}
