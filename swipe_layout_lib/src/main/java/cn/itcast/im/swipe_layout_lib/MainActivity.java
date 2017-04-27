package cn.itcast.im.swipe_layout_lib;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import cn.itcast.im.commenlib.Cheeses;
import cn.itcast.im.commenlib.CommentUtil;

public class MainActivity extends Activity implements SwipeAdapter.Callback {

    private SwipeAdapter<Data> dataSwipeAdapter;

    List<Data> datas = new ArrayList<Data>();
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*GooView gooView = new GooView(this);
        gooView.setGooViewCallback(new GooView.GooViewCallback() {
            @Override
            public void onDispear() {

                CommentUtil.showSingleToast(getApplicationContext(), "onDispear");
            }

            @Override
            public void onReset() {

                CommentUtil.showSingleToast(getApplicationContext(), "onReset");
            }
        });
        setContentView(gooView);*/

        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.lv);


        for (int i = 0; i < Cheeses.NAMES.length; i++) {

            datas.add(new Data(i));

        }

        dataSwipeAdapter = new SwipeAdapter<>(this, datas);

        mListView.setAdapter(dataSwipeAdapter);

        dataSwipeAdapter.setCallback(this);

        /*SwipeLayout swipeLayout = (SwipeLayout) findViewById(R.id.sll);
        swipeLayout.setOnSwipeDragStatusChangeListener(this);*/
    }

    public void getOpenedCount(View view) {

        int openedSwipeCount = dataSwipeAdapter.getOpenedSwipeCount();
        CommentUtil.showSingleToast(this, openedSwipeCount + "");
    }

    public void closeAll(View view) {

        dataSwipeAdapter.closeAllSwipeLayout();

    }

    @Override
    public void onItemClickListener(SwipeData swipeData) {

        CommentUtil.showSingleToast(this, swipeData.getUserJid());

    }

    @Override
    public void onClickDelete(SwipeData swipeData) {


//        datas.remove(swipeData);
        // dataSwipeAdapter.notifyDataSetChanged();
        CommentUtil.showSingleToast(this, "Delete;" + swipeData.getUserJid());
    }

    @Override
    public void onUnReadNewMsgCount(SwipeData swipeData) {

    }

    /*@Override
    public void onClose(SwipeLayout swipeLayout) {

        CommentUtil.showSingleToast(this, "onClose");
    }

    @Override
    public void onOpen(SwipeLayout swipeLayout) {

        CommentUtil.showSingleToast(this, "onOpen");
    }*/
}
