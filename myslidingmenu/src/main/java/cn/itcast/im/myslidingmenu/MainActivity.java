package cn.itcast.im.myslidingmenu;

import android.app.Activity;
import android.os.Bundle;

import cn.itcast.im.commenlib.CommentUtil;

public class MainActivity extends Activity {

    private MyDrawerLayout mMyDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMyDrawerLayout = (MyDrawerLayout) findViewById(R.id.mdl);
        mMyDrawerLayout.setOnStatusChangeListener(new MyDrawerLayout.OnStatusChangeListener() {

            @Override
            public void onOpen() {

                CommentUtil.showSingleToast(getApplicationContext(), "onOpen");
            }

            @Override
            public void onClose() {

                CommentUtil.showSingleToast(getApplicationContext(), "onClose");
            }

            @Override
            public void onDraging(float percent) {

                CommentUtil.showSingleToast(getApplicationContext(), "onDraging");
            }
        });
    }
}
