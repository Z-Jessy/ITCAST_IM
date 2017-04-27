package cn.itcast.im.itcast_im.bean;

import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;

import cn.itcast.im.commenlib.CommentUtil;
import cn.itcast.im.swipe_layout_lib.SwipeData;

/**
 * Created by Administrator on 2016/4/10.
 *
 * Msg对象保存到数据库:DBUitls处理，以对象为表名字
 */
//数据库的表的名字
@Table(name = "msg_table")
public class Msg extends SwipeData {

    public static final int MSG_TYPE_SEND = 0;
    public static final int MSG_TYPE_RECEIVE = 1;

    //给数据库保存消息对象使用
    //制定id
    @Id(column = "_id")
    public int id;

    public int msgType;

    public String msgWithJid;

    public String msgContent;

    public String msgTime;

    public int msgNewCount;

    public Msg() {
    }

    public Msg(int msgType, String msgWithJid, String msgContent) {
        this.msgType = msgType;
        this.msgWithJid = msgWithJid;
        this.msgContent = msgContent;
        this.msgTime = CommentUtil.getCurrentTime();
    }

    public static Msg createMsg(int msgType, String msgWithJid, String msgContent) {

        return new Msg(msgType, msgWithJid, msgContent);
    }

    @Override
    public String getUserJid() {

        return msgWithJid;
    }

    @Override
    public String getConten() {

        return msgContent;
    }

    @Override
    public int getNewMsgCount() {

        return msgNewCount;
    }
}
