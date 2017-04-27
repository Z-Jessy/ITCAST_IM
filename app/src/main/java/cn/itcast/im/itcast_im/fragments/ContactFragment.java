package cn.itcast.im.itcast_im.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.lidroid.xutils.view.annotation.ViewInject;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.itcast.im.commenlib.CommentUtil;
import cn.itcast.im.itcast_im.R;
import cn.itcast.im.itcast_im.act.ChatActivity;
import cn.itcast.im.itcast_im.adapter.ContactAdapter;
import cn.itcast.im.itcast_im.core.ConnectionManager;
import cn.itcast.im.itcast_im.ui.QuickIndexBar;

/**
 * A simple {@link Fragment} subclass.
 * <p/>
 * 1.获取联系人显示到界面
 * 2.联系人按照拼音排序
 * 3.滑动索引条的根据当前滑动的字母去ListView找position :分组的首字母的位置position
 */
public class ContactFragment extends BaseFragment implements RosterListener, AdapterView.OnItemClickListener {

    private static final String TAG = "ContactFragment";
    @ViewInject(R.id.listview)
    private ListView mListView;
    @ViewInject(R.id.qib)
    private QuickIndexBar mQuickIndexBar;
    @ViewInject(R.id.tv_show_letter)
    private TextView mTextViewShowLetter;
    private Roster mRoster;

    //定义一个集合存放联系人数据
    private List<RosterEntry> mRosterEntries = new ArrayList<RosterEntry>();
    private ContactAdapter mContactAdapter;


    @Override
    public View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragment_contact, null);
    }

    //消失隐藏mTextViewShowLetter的任务
    private Runnable mHideTextViewShowLetterTask = new Runnable() {

        @Override
        public void run() {

            mTextViewShowLetter.setVisibility(View.INVISIBLE);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRoster = ConnectionManager.getInstance().getRoster();
        mRoster.addRosterListener(this);
    }

    //排序的比较器
    private Comparator<RosterEntry> mComparator = new Comparator<RosterEntry>() {

        @Override
        public int compare(RosterEntry lhs, RosterEntry rhs) {

            //按照showName的拼音排序
            String lhShowBuddyName = CommentUtil.getShowBuddyName(lhs);
            String rhShowBuddyName = CommentUtil.getShowBuddyName(rhs);
            //ShowBuddyName转拼音
            String lhPinyin = CommentUtil.hanziTextToPinyinFormat(lhShowBuddyName);
            String rhPinyin = CommentUtil.hanziTextToPinyinFormat(rhShowBuddyName);


            return lhPinyin.compareTo(rhPinyin);
        }
    };

    private void setAdapter() {

        //获取联系人数据
        Collection<RosterEntry> entries = mRoster.getEntries();

        mRosterEntries.clear();
        mRosterEntries.addAll(entries);

        //排序
        Collections.sort(mRosterEntries, mComparator);

        if (mContactAdapter == null) {

            mContactAdapter = new ContactAdapter(mContext, mRosterEntries);
            mListView.setAdapter(mContactAdapter);
            mListView.setOnItemClickListener(this);
        } else {

            mContactAdapter.notifyDataSetChanged();
        }


    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //触摸显示字母
        //5秒杀消失
        mQuickIndexBar.setOnTouchLetterListener(new QuickIndexBar.OnTouchLetterListener() {

            @Override
            public void onTouchLetter(String letter) {

                mTextViewShowLetter.setVisibility(View.VISIBLE);
                mTextViewShowLetter.setText(letter);
                //移除上一次的任务
                CommentUtil.getMainHandler().removeCallbacks(mHideTextViewShowLetterTask);
                CommentUtil.getMainHandler().postDelayed(mHideTextViewShowLetterTask, 5000);

                //滑动索引条的根据当前滑动的字母去ListView找position :分组的首字母的位置position
                selectListViewItemByTouchLetter(letter);
            }
        });


        setAdapter();
    }

    private void selectListViewItemByTouchLetter(String letter) {

        int count = mContactAdapter.getCount();

        for (int i = 0; i < count; i++) {

            String showBuddyName = CommentUtil.getShowBuddyName(mContactAdapter.getItem(i));
            String l = CommentUtil.hanziTextToPinyinFormat(showBuddyName).charAt(0) + "";
            if (TextUtils.equals(letter, l)) {

                mListView.setSelection(i);
                break;
            }
        }

    }

    //添加好友调用：参数是多个好友的jid集合
    @Override
    public void entriesAdded(Collection<String> collection) {

        Log.i(TAG, "entriesAdded");
        //重写获取新的联系人数据

        CommentUtil.runOnUIThread(new Runnable() {
            @Override
            public void run() {

                setAdapter();
            }
        });

    }

    //删除好友及修改昵称的时候调用
    @Override
    public void entriesUpdated(Collection<String> collection) {

        Log.i(TAG, "entriesUpdated");
        CommentUtil.runOnUIThread(new Runnable() {
            @Override
            public void run() {

                setAdapter();
            }
        });

    }

    //删除好友的时候调用
    @Override
    public void entriesDeleted(Collection<String> collection) {

        Log.i(TAG, "entriesDeleted");
        CommentUtil.runOnUIThread(new Runnable() {
            @Override
            public void run() {

                setAdapter();
            }
        });

    }

    /*
    xmpp : Presence出席：好友的状态：上线、下线


     好友下线的时候
    I/ContactFragment( 9112): presenceChanged
I/ContactFragment( 9112): presence=<presence id="8Neh0-11" to="xiaokong@itcast.c
n" from="zrt@itcast.cn/Smack" type="unavailable"></presence>
     */
    @Override
    public void presenceChanged(Presence presence) {

        Log.i(TAG, "presenceChanged");
        if (presence != null) {

            Log.i(TAG, "presence=" + presence.toXML());
        }
    }

    //点击跳转到聊天界面
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent intent = new Intent(mContext, ChatActivity.class);
        intent.putExtra(CommentUtil.USER_JID_KEY, mRosterEntries.get(position).getUser());
        mContext.startActivity(intent);
    }
}
