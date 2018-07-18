package com.guanghui.admonitor.catalog_ui.demo;

import com.guanghui.admonitor.catalog_ui.msgservchannel.msgs.*;
import com.guanghui.admonitor.syctrl.TimeTransform;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.LinkedList;

/**
 * Created by cuiju on 2016/10/16.
 *
 */
public class H2Db {
   // private String dburl = "jdbc:h2:~/ad";
   public synchronized int startDb(int webPort, String dbUrl, String dbDriver, String dbusr, String dbpasswd){
        String dburl =  dbUrl;//"jdbc:h2:./db/db";
        try {
            Class.forName(dbDriver);//"org.h2.Driver");
            org.h2.tools.Server.createWebServer("-webPort", Integer.toString(webPort)).start();
            try {
                //connection = DriverManager.getConnection(dburl + ";IFEXISTS=TRUE", "sa", "cjs567");
                connection = DriverManager.getConnection("jdbc:h2:./db/db" + ";IFEXISTS=TRUE", dbusr, dbpasswd);
                connection.setAutoCommit(true);//执行一条语句就自动提交一次
                initDatabase();
                System.out.println("init h2 success");

                String driver = "com.mysql.cj.jdbc.Driver";
                String url = "jdbc:mysql://115.28.61.129:3306/syads?serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8 ";
                String user = "root";
                String password = "123456";
                Class.forName(driver);
                conn2 = DriverManager.getConnection(url,user,password);
                //if(!conn2.isClosed())
                //sout
                UserProductInfo a = getUserProductInfo(1);
                ReqChannelMatchMsg rsq = new ReqChannelMatchMsg();
                rsq.channelid = 17;
                rsq.startTime = 1527715447280L;
                rsq.endTime = 1527715670040L;


            

            } catch (Exception e) {
                logger.error("database doset not exist: {} ", ExceptionUtils.getStackTrace(e));
                e.printStackTrace();
                //connection = DriverManager.getConnection(dburl, "sa", "cjs567");

                connection = DriverManager.getConnection(dburl, dbusr, dbpasswd);
                connection.setAutoCommit(true);
                initDatabase();

                //call create table, init database

            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            e.printStackTrace();

            System.out.println("init h2 fail");
            return -1;
        }
        return 0;
    }

    //private synchronized int initdb(){}

    private synchronized int initDatabase(){
       /*this is just demo how to use msgservchannel, there are more work to do, eample:
       *  1. create inex
       *  2. add more column
       * */
        String createSequenceForClassfictionSql = "CREATE SEQUENCE IF NOT EXISTS classficationid_seq";
        String createSeqForUserId = "CREATE SEQUENCE IF NOT EXISTS userid_seq";
        String createSeqForChannelId = "CREATE SEQUENCE IF NOT EXISTS channelid_seq";
        String crtSeqForTaskId = "CREATE SEQUENCE IF NOT EXISTS taskid_seq";
        String crtSeqFromClipIdInfo = "CREATE SEQUENCE IF NOT EXISTS clipinfoid_seq";
        String crtSeqRefClipId = "CREATE SEQUENCE IF NOT EXISTS refclipid_seq";
        String crtSeqMaskRectid = "CREATE SEQUENCE IF NOT EXISTS mask_rectid_seq";
        String crtSeqAdOwnerId = "CREATE SEQUENCE IF NOT EXISTS adownerid_seq";
        String crtSeqAdAgentId = "CREATE SEQUENCE IF NOT EXISTS adagentid_seq";
        String crtSeqChnlMaskId = "CREATE SEQUENCE IF NOT EXISTS channel_maskid_seq";

        String createAdClassficationSql = "create table if not exists ad_classfication_tab " +
                "(level INTEGER not null, " +
                "class_item_id INTEGER NOT NULL DEFAULT nextval('classficationid_seq') PRIMARY KEY, " +
                "parent_id int NOT NULL, " +
                "name varchar(50) NOT NULL," +
                "is_using BOOLEAN DEFAULT false)";
        String createUserSql = "create table if not exists user_tab" +
                "(userid INTEGER NOT NULL DEFAULT nextval('userid_seq') PRIMARY KEY, " +
                "user_name varchar(20) NOT NULL UNIQUE, " +
                "passwd VARCHAR(20) NOT NULL," +
                "name VARCHAR(20)," +
                "phone_number VARCHAR(200), " +
                "email varchar(100) UNIQUE)";
        String createChannelTaskSql = "CREATE TABLE IF NOT EXISTS channel_tab" +
                "(channelid INTEGER NOT NULL DEFAULT nextval('channelid_seq')  PRIMARY KEY," +
                "name varchar(50) NOT NULL UNIQUE," +
                "url VARCHAR(255) NOT NULL UNIQUE," +
                "catalog_time BIGINT NOT NULL," +
                "check_time BIGINT," +
                "confirm_time BIGINT," +
                "catalog_userid INTEGER NOT NULL REFERENCES user_tab (userid))";

        String crtAdOwner = "CREATE TABLE IF NOT EXISTS ad_owner_tab" +
                "(ad_owner_id INTEGER NOT NULL DEFAULT nextval('adownerid_seq') PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL)";
        String crtAdAgent = "CREATE TABLE IF NOT EXISTS ad_agent_tab" +
                "(ad_agent_id INTEGER NOT NULL DEFAULT nextval('adagentid_seq') PRIMARY KEY," +
                "name VARCHAR(100))";

        String crtMaskRect = "CREATE TABLE IF NOT EXISTS mask_tab" +
                "(maskid INTEGER NOT NULL DEFAULT nextval('taskid_seq') PRIMARY KEY," +
                "lefttopx INTEGER NOT NULL CHECK (lefttopx > 0)," +
                "lefttopy INTEGER NOT NULL CHECK (lefttopy > 0)," +
                "width INTEGER NOT NULL CHECK(width > 0)," +
                "height INTEGER NOT NULL CHECK (height > 0))";

        String createAdClipSql = "CREATE TABLE IF NOT EXISTS adclipinfo_tab" +
                "(clipid INTEGER NOT NULL DEFAULT nextval('clipinfoid_seq') PRIMARY KEY ," +
                "add_by_userid INTEGER NOT NULL REFERENCES user_tab (userid)," +
                "chk_by_userid INTEGER REFERENCES user_tab (userid)," +
                "confirm_by_userid INTEGER REFERENCES user_tab (userid)," +
                "own_channelid INTEGER NOT NULL REFERENCES channel_tab (channelid)," +
                "start_time BIGINT NOT NULL," +
                "frame_nr INTEGER NOT NULL," +
                "classficationid INTEGER NOT NULL REFERENCES ad_classfication_tab (class_item_id)," +
                "ad_ownerid INTEGER REFERENCES ad_owner_tab (ad_owner_id)," +
                "ad_agentid INTEGER REFERENCES ad_agent_tab (ad_agent_id)," +
                "loudness FLOAT )" ;
        String crtChannelMaskSql = "CREATE TABLE IF NOT EXISTS channel_mask_tab" +
                "(id INTEGER NOT NULL DEFAULT nextval('channel_maskid_seq') PRIMARY KEY," +
                "channelid INTEGER NOT NULL REFERENCES channel_tab (channelid)," +
                "maskids VARCHAR(100) NOT NULL," +
                "start_time TIMESTAMP NOT NULL," +
                "end_time TIMESTAMP)";
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(createSequenceForClassfictionSql);
            stmt.executeUpdate(createSeqForUserId);
            stmt.executeUpdate(createSeqForChannelId);
            stmt.executeUpdate(crtSeqForTaskId);
            stmt.executeUpdate(crtSeqForTaskId);
            stmt.executeUpdate(crtSeqFromClipIdInfo);
            stmt.executeUpdate(crtSeqRefClipId);
            stmt.executeUpdate(crtSeqRefClipId);
            stmt.executeUpdate(crtSeqMaskRectid);
            stmt.executeUpdate(crtSeqAdOwnerId);
            stmt.executeUpdate(crtSeqAdAgentId);
            stmt.executeUpdate(crtSeqChnlMaskId);

            stmt.executeUpdate(createAdClassficationSql);
            stmt.executeUpdate(createUserSql);
            stmt.executeUpdate(createChannelTaskSql);
            stmt.executeUpdate(crtAdAgent);
            stmt.executeUpdate(crtAdOwner);
            stmt.executeUpdate(crtMaskRect);
            stmt.executeUpdate(createAdClipSql);
            stmt.executeUpdate(crtChannelMaskSql);


        } catch (SQLException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return 0;
    }

    private synchronized int getUserId(String name){
        int id = -1;
        String sql = "SELECT userid from user_tab where user_name = ?";
        try {
            PreparedStatement statement = conn2.prepareStatement(sql);
            statement.setString(1, name);
            ResultSet rs = statement.executeQuery();
            if (rs.next()){
                id = rs.getInt("userid");
            }
        } catch (SQLException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return id;
    }
    public synchronized UserTaskInfo getUserTaskInfo(String userName){
        UserTaskInfo res = null;
        int id = getUserId(userName);
        if (id < 0){
            logger.error("no user: {}", userName);
            return null;
        }
        String sql = "SELECT channelid,name, url, catalog_time  FROM channel_tab WHERE catalog_userid = ?";
        try {
            PreparedStatement statement = conn2.prepareStatement(sql);
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            res = new UserTaskInfo();
            while (rs.next()){
                Task task = new Task();
                task.channelID = rs.getInt("channelid");
                task.channelName = rs.getString("name");
                task.channelResourcePath = rs.getString("url");
                task.taskStartTime = rs.getLong("catalog_time");
                //task.taskEndTime = rs.getLong("last_media_time");
                System.out.println("taskinfo----");
                System.out.println(task.channelID+" "+task.channelName+task.channelResourcePath+task.taskStartTime);
                res.tasks.push(task);
            }
        } catch (SQLException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            e.printStackTrace();
        }
        return res;
    }

    public synchronized AdOwnerInfo getAdOwners(){
        AdOwnerInfo res = null;
        String sql = "SELECT * FROM firm_info ";
        try{
            PreparedStatement statement = conn2.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            res = new AdOwnerInfo();
            while (rs.next()){
                AdOwnerItem owner = new AdOwnerItem();
                owner.id = rs.getLong("id");
                owner.itemName = rs.getString("name");
                res.items.push(owner);
            }
        }catch (SQLException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            e.printStackTrace();
        }
        return res;
        
    }

    public synchronized ChannelMatchMsg getChannelMatch(ReqChannelMatchMsg reqMsg){
        //?+8h后可能调整
        String sql = "select * from adclipinfo_tab inner join ad_info on adclipinfo_tab.url= ad_info.file_path where own_channelid = ? and start_time between ? and ?";
        ChannelMatchMsg res = new ChannelMatchMsg();
        try{
            PreparedStatement statement = conn2.prepareStatement(sql);
            statement.setInt(1, reqMsg.channelid);
            //要改成2002-01-29 04:33:38.531格式字符串时间
            statement.setLong(2, reqMsg.startTime);
            statement.setLong(3, reqMsg.endTime);
            System.out.println("select * from adclipinfo_tab inner join adinfo on adclipinfo_tab.url= adinfo.lambdaFile where own_channelid = "+reqMsg.channelid+" and start_time between '"+reqMsg.startTime+"' and '"+reqMsg.endTime+"'");
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                MatchItem ma = new MatchItem();
                ma.clipid = rs.getInt("clipid");
                ma.refname = rs.getString("pro_desc");
                ma.ownerid = rs.getInt("ad_ownerid");
                ma.agentid = rs.getInt("ad_agentid");
                ma.classficationid = rs.getInt("classficationid");
                ma.channelid = rs.getInt("own_channelid");
                ma.refurl = rs.getString("url");
                ma.startTime = rs.getLong("start_time");
                //ma.frameNr =  rs.getInt("frame_nr");
                ma.endTime = ma.startTime+rs.getInt("frame_nr")/24*1000;
                res.matchItems.add(ma);
                System.out.println("match");
                System.out.println("match:"+rs.getInt("clipid")+" "+ma.startTime+ma.endTime);
            }
        }catch(Exception e){
            e.printStackTrace();
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return res;
    }
    
    public synchronized UserProductInfo getUserProductInfo(int ownerid){
       UserProductInfo res = null;
       String sql = "SELECT * FROM ad_info  WHERE firm_id = ?";
       try{
           PreparedStatement statement = conn2.prepareStatement(sql);
           statement.setInt(1, ownerid);
           ResultSet rs = statement.executeQuery();
           res = new UserProductInfo();
           System.out.println("userproductinfo----");
           while(rs.next()){
               Productinfo pro = new Productinfo();
               pro.id = rs.getLong("id");
               pro.itemName = rs.getString("brand")+" "+rs.getString("pro_desc");
               res.items.push(pro);
               System.out.println(pro.id+" "+pro.itemName);
           }

        }catch (Exception e){
           logger.error(ExceptionUtils.getStackTrace(e));
           e.printStackTrace();
       }
       return res;
    }

    public synchronized UserInfo getUserInfo(String userName){
        String sql = "select * from user_tab where user_name = ?" ;
        UserInfo userInfo = null;
        try {
            PreparedStatement statement = conn2.prepareStatement(sql);
            statement.setString(1, userName);
            ResultSet rs = statement.executeQuery();
            if (rs.next()){
                userInfo = new UserInfo();
                userInfo.email = rs.getString("email");
                userInfo.name = userName;
                userInfo.passWd = rs.getString("passwd");
                userInfo.phoneNumber = rs.getString("phone_number");
                userInfo.userName = rs.getString("user_name");
            }
        } catch (SQLException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return userInfo;
    }

    private class ClassItemWithParentID{
        ClassItem classItem = new ClassItem();
        int parentId;
        int level;
        LinkedList<ClassItemWithParentID> subitem = new LinkedList<>();
    }

    private synchronized LinkedList<ClassItemWithParentID> getAdClassAtLevel(int level, int parentid){
        LinkedList<ClassItemWithParentID> ciid = new LinkedList<>();
         String getLevelClassItemSql = "select class_item_id, name from ad_classfication_tab where level = ? " +
                 "and parent_id = ? and is_using = ?";
         logger.debug("getAdClassAtLevel with level={}, parentid={}", level, parentid);
        try {
            PreparedStatement statement = conn2.prepareStatement(getLevelClassItemSql);
            statement.setInt(1, level);
            statement.setInt(2, parentid);
            statement.setInt(3, 1);
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                ClassItemWithParentID ci = new ClassItemWithParentID();
                ci.level = level;
                ci.parentId = parentid;
                ci.classItem.id = rs.getInt("class_item_id");
                ci.classItem.itemName = rs.getString("name");
                ciid.push(ci);
                logger.debug("classitem:level ={},id={}, name={}", level, ci.classItem.id, ci.classItem.itemName);
            }
        } catch (SQLException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return ciid;
    }

    private void getSubClassItem(ClassItemWithParentID ci){
        ci.subitem = getAdClassAtLevel(ci.level + 1, ci.classItem.id);
        if (ci.subitem.size() == 0){
            return;
        }
        ci.subitem.forEach(this::getSubClassItem);
    }

    private void ciWithIdToCi(ClassItemWithParentID ciid, ClassItem ci){
        if (ciid.subitem.size() == 0){
            return;
        }
        ciid.subitem.forEach(subciid ->{
            ci.subItems.push(subciid.classItem);
            ciWithIdToCi(subciid, subciid.classItem);
        });
    }

    AdClassfication getAdClassfication(){
        AdClassfication res = new AdClassfication();
        LinkedList<ClassItemWithParentID> rootci = getAdClassAtLevel(0, 0);
        rootci.forEach(this::getSubClassItem);
        rootci.forEach(ciid -> {
             res.items.push(ciid.classItem);
             ciWithIdToCi(ciid, ciid.classItem);
        });
        return res;
    }
    private Connection connection;
    private Connection conn2;
    final private Logger logger = LoggerFactory.getLogger("H2Db");
}
