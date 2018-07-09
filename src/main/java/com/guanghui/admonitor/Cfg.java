package com.guanghui.admonitor;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Created by cuiju on 2016/8/11.
 *
 */

class Cfg {
    int loadCfg(String fn) {
        int res = 0;
        properties = new Properties();
        try {
            properties.load(new FileInputStream(new File(fn)));
            setServMonitor2CntPort(getInt("serv_recv_listen_port", servMonitor2CntPort));
            setServCnt2MonitorPort(getInt("serv_send_listen_port", servCnt2MonitorPort));
            setCatalogUiListenPort(getInt("catalog_ui_listen_port", catalogUiListenPort));
            setDatabaseUrl(getString("database_url", databaseUrl));
            setDbPasswd(getString("database_password", dbPasswd));
            setDbDriver(getString("database_driver", dbDriver));
            setDbuser(getString("database_user", dbuser));
            setDbWebPort(getInt("db_web_port", getDbWebPort()));
        }catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            res = -1;
        }
        return res;
    }

    private String getString(String key, String defStr){
        String str =  properties.getProperty(key);
        if (str == null) {
            logger.info("get {}={}(defualt value)", key, defStr);
            str = defStr;
        }else {
            logger.info("get {}={}", key, str);
        }
        return str;
    }

    private int getInt(String key, int defValue){
        String str = properties.getProperty(key);
        int value = defValue;
        try {
            value = Integer.parseInt(str);
            logger.info("get {}={}", key, value);
        }catch (Exception e) {
            logger.info("get {}={}(default value)", key, defValue);
        }
        return value;
    }

    private double getDouble(String key, double defValue){
        String str = properties.getProperty(key);
        double value = defValue;
        try {
            value = Double.parseDouble(str);
            logger.info("get {}={}", key, value);
        }catch (Exception e) {
            logger.info("get {}={}(default value)", key, defValue);
        }
        return value;
    }

    private Properties properties;
    private int servMonitor2CntPort = 16010;
    private int servCnt2MonitorPort = 16020;
    private String databaseUrl = "jdbc:h2:./db/db";
    private String dbuser = "sa";
    private String dbPasswd = "cjs567";
    private int catalogUiListenPort = 10086;
    private int dbWebPort = 8082;
    private String dbDriver = "org.h2.Driver";


    public int getServMonitor2CntPort() {
        return servMonitor2CntPort;
    }

    private void setServMonitor2CntPort(int servMonitor2CntPort) {
        this.servMonitor2CntPort = servMonitor2CntPort;
    }

    public int getServCnt2MonitorPort() {
        return servCnt2MonitorPort;
    }

    private void setServCnt2MonitorPort(int servCnt2MonitorPort) {
        this.servCnt2MonitorPort = servCnt2MonitorPort;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    private void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public String getDbuser() {
        return dbuser;
    }

    private void setDbuser(String dbuser) {
        this.dbuser = dbuser;
    }

    public String getDbPasswd() {
        return dbPasswd;
    }

    private void setDbPasswd(String dbPasswd) {
        this.dbPasswd = dbPasswd;
    }

    public int getCatalogUiListenPort() {
        return catalogUiListenPort;
    }

    private void setCatalogUiListenPort(int catalogUiListenPort) {
        this.catalogUiListenPort = catalogUiListenPort;
    }

    public int getDbWebPort() {
        return dbWebPort;
    }

    private void setDbWebPort(int dbWebPort) {
        this.dbWebPort = dbWebPort;
    }

    private Logger logger = LoggerFactory.getLogger("Cfg");

   private static Cfg cfg = new Cfg();
   static Cfg getInstance(){
       return cfg;
   }
   private Cfg(){}

    public String getDbDriver() {
        return dbDriver;
    }

    private void setDbDriver(String dbDriver) {
        this.dbDriver = dbDriver;
    }
}
