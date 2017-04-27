package cn.itcast.im.itcast_im.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * Created by Administrator on 2016/4/8.
 * 侧滑菜单打开的时候，点击主面板的就关闭侧滑菜单：打开菜单的时候把主界面的触摸事件拦截下来，转给关闭侧滑
 * <p/>
 * <p/>
 * <p/>
 * 1.处理决定是否要拦截主界面的所有事件：什么时候拦截：侧滑打开的时候
 * 需要知道侧滑是否打开
 * 2.如果是打开了侧滑：拦截事件：判断当前的事件是一个点击事件：关闭侧滑
 * <p/>
 * <p/>
 * <p/>
 * 需要知道侧滑是否打开
 * 需要给这个类提供关闭侧滑接口功能
 * <p/>
 * 如果一个类想干一些事情就定义接口
 * public interface  Callback {
 * <p/>
 * public boolean LeftMenuIsOpen();
 * <p/>
 * public void closeLeftMenu();
 * }
 */
public class MainPanleLinearLayout extends LinearLayout {

    private GestureDetector mGestureDetector;

    public MainPanleLinearLayout(Context context) {
        this(context, null);
    }

    public MainPanleLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainPanleLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mGestureDetector = new GestureDetector(context, mSimpleOnGestureListener);
    }


    private GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {

        //这个方法被调用了点击了
        @Override
        public boolean onSingleTapUp(MotionEvent e) {

            if (mCallback != null) {

                mCallback.closeLeftMenu();
            }
            return super.onSingleTapUp(e);
        }
    };

    public interface Callback {

        public boolean LeftMenuIsOpen();

        public void closeLeftMenu();
    }

    private Callback mCallback;

    public void setCallback(Callback callback) {


        this.mCallback = callback;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        //当侧滑菜单打开的时候去拦截事件
        if (mCallback != null && mCallback.LeftMenuIsOpen()) {

            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {


        //拦截了事件处理点击关闭侧滑菜单:判断当前的事件的是一个点击事件
        mGestureDetector.onTouchEvent(event);


        return true;
    }
}
