package com.guanghui.admonitor.catalog_ui.msgservchannel.msgs;

import java.util.LinkedList;

/**
 * Created by cuiju on 2017/1/11. 21 51
 */
public class ClassItem {
    public int id;
    public String itemName;
    public LinkedList<ClassItem> subItems = new LinkedList<>();
}
