package cn.itcast.im.itcast_im.act;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import org.jivesoftware.smack.XMPPException;

import cn.itcast.im.itcast_im.R;
import cn.itcast.im.itcast_im.core.ConnectionManager;

public class LoginActivity extends Activity {

    @ViewInject(R.id.account)
    private EditText mAccount;
    @ViewInject(R.id.pwd)
    private EditText mPassword;
    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ViewUtils.inject(this);

        connectionManager = ConnectionManager.getInstance();
        mAccount.setText("xiaokong");
    }


    //点击登录
    public void login(View view) {

        String username = mAccount.getText().toString().trim();
        String pwd = mPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(pwd)) {

            Toast.makeText(this, "帐号或密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        //调用登录API
        try {


            connectionManager.login(this, username, pwd);
            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();

            //跳转到主界面
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();


        } catch (XMPPException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }
}
