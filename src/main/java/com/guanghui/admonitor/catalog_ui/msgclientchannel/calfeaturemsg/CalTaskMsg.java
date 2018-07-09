package com.guanghui.admonitor.catalog_ui.msgclientchannel.calfeaturemsg;

import java.util.LinkedList;

/**
 * Created by cuiju on 2017/1/19. 16 54
 */
public class CalTaskMsg {
    public int maskid;
    public LinkedList<MaskRect> masks = new LinkedList<>();
    public String calPath;
}
