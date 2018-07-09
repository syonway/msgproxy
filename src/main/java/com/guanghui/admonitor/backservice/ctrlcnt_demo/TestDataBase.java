package com.guanghui.admonitor.backservice.ctrlcnt_demo;

import com.guanghui.admonitor.backservice.ctrlcntmsg.recv.*;
import com.guanghui.admonitor.backservice.ctrlcntmsg.send.FindReplayClipTaskMsg;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.LinkedList;

/**
 * Created by cuiju on 2017/2/14. 19 22
 */

public class TestDataBase {

    private int insertReplayAdClip2Db(LinkedList<AdClipReplay> adClipReplays, int monitorid, int task_id){
        String sql = "INSERT INTO channel_replay_ad_clips VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        int res = 0;
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            adClipReplays.forEach(clip -> {
                RefClip refClip = getRefClipId(clip.adUrl);
                if (refClip == null){
                    logger.error("get {}'s refAdClip null", clip.adUrl);
                }else {
                    try {
                        statement.setInt(1, task_id);
                        statement.setInt(2, refClip.id);
                        statement.setInt(3, clip.adoffset);
                        statement.setInt(4, clip.matchFrameNr);
                        statement.setLong(5, clip.startMatchChannelTimeLong);
                        statement.setInt(6, monitorid);
                        statement.setString(7, "none");
                        statement.setBoolean(8, false);
                        statement.execute();
                    } catch (SQLException e) {
                        logger.error(ExceptionUtils.getStackTrace(e));
                    }
                }
            });
        } catch (SQLException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            res = -1;
        }
        return res;
    }

    public synchronized int addRptAdClips2Db(int monitorid, ChnlReplayAdClipMsg msg){
        int chnlid = getChannelID(msg.channelPath);
        if (chnlid < 0){
            logger.error("get {}'s channelid={}", msg.channelPath, chnlid);
            return -1;
        }
        int taskId = getAdClipSearchTaskId(chnlid, monitorid, msg.startTimeLong, msg.endTimeLong);
        if (taskId  < 0){
            logger.error("get 'chnlid={},monitorid={},starttime={},endtime={}'s taskid = {}",
                    chnlid, monitorid, msg.startTimeLong, msg.endTimeLong, taskId);
            return -1;
        }
        changeTaskState("search_adclips_task_tab", taskId, 2);
        return   insertReplayAdClip2Db(msg.replayAdClips, monitorid, taskId);
    }

    private int inssetSelfReplayClips2Db(int taskid, int monitorid, LinkedList<ReplayClip> replayClips){
        int res = 0;
        String sql = "INSERT INTO self_replay_clips_info VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            for (ReplayClip clip : replayClips){
                for (ReplayInfo info : clip.replayInfos){
                    statement.setInt(1, taskid);
                    statement.setLong(2, clip.fstartTimeLong);
                    statement.setLong(3, info.replayStartTimeLong);
                    statement.setInt(4, info.replayFrameNr);
                    statement.setInt(5, monitorid);
                    statement.setString(6, "none");
                    statement.setBoolean(7, false);
                }
            }
        } catch (SQLException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            res = -1;
        }
        return res;
    }

    public synchronized int addReplayClips(int monitorid, ReplayClipsMsg msg){
        int chnlId = getChannelID(msg.channelPath);
        if (chnlId < 0){
            logger.error(" get {}'s channelid={}", msg.channelPath, chnlId);
            return -1;
        }
        int taskid = getSelfTaskId(chnlId, monitorid, msg.startTimeLong, msg.endTimeLong);
        if (taskid < 0){
            logger.error("get taskid = {}", taskid);
            return -1;
        }
        changeTaskState("self_channel_search_task_tab", taskid, 2);
        return inssetSelfReplayClips2Db(taskid, monitorid, msg.replayClips);
    }

    private int getChannelID(String channelPath){
        String sql = "select channel_id from channel_info_tab where channel_path=?";
        int id = -1;
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, channelPath);
            ResultSet rs = statement.executeQuery();
            if(rs.next()){
                id = rs.getInt("channel_id");
            }
        } catch (SQLException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return id;
    }

    private int getTaskId(int channelId, int monitorid, long startTime, long endTime, String tabName){
        String sql = "SELECT task_id from " + tabName + " WHERE " +
                "channel_id=? and start_time=? and end_time=? and monitor_id=?";
        int id = -1;
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, channelId);
            statement.setLong(2, startTime);
            statement.setLong(3, endTime);
            statement.setInt(4, monitorid);
            ResultSet rs = statement.executeQuery();
            if(rs.next()){
                id = rs.getInt("task_id");
            }
        } catch (SQLException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return id;
    }

    private int  getSelfTaskId(int channelId, int monitorid, long startTime, long endTime){
        return getTaskId(channelId, monitorid, startTime, endTime, "self_channel_search_task_tab");
    }

    private int getAdClipSearchTaskId(int channelid ,int monitorid, long startTime, long endTime){
        return getTaskId(channelid, monitorid, startTime, endTime, "search_adclips_task_tab");
    }

    class RefClip{
        int id;
        String name;
        String path;
        int channel_id;
    }
    private RefClip getRefClipId(String url){
        RefClip res = null;
        String sql = "SELECT * from ref_ad_clip_tab WHERE path = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, url);
            ResultSet rs = statement.executeQuery();
            if (rs.next()){
                res = new RefClip();
                res.channel_id = rs.getInt("channel_id");
                res.name = rs.getString("name");
                res.path = rs.getString("path");
                res.id = rs.getInt("id");
            }
        } catch (SQLException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return res;
    }


    public synchronized int connet2Db(String dburl, String usr, String passwd){
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection(dburl, usr, passwd);
            initDbTab();
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            return -1;
        }
        return 0;
    }

    class ChannelInfo{
        int channel_id;
        String channel_name;
        String channel_path;
        long start_record_time;
        long record_time;
        long cal_feature_time;
        long match_time;
    }

    private ChannelInfo getChnlInfo(int chnl_id){
        ChannelInfo channelInfo = null;
        String sql = "select * from channel_info_tab where channel_id=?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, chnl_id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()){
                channelInfo = new ChannelInfo();
                channelInfo.channel_id = chnl_id;
                channelInfo.channel_name = rs.getString(2);
                channelInfo.channel_path = rs.getString(3);
                channelInfo.start_record_time = rs.getLong(4);
                channelInfo.record_time = rs.getLong(5);
                channelInfo.cal_feature_time = rs.getLong(6);
                channelInfo.match_time = rs.getLong(7);
            }
        } catch (SQLException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            channelInfo = null;
        }
        return channelInfo;
    }

    synchronized public int getSelFindTaskMsg(FindReplayClipTaskMsg msg){
        String sql = "select task_id, channel_id, start_time, end_time where task_state=0";
        int taskid = -1;
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            if (rs.next()){
                taskid = rs.getInt(1);
                int channelid = rs.getInt(2);
                long startTime = rs.getLong(3);
                long endtime = rs.getLong(4);
                ChannelInfo channelInfo = getChnlInfo(channelid);
                msg.endTimeLong = endtime;
                msg.startTimeLong = startTime;
                msg.channelPath = channelInfo.channel_path;
                msg.long2String();
            }
        } catch (SQLException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return taskid;
    }

    synchronized public int getFindReplayAdClipTaskMsg(FindReplayClipTaskMsg msg){
        int taskid = -1;
        String sql = "SELECT task_id, channel_id, start_time, end_time FROM search_adclips_task_tab " +
                "WHERE task_state = 0";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            if (rs.next()){
                taskid = rs.getInt(1);
                int chnlid = rs.getInt(2);
                msg.startTimeLong = rs.getLong(3);
                msg.endTimeLong = rs.getLong(4);
                ChannelInfo channelInfo = getChnlInfo(chnlid);
                msg.channelPath = channelInfo.channel_path;
                msg.long2String();
            }
        } catch (SQLException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            taskid = -1;
        }
        return taskid;
    }

    synchronized public int changeSearchAdClipsTask(int taskid, int state){
        return changeTaskState("search_adclips_task_tab", taskid, state);
    }

    synchronized public int changeSelfSearchReplay(int taskid, int state){
        return changeTaskState("self_channel_search_task_tab", taskid, state);
    }

    private int changeTaskState(String tab, int taskid, int state){
        int res = 0;
        String sql = "update " + tab + " set task_state=? WHERE task_id=?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, state);
            statement.setInt(2, taskid);
            if (statement.executeUpdate() <= 0){
                res = -1;
            }
        } catch (SQLException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            res = -1;
        }
        return res;
    }

    private int initDbTab(){
        String crtChannelSeq = "CREATE SEQUENCE if NOT EXISTS channel_seq_id";
        String crtChannelInfoTab = "create table if not exists channel_info_tab(" +
                "channel_id INTEGER NOT NULL DEFAULT nextval('channel_seq_id') PRIMARY KEY , " +
                "channnel_name varchar(20), " +
                "channel_path varchar(256) UNIQUE ," +
                "start_record_time BIGINT NOT NULL," +
                "record_time BIGINT, " +
                "cal_featrue_time BIGINT, " +
                "match_time BIGINT)";
        String crtRefAdClipSeq = "CREATE SEQUENCE IF NOT EXISTS ref_ad_clip_seq_id";
        String crtRefAdClipTab= "CREATE TABLE IF NOT EXISTS ref_ad_clip_tab(" +
                "id INTEGER NOT NULL  DEFAULT  nextval('crtRefAdClipSeq') PRIMARY KEY ," +
                "name VARCHAR(100) NOT NULL UNIQUE ," +
                "path VARCHAR(256) NOT NULL UNIQUE ," +
                "channel_id BIGINT NOT NULL REFERENCES channel_info_tab(channel_id))";

        String crtSelfChannelTaskSeq = "CREATE SEQUENCE  IF NOT EXISTS self_channel_search_task_seq";
        String crtSelfChannelTask = "CREATE TABLE  IF NOT EXISTS self_channel_search_task_tab(" +
                "task_id INTEGER NOT NULL DEFAULT nextval('self_channel_search_task_seq') PRIMARY KEY , "  +
                "channel_id INTEGER NOT NULL REFERENCES channel_info_tab(channel_id)," +
                "start_time BIGINT NOT NULL, " +
                "end_time BIGINT NOT NULL," +
                "monitor_id INTEGER," +
                "task_state INTEGER DEFAULT 0)";

        //String crtSelfClipsSeq = "CREATE SEQUENCE IF NOT EXISTS self_clips_seq";
        String crtSelfReplayClipsTab = "CREATE TABLE IF NOT EXISTS self_replay_clips_info(" +
                "selfsearch_task_id INTEGER NOT NULL REFERENCES self_channel_search_task_tab(task_id)," +
                "start_time BIGINT NOT NULL," +
                "replay_start_time BIGINT NOT NULL," +
                "replay_frame_nr INTEGER," +
                "monitor_id INTEGER," +
                "checkby VARCHAR(50)," +
                "ischeckok BOOLEAN DEFAULT FALSE )";

        String crtAdClipSearchTaskSeq = "CREATE SEQUENCE IF NOT EXISTS search_adclips_task_seq";
        String crtAdClipSearchTask = "CREATE TABLE IF NOT EXISTS search_adclips_task_tab(" +
                "task_id INTEGER NOT NULL DEFAULT nextval('search_adclips_task_seq') PRIMARY KEY," +
                "channel_id INTEGER NOT NULL REFERENCES channel_info_tab(channel_id)," +
                "start_time BIGINT NOT NULL," +
                "end_time BIGINT NOT NULL," +
                "monitor_id INTEGER," +
                "task_state INTEGER DEFAULT 0)";
        String crtAdReplayClipsTab = "CREATE TABLE IF NOT EXISTS channel_replay_ad_clips(" +
                "search_task_id INTEGER NOT NULL REFERENCES search_adclips_task_tab(task_id)," +
                "ref_ad_clip_id INTEGER NOT NULL REFERENCES ref_ad_clip_tab(id)," +
                "ad_offset INTEGER NOT NULL," +
                "matched_frame_nr INTEGER NOT NULL," +
                "start_match_channel_time BIGINT NOT NULL," +
                "monitor_id INTEGER," +
                "checkby VARCHAR(50)," +
                "ischeckok BOOLEAN DEFAULT FALSE)";

        /*  channelpath search_starttime search_end_time refstartTime startTime frmNr*/
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(crtChannelSeq);
            stmt.executeUpdate(crtChannelInfoTab);
            stmt.executeUpdate(crtRefAdClipSeq);
            stmt.executeUpdate(crtRefAdClipTab);
            stmt.executeUpdate(crtSelfChannelTaskSeq);
            stmt.executeUpdate(crtSelfChannelTask);
            stmt.executeUpdate(crtSelfReplayClipsTab);
            stmt.executeUpdate(crtAdClipSearchTaskSeq);
            stmt.executeUpdate(crtAdClipSearchTask);
            stmt.executeUpdate(crtAdReplayClipsTab);
        } catch (SQLException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            return -1;
        }
        return 0;
    }



    private Connection connection = null;
    public static TestDataBase getIns() {return ins;}
    final static private TestDataBase ins = new TestDataBase();
    private TestDataBase(){
    }

    private Logger logger   = LoggerFactory.getLogger("TestDatabBase");
}
