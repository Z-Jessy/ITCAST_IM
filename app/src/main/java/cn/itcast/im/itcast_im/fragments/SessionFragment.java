package cn.itcast.im.itcast_im.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lidroid.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

import cn.itcast.im.commenlib.CommentUtil;
import cn.itcast.im.itcast_im.R;
import cn.itcast.im.itcast_im.act.ChatActivity;
import cn.itcast.im.itcast_im.adapter.SessionAdapter;
import cn.itcast.im.itcast_im.bean.Msg;
import cn.itcast.im.itcast_im.core.ConnectionManager;
import cn.itcast.im.itcast_im.util.DataBaseUtil;
import cn.itcast.im.swipe_layout_lib.SwipeAdapter;
import cn.itcast.im.swipe_layout_lib.SwipeData;

/**
 * A simple {@link Fragment} subclass.
 */
public class SessionFragment extends BaseFragment implements ConnectionManager.MsgObserver, SwipeAdapter.Callback {

    @ViewInject(R.id.listview)
    private ListView mListView;
    private SwipeAdapter mSessionAdapter;

    private List<Msg> mMsges = new ArrayList<>();


    @Override
    public View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragment_session, null);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ConnectionManager.getInstance().addMsgObserver(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        //获取会话数据,显示
        setAdapter();
    }

    private void setAdapter() {

        List<Msg> session = DataBaseUtil.getSession();
        if (session == null) return;

        mMsges.clear();
        mMsges.addAll(session);

        if (mSessionAdapter == null) {

            mSessionAdapter = new SwipeAdapter(mContext, mMsges);
            mListView.setAdapter(mSessionAdapter);
            //mListView.setOnItemClickListener(this);
            mSessionAdapter.setCallback(this);
        } else {

            mSessionAdapter.notifyDataSetChanged();
        }


    }

    //返回打开的SwipeLayout的数量
    @Override
    public int getOpenedSwipeLayout() {

        if (mSessionAdapter!=null) {

            return mSessionAdapter.getOpenedSwipeCount();
        }

        return 0;
    }

    //提供实现关闭所有大口的侧拉删除的功能
    public void closeAllSwipeLayout() {

        if (mSessionAdapter!=null) {

            mSessionAdapter.closeAllSwipeLayout();
        }

    }


    /*


     */
    @Override
    public void notify(Msg msg) {


        CommentUtil.runOnUIThread(new Runnable() {
            @Override
            public void run() {

                setAdapter();

                CommentUtil.showSingleToast(mContext, "notify msg");

            }
        });

    }

    @Override
    public void onItemClickListener(SwipeData swipeData) {

        Intent intent = new Intent(mContext, ChatActivity.class);
        intent.putExtra(CommentUtil.USER_JID_KEY, swipeData.getUserJid());
        mContext.startActivity(intent);
    }

    //真正的删除数据，从数据库删除数据
    @Override
    public void onClickDelete(SwipeData swipeData) {

        DataBaseUtil.deleteSessionByWithJid(swipeData.getUserJid());
    }

    @Override
    public void onUnReadNewMsgCount(SwipeData swipeData) {

        DataBaseUtil.clearUnReadNewMsgCount(swipeData.getUserJid());
    }
}
