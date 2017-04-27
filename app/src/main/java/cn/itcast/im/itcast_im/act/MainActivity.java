package cn.itcast.im.itcast_im.act;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.util.ArrayList;

import cn.itcast.im.commenlib.Cheeses;
import cn.itcast.im.itcast_im.R;
import cn.itcast.im.itcast_im.core.ConnectionManager;
import cn.itcast.im.itcast_im.fragments.BaseFragment;
import cn.itcast.im.itcast_im.fragments.ContactFragment;
import cn.itcast.im.itcast_im.fragments.SessionFragment;
import cn.itcast.im.itcast_im.ui.MainPanleLinearLayout;
import cn.itcast.im.itcast_im.ui.NoScrollViewPager;
import cn.itcast.im.myslidingmenu.MyDrawerLayout;

public class MainActivity extends FragmentActivity implements RadioGroup.OnCheckedChangeListener {

    @ViewInject(R.id.main_panle_layout)
    private MainPanleLinearLayout mMainPanleLinearLayout;

    @ViewInject(R.id.mdl)
    private MyDrawerLayout mMyDrawerLayout;
    @ViewInject(R.id.tv_account)
    private TextView mLeftAccountTextView;
    @ViewInject(R.id.lv)
    private ListView mLeftListVew;

    @ViewInject(R.id.iv_main_head)
    private ImageView mImagViewMainHead;
    @ViewInject(R.id.tv_main_title)
    private TextView mMainTitleTextView;

    @ViewInject(R.id.viewpager)
    private NoScrollViewPager mNoScrollViewPager;
    @ViewInject(R.id.rg)
    private RadioGroup mRadioGroup;

    public static final String[] TITLES = {"会话", "联系人"};


    private ConnectionManager mConnectionManager;
    private ArrayAdapter<String> mArrayAdapter;
    private ArrayList<BaseFragment> mFragments;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewUtils.inject(this);

        mConnectionManager = ConnectionManager.getInstance();

        initLeftMenu();

        initMainPanle();


        //侧滑菜单打开的时候，点击主面板的就关闭侧滑菜单：打开菜单的时候把主界面的触摸事件拦截下来，转给关闭侧滑
        mMainPanleLinearLayout.setCallback(new MainPanleLinearLayout.Callback() {

            @Override
            public boolean LeftMenuIsOpen() {

                return mMyDrawerLayout.getStatus() == MyDrawerLayout.Status.Open;
            }

            @Override
            public void closeLeftMenu() {

                mMyDrawerLayout.close();
            }
        });
    }

    /*
        左边菜单的逻辑处理
     */
    private void initLeftMenu() {
        //帐号显示
        mLeftAccountTextView.setText(mConnectionManager.getMyAccountJid());
        //ListView功能条目显示
        mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, Cheeses.QQ_FUCTIONS) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(Color.WHITE);

                return view;
            }
        };
        mLeftListVew.setAdapter(mArrayAdapter);
        //点击登出，跳转到登录界面:1.点击事件,2.跳转到登录
        mLeftListVew.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (Cheeses.QQ_FUCTIONS.length - 1 == position) {

                    //登录：实际是断开了连接
                    mConnectionManager.disconnect();
                    //Caused by: java.lang.IllegalStateException: Already logged in to server.
                    Intent intent = new Intent(MainActivity.this, Splash.class);
                    startActivity(intent);
                    finish();
                }

            }
        });


    }


    /*

        主界面的逻辑处理
     */
    private void initMainPanle() {


        //1.处理主界面标题的头像的动画:注册监听处理
        mMyDrawerLayout.setOnStatusChangeListener(new MyDrawerLayout.OnStatusChangeListener() {
            @Override
            public void onOpen() {

            }

            @Override
            public void onClose() {

                ObjectAnimator animator = ObjectAnimator.ofFloat(mImagViewMainHead, "translationX", 5f);
                //循环的移动
                animator.setInterpolator(new CycleInterpolator(5f));
                animator.setDuration(500);
                animator.start();
            }

            @Override
            public void onDraging(float percent) {

                ViewCompat.setAlpha(mImagViewMainHead, 1 - percent);
            }
        });

        //注册MyDrawerLayout是否拦截的监听
        mMyDrawerLayout.setCallback(new MyDrawerLayout.Callback() {

            @Override
            public boolean canOpenMenu() {

                return !(mFragments.get(0).getOpenedSwipeLayout() > 0);
            }
        });

        //初始化Fragment
        initFragments();

        //Fragment填充到ViewPager
        mNoScrollViewPager.setAdapter(mPagerAdapter);

        //点击RadioButton切换Fragment
        mRadioGroup.setOnCheckedChangeListener(this);

        //默认选中会话
        mRadioGroup.check(R.id.rb_session);


    }

    private void initFragments() {

        mFragments = new ArrayList<>();
        mFragments.add(new SessionFragment());
        mFragments.add(new ContactFragment());
    }


    private FragmentPagerAdapter mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {


        @Override
        public int getCount() {


            return mFragments.size();
        }

        @Override
        public Fragment getItem(int position) {

            return mFragments.get(position);
        }
    };

    //点击切换Fragment
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

        int index = group.indexOfChild(group.findViewById(checkedId));
        mNoScrollViewPager.setCurrentItem(index);

        //改变标题
        mMainTitleTextView.setText(TITLES[index]);

        //当点击切换到联系人的时候关闭所有打开的侧拉删除
        if (index == 1) {

            int openedSwipeLayout = mFragments.get(0).getOpenedSwipeLayout();
            if (openedSwipeLayout > 0) {

                mFragments.get(0).closeAllSwipeLayout();
            }
        }


    }
}
