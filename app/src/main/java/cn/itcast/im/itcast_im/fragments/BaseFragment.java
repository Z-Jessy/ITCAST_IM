package cn.itcast.im.itcast_im.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lidroid.xutils.ViewUtils;

/**
 * A simple {@link Fragment} subclass.
 *
 * 父类：1.把子类相同的属性在这里做了， 2.父类做不了的事情，制定规则标准让子类去实现
 *
 *
 */
public abstract class BaseFragment extends Fragment {

    protected Context mContext;
    private View mRootView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    public BaseFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {

        if (mRootView==null) {

            mRootView = createView(inflater, container, savedInstanceState);
            //从param2：view去findViewByid 注入到param1对象标注了ViewInject注解的成员赋值
            ViewUtils.inject(this, mRootView);
        }


        return mRootView;
    }


    public abstract View createView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState);

    public int getOpenedSwipeLayout() {

        return 0;
    }

    public void closeAllSwipeLayout() {}


}
