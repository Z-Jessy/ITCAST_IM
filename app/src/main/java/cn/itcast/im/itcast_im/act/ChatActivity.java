package cn.itcast.im.itcast_im.act;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.XMPPException;

import java.util.ArrayList;
import java.util.List;

import cn.itcast.im.commenlib.CommentUtil;
import cn.itcast.im.itcast_im.R;
import cn.itcast.im.itcast_im.adapter.ChatAdapter;
import cn.itcast.im.itcast_im.bean.Msg;
import cn.itcast.im.itcast_im.core.ConnectionManager;
import cn.itcast.im.itcast_im.util.DataBaseUtil;

public class ChatActivity extends Activity implements /*MessageListener*/ConnectionManager.MsgObserver {

    @ViewInject(R.id.title)
    private TextView mTitleTextView;
    @ViewInject(R.id.listview)
    private ListView mListView;
    @ViewInject(R.id.input)
    private EditText mEditTextInput;
    private String mBuddyUserJid;
    private ChatManager mChatManager;
    private Chat mChat;

    //给Adaptet使用
    private List<Msg> mMsges = new ArrayList<Msg>();
    private ChatAdapter mChatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ViewUtils.inject(this);

        //取好友的jid
        mBuddyUserJid = getIntent().getStringExtra(CommentUtil.USER_JID_KEY);

        //设置标题
        String titleStr = getResources().getString(R.string.chat_title_str);
        titleStr = String.format(titleStr, mBuddyUserJid);
        mTitleTextView.setText(titleStr);

        //获取ChatManager创建聊天的对象
        mChatManager = ConnectionManager.getInstance().getChatManager();
        //创建聊天
        //param1:和当前好友聊天好友的jid,param2:MessageListener接受来自param1好友的消息
        mChat = mChatManager.createChat(mBuddyUserJid, null);


        //获取历史聊天记录
        List<Msg> historyMsges = DataBaseUtil.getMsg(mBuddyUserJid);
        if (historyMsges != null && historyMsges.size() > 0) {

            mMsges.addAll(historyMsges);
            setAdapter();
        }

        //注册自己实现的消息的观察者
        ConnectionManager.getInstance().addChatMsgObserver(mBuddyUserJid, this);

        //进入到聊天界面清除未读消息数量
        DataBaseUtil.clearUnReadNewMsgCount(mBuddyUserJid);

    }

    //退出聊天的时候要移除被其他对象引用
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mChat.removeMessageListener(this);
        ConnectionManager.getInstance().removeChatMsgObserver(mBuddyUserJid);
    }

    //点击发送消息
    public void send(View view) {

        String content = mEditTextInput.getText().toString().trim();

        if (TextUtils.isEmpty(content)) {

            return;
        }

        mEditTextInput.setText("");

        try {
            //发送消息
            mChat.sendMessage(content);
        } catch (XMPPException e) {
            e.printStackTrace();
        }

        //构造Msg对象
        Msg msg = Msg.createMsg(Msg.MSG_TYPE_SEND, mBuddyUserJid, content);
        mMsges.add(msg);
        setAdapter();

        //保存消息到数据库
        DataBaseUtil.saveMsg(msg);
    }

    //XmPP消息的回调接口：当有好友发消息的时候被调用:注意这个方法在子线程被调用
   /* @Override
    public void processMessage(Chat chat, Message message) {

        CommentUtil.showSafeToast(this, message.toXML());

        //构造Msg对象
        Msg msg = Msg.createMsg(Msg.MSG_TYPE_RECEIVE, mBuddyUserJid, message.getBody());
        mMsges.add(msg);
        CommentUtil.runOnUIThread(new Runnable() {
            @Override
            public void run() {

                setAdapter();
            }
        });

        //保存消息到数据库
        DataBaseUtil.saveMsg(msg);

    }*/

    private void setAdapter() {

        if (mChatAdapter == null) {

            mChatAdapter = new ChatAdapter(this, mMsges);
            mListView.setAdapter(mChatAdapter);
        } else {

            mChatAdapter.notifyDataSetChanged();
        }
        //选中最近的消息条目
        mListView.setSelection(mMsges.size());
    }

    @Override
    public void notify(Msg msg) {

        //受到消息的时候去清除未读消息记录
        DataBaseUtil.clearUnReadNewMsgCount(mBuddyUserJid);

        mMsges.add(msg);
        CommentUtil.runOnUIThread(new Runnable() {
            @Override
            public void run() {

                setAdapter();
            }
        });
    }
}
