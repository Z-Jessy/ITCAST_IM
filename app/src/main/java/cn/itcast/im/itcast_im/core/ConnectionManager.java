package cn.itcast.im.itcast_im.core;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.itcast.im.commenlib.CommentUtil;
import cn.itcast.im.itcast_im.bean.Msg;
import cn.itcast.im.itcast_im.util.DataBaseUtil;

/**
 * Created by Administrator on 2016/4/7.
 * <p/>
 * 这个类负责和xmpp服务器连接，和服务器交互的代码找这个类，这个类使用asmack登录的
 * <p/>
 * 1.连接的方法
 * 2.登录
 * 3.获取联系人
 * 4.发送消息
 * 5.接受消息
 * <p/>
 * 通过XmppConnection---和asmack 建立沟通
 */
public class ConnectionManager implements PacketFilter, PacketListener {

    private static final String TAG = "ConnectionManager";
    private String HOSt = DEF_HOST;

    private static final java.lang.String DEF_HOST = "127.0.0.1";
    //xmpp默认端口:5222
    private static final int PORT = 5222;
    private XMPPConnection mXmppConnection;

    private ConnectionManager() {
    }

    private static ConnectionManager sInstance = new ConnectionManager();

    public static ConnectionManager getInstance() {

        return sInstance;
    }

    //1.连接的方法,这个在开启app的时候只调用一次
    public void connect(String ip) throws XMPPException {

        if (!TextUtils.isEmpty(ip)) {

            HOSt = ip;
        }

        ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration(HOSt, PORT);
        //安全模式关闭，实际开发开启
        connectionConfiguration.setSASLAuthenticationEnabled(false);
        mXmppConnection = new XMPPConnection(connectionConfiguration);
        mXmppConnection.connect();
    }

    public void login(Context context, String username, String password) throws XMPPException {

        //这个方法是以异常的机子来告诉调用者帐号或密码错误
        mXmppConnection.login(username, password);
        //登录成功后初始化数据库
        if (mXmppConnection.isAuthenticated()) {

            DataBaseUtil.init(context, generaDbNameByUserJid());

            /*'
            添加监听器监听来自好友的消息
            Connection对象的方法

     */
            mXmppConnection.addPacketListener(this, this);

        }

    }

    private String generaDbNameByUserJid() {

        //@---.#
       return CommentUtil.hanziTextToPinyinFormat(mXmppConnection.getUser());
    }

    //判段是否连接
    public boolean isConnected() {


        return mXmppConnection != null && mXmppConnection.isConnected();
    }

    //判断是否登录
    public boolean isLogin() {


        return mXmppConnection.isAuthenticated();
    }

    //获取自己的jid,xmpp的完整帐号
    public String getMyAccountJid() {


        return mXmppConnection.getUser();
    }

    //登出
    public void disconnect() {

        mXmppConnection.disconnect();
    }

    /*

        获取Roster对象去：获取联系人列表数据
     */
    public Roster getRoster() {


        return mXmppConnection.getRoster();
    }


    //获取ChatManager:通过ChatManager去创建聊天发送消息及接受消息
    public ChatManager getChatManager() {


        return mXmppConnection.getChatManager();
    }

    /*
        观察者

     */
    public interface MsgObserver {

        public void notify(Msg msg);
    }


    private List<MsgObserver> mMsgObservers = new ArrayList<MsgObserver>();
    /*

        添加观察者
     */
    public void addMsgObserver(MsgObserver msgObserver) {

        this.mMsgObservers.add(msgObserver);
    }

    /*
     移除观察者
     */
    public void removeMsgObservers(MsgObserver msgObserver) {

        this.mMsgObservers.remove(msgObserver);
    }

    /*

        jid---->MsgObserver
     */
    private Map<String, MsgObserver> mChatByJidMsgObservers  = new HashMap<String, MsgObserver>();


    public void addChatMsgObserver(String userJid, MsgObserver msgObserver)  {


        mChatByJidMsgObservers.put(userJid, msgObserver);
    }

    public void removeChatMsgObserver(String userJid) {

        mChatByJidMsgObservers.remove(userJid);
    }


    /*
          过滤器过滤消息：好友出席的消息Presence, Message, .....
          这里只需要Message对象数据
     */
    @Override
    public boolean accept(Packet packet) {

        return packet instanceof Message;
    }

    /*

        接受消息：

        当接受到来自好友的消息的时候：1.通知界面 2.保存消息到数据库

        解决方案：观察者模式， add, remove

        注意这个方法是在子线程被调用的：
     */
    @Override
    public void processPacket(Packet packet) {

        Message message = (Message) packet;
        // xiaoze@itcast/Smack
        String messageFrom = message.getFrom();
        String comeMsgJid = getComeMsgJidByMessageFrom(messageFrom);

        Log.i(TAG, message.toXML());

        Msg msg = Msg.createMsg(Msg.MSG_TYPE_RECEIVE, comeMsgJid, message.getBody());

        //保存消息到数据库：保存处理未读的消失并统计
        DataBaseUtil.saveComeMsg(msg);


        /*
            通知所有的观察者
         */
        for (MsgObserver msgObserver : mMsgObservers) {


            msgObserver.notify(msg);
        }


        /*

            通知聊天界面:ChatActivity
         */

        MsgObserver msgObserver = mChatByJidMsgObservers.get(comeMsgJid);
        if (msgObserver!=null) {

            msgObserver.notify(msg);
        }

    }

    private String getComeMsgJidByMessageFrom(String messageFrom) {

        int end = messageFrom.lastIndexOf("/");
        // left<=  < end
        return messageFrom.substring(0, end);
    }
}
