package com.guanghui.admonitor.catalog_ui.msgservchannel.msgs;

/**
 * Created by cuiju on 2017/1/12. 10 17
 */
public class MatchItem {

    public int clipid;//暂时
    public int channelid;
    public long startTime;
    public long endTime;
    public String refurl;
    public long frameNr;
    public long matchNr;
    public float loudness;
    public boolean isAutoMatched = true;
    public boolean isCatalogued = false;
    public boolean isChecked = false;
    public boolean isFinalChecked = false;
    //check完成后添加确认的广告记录进入数据库
}
