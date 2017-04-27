package cn.itcast.im.itcast_im.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Administrator on 2016/4/8.
 */
public class NoScrollViewPager extends ViewPager {

    public NoScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoScrollViewPager(Context context) {
        this(context, null);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {


        return false;
    }


}
