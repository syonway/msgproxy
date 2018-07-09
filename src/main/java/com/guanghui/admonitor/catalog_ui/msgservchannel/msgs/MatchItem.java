package com.guanghui.admonitor.catalog_ui.msgservchannel.msgs;

/**
 * Created by cuiju on 2017/1/12. 10 17
 */
public class MatchItem {

    public int clipid;//暂时
    public long startTime;
    public long endTime;
    public String refurl;
    public boolean isAutoMatched;
    public boolean isCatalogued;
    public boolean isChecked;
    public boolean isFinalChecked;
    //check完成后添加确认的广告记录进入数据库
}
