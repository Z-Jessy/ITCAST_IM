package cn.itcast.im.itcast_im.act;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.jivesoftware.smack.XMPPException;

import cn.itcast.im.commenlib.CommentUtil;
import cn.itcast.im.itcast_im.R;
import cn.itcast.im.itcast_im.core.ConnectionManager;

/*

连接
 */

public class Splash extends Activity {

    private EditText mEditText;
    private ConnectionManager mConnectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mEditText = (EditText) findViewById(R.id.et_ip);

        mConnectionManager = ConnectionManager.getInstance();


        connect(null);


    }

    //连接的方法
    public void connect(final String ip) {


        CommentUtil.runOnThread(new Runnable() {
            @Override
            public void run() {

                try {


                    if (!mConnectionManager.isConnected()) {

                        //耗时间
                        mConnectionManager.connect(ip);

                    }

                    //判断是否登录
                    if (mConnectionManager.isLogin()) {

                        //直接到主界面
                        Intent intent = new Intent(Splash.this, MainActivity.class);
                        startActivity(intent);
                    } else {

                        //到登录界面
                        //跳转到登录界面
                        Intent intent = new Intent(Splash.this, LoginActivity.class);
                        startActivity(intent);

                    }

                    finish();


                    //CommentUtil.showSafeToast(getApplicationContext(), "连接成功");


                } catch (XMPPException e) {
                    e.printStackTrace();
                    CommentUtil.showSafeToast(getApplicationContext(), e.getMessage());
                }
            }
        });
    }

    public void reConnect(View view) {

        String ip = mEditText.getText().toString().trim();
        connect(ip);
    }


}
