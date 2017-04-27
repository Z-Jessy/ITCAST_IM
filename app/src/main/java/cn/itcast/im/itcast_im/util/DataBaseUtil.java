package cn.itcast.im.itcast_im.util;

import android.content.Context;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.DbModelSelector;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.db.table.DbModel;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;
import java.util.List;

import cn.itcast.im.itcast_im.bean.Msg;

/**
 * Created by Administrator on 2016/4/10.
 * <p/>
 * 将Msg对象保存到数据库
 * 获取Msg对象集合
 */
public class DataBaseUtil {

    private static DbUtils sDbUtils;

    //初始化数据库，制定数据库名字：根据帐号变化
    public static void init(Context context, String dbName) {

        sDbUtils = DbUtils.create(context, dbName);
    }

    //保存消息对象
    public static void saveMsg(Msg msg) {

        try {
            sDbUtils.save(msg);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /*

        获取聊天的历史记录根据好友的jid
        select * from msg_table where mgsWithJid = mgsWithJid
        Selector.from(Msg.class).where("msgWithJid", "=", msgWithJid);
     */
    public static List<Msg> getMsg(String msgWithJid) {

        Selector selector = Selector.from(Msg.class).where("msgWithJid", "=", msgWithJid);
        try {
            return sDbUtils.findAll(selector);
        } catch (DbException e) {
            e.printStackTrace();
        }

        return null;
    }

    //select * from msg_table group by msgWithJid order by msgTime desc
    /*
        获取会话的列表数据对象
        DbModel--->将数据库查询到每一条目数据以map键值对的形式封装到DbModel对象


     */
    public static List<Msg> getSession() {

        DbModelSelector dbModelSelector = Selector.from(Msg.class).groupBy("msgWithJid")
                .orderBy("msgTime", true)
                .select(new String[]{"msgType", "msgWithJid", "msgContent", "msgNewCount"});
        try {

            List<DbModel> dbModelAll = sDbUtils.findDbModelAll(dbModelSelector);
            //每个DbModel对象一个会话记录也就是一个Msg对象
            if (dbModelAll == null) return null;

            List<Msg> msges = new ArrayList<>();

            for (DbModel model : dbModelAll) {

                int msgType = model.getInt("msgType");
                String msgWithJid = model.getString("msgWithJid");
                String msgContent = model.getString("msgContent");
                int msgNewCount = model.getInt("msgNewCount");
                Msg msg = Msg.createMsg(msgType, msgWithJid, msgContent);
                msg.msgNewCount = msgNewCount;
                msges.add(msg);
            }

            return msges;


        } catch (DbException e) {
            e.printStackTrace();
        }

        return null;

    }

    /*
       delete from msg_table where msg_jid = userJid
     */
    public static void deleteSessionByWithJid(String userJid) {

        WhereBuilder whereBuilder = WhereBuilder.b("msgWithJid", "=", userJid);
        try {
            sDbUtils.delete(Msg.class, whereBuilder);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }


    /*
        不考虑已读的情况
        好友第一次发消息过来消息  Msg对象保存的时候：Msg : msgNewCount = 1
        好友第二次发消息过来消息  Msg对象保存的时候：Msg ：msgNewCount = last : msgNewCount + 1

        。。。。。
        好友第n次发消息过来消息   Msg对象保存的时候：Msg ：msgNewCount = lastMsg : msgNewCount + 1

       1: Msg : msgNewCount = 1
       2: Msg : msgNewCount = 2
       3: Msg : msgNewCount = 3

       n: Msg :  msgNewCount = n ====》n - 1: Msg :msgNewCount + 1



     */
    public static void saveComeMsg(Msg msg) {

        Msg recentMsg = getRecentMsgByWithJid(msg.getUserJid());

        //该好友第一一次发消息过来
        if (recentMsg == null) {

            msg.msgNewCount = 1;

            //该好友上一次发过消息
        } else {

            msg.msgNewCount = recentMsg.msgNewCount + 1;

        }

        saveMsg(msg);
    }

    /*

        当点击进入聊天界面代表读取消息：清除未读消息记录
     */
    public static void clearUnReadNewMsgCount(String msgWithJid) {

        Msg msg = new Msg();
        msg.msgNewCount = 0;
        WhereBuilder whereBuilder = WhereBuilder.b("msgWithJid", "=", msgWithJid);
        try {
            sDbUtils.update(msg, whereBuilder, new String[]{"msgNewCount"});
        } catch (DbException e) {
            e.printStackTrace();
        }
    }



    /*

        根据jid获取最近的消息
     */
    private static Msg getRecentMsgByWithJid(String userJid) {

        Selector selector = Selector.from(Msg.class).where("msgWithJid", "=", userJid).orderBy("msgTime", true);
        try {

            return sDbUtils.findFirst(selector);

        } catch (DbException e) {
            e.printStackTrace();
        }
        return null;
    }
}
