package com.guanghui.admonitor.backservice.ctrlcntmsg.send.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by cuiju on 2017/3/9. 22 35
 */
public class NasMountInfo {
    public String nasIp;
    public String mountPath;

    @JsonIgnore
    public String toString(){
        return "nasIp=" + nasIp + ", mountPath=" + mountPath;
    }
}
