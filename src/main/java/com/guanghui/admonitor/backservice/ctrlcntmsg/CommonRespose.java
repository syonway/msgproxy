package com.guanghui.admonitor.backservice.ctrlcntmsg;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by cuiju on 2017/2/13. 19 05
 */
public class CommonRespose {
    public int code = 0;
    public String info = "ok";

    @JsonIgnore
    public String toString(){
        return "code=" + code + ", info=" + info;
    }
}
