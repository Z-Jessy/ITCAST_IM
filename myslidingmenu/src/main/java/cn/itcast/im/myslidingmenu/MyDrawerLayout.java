package cn.itcast.im.myslidingmenu;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import cn.itcast.im.commenlib.CommentUtil;

/**
 * Created by Administrator on 2016/4/7.
 * <p/>
 * 提供侧滑功能的控件：两个子控件：menu, mainPanle
 * <p/>
 * 侧滑控件的需求:
 * 1.限制拖拽的范围：菜单界面不能拖拽，主面板范围：0---->width * 0.6
 * 在哪个方法里面限制
 * 2.触摸拖拽菜单的滑动距离dx作用在主面板
 * 3.打开关闭的菜单： 打开: 松手的位置在位置 速度为 0 && > mDragRange  * 0.5 || 速度>0
 * 4.打开关闭流畅效果：动画
 * 5.伴随动画：关闭到打开的动画
 * 1.主面板的缩放动画：1---->0.8
 * 2.菜单缩放：  0。5---> 1
 * 3.菜单位置移动：-mWidth---->0
 * 4.渐变ALpha : 0---->1
 * 5.背景：从黑色---->透明
 * 6.拖拽状态的更新，回调
 */
public class MyDrawerLayout extends FrameLayout {


    private static final String TAG = "MyDrawerLayout";
    private View mMeueView;
    private View mMainPanle;
    private ViewDragHelper mViewDragHelper;
    private int mWidth;
    private int mHeight;
    private int mDragRange;

    public MyDrawerLayout(Context context) {
        this(context, null);
    }

    public MyDrawerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyDrawerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //使用ViewDragHelper
        mViewDragHelper = ViewDragHelper.create(this, mViewDragHelperCallback);
        //背景
        setBackgroundResource(R.drawable.bg);
    }

    private ViewDragHelper.Callback mViewDragHelperCallback = new ViewDragHelper.Callback() {
        //mViewDragHelper.shouldInterceptTouchEvent(ev)
        //当ViewDragHelper在判断当前的拖拽事件的时候如果有子控件也是可以滑动的时候，
        //根据这个的返回值进行判断，是否要拦截事件
        public int getViewHorizontalDragRange(View child) {


            return mDragRange;
        }


        @Override
        public boolean tryCaptureView(View child, int pointerId) {


            /*if (mMeueView == child) {

                return false;
            }

            if (mMainPanle == child) {


                return true;
            }*/
            return true;
        }

        //返回值修正限制拖拽的子控件的水平方向的移动距离
        //left  = child.getLeft + dx
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {


            Log.i(TAG, "child.getLeft()=" + child.getLeft());
            Log.i(TAG, "left=" + left + ", dx=" + dx);
            //限制拖拽范围：0----->mDragRange
            if (child == mMainPanle) {

                left = fixDragLeftRange(left);
            }

            return left;
        }

        //当子控件的位置发送改变的时候ViewDragHelper调用,告诉使用者拖拽的子控件已经移动了
        //当tryCaptureView对拖拽的控件限制的时候这里触摸的控件不会改变位置
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {

            //菜单拖拽的时候，把拖拽菜单dx作用在主面板
            if (changedView == mMeueView) {

                //不能让菜单移动，
                mMeueView.layout(0, 0, mWidth, mHeight);
                //可以移动主面板但是范围没有限制了
                //mMainPanle.offsetLeftAndRight(dx);
                int mainLeft = mMainPanle.getLeft() + dx;
                mainLeft = fixDragLeftRange(mainLeft);
                mMainPanle.layout(mainLeft, 0, mainLeft + mWidth, mHeight);
                Log.i(TAG, "菜单被拖拽：dx=" + dx);
            }

            //left: 0----->mDragRange
            int l = mMainPanle.getLeft();

            //percent: 0----->1
            float percent = l * 1.0f / mDragRange;

            //处理打开关闭的伴随动画
            dispatchAnimation(percent);

            //处理更新拖拽
            disatchStatus(percent);

        }

        //拖拽控件松手的时候被调用，xvel水平的加速度>0向右的加速度
        public void onViewReleased(View releasedChild, float xvel, float yvel) {

            //打开
            int left = mMainPanle.getLeft();
            //加速度为0,以松手时候位置为准
            if (xvel == 0 && left > mDragRange * 0.5) {

                open();
                //加速度向右
            } else if (xvel > 0) {

                open();
            } else {

                close();
            }

        }
    };

    //由使用者实现监听
    private OnStatusChangeListener mStatusChangeListener;

    public void setOnStatusChangeListener(OnStatusChangeListener statusChangeListener) {

        this.mStatusChangeListener = statusChangeListener;
    }


    //定义接口打开，关闭，拖拽的时候告诉使用者,监听
    public interface OnStatusChangeListener {

        public void onOpen();

        public void onClose();

        public void onDraging(float percent);

    }

    //当前的状态
    private Status mStatus = Status.Close;

    //外部需要知道当前的状态
    public Status getStatus() {

        return mStatus;
    }

    //调用状态更新，回调接口
    private void disatchStatus(float percent) {

        Status preStatus = mStatus;
        mStatus = updateStatus(percent);


        if (mStatusChangeListener == null) return;

        //回调使用者实现的接口
        mStatusChangeListener.onDraging(percent);

        //从一种状态到另外一种状态的时候调用
        if (preStatus == mStatus) return;

        switch (mStatus) {

            case Open:
                mStatusChangeListener.onOpen();
                break;
            case Close:
                mStatusChangeListener.onClose();
                super.requestLayout();
                break;

            default:
                break;
        }

    }

    //处理拖拽状态定义
    public enum Status {

        Close, Open, Draging;
    }

    //状态更新
    public Status updateStatus(float percent) {

        if (percent == 0) {

            return Status.Close;
        } else if (percent == 1) {


            return Status.Open;
        } else {


            return Status.Draging;
        }


    }


    //处理伴随动画
    /*

      1.主面板的缩放动画：1---->0.8
 *    2.菜单缩放：  0。5---> 1
 *    3.菜单位置移动：-mWidth---->0
 *    4.渐变ALpha : 0---->1
 *    5.背景：从黑色---->透明
     */
    private void dispatchAnimation(float percent) {

        //mainScaleValue : 1----->0.8
        //1.主面板的缩放动画：1---->0.8
        float mainScaleValue = CommentUtil.evaluateFloat(percent, 1, 0.8);
        ViewCompat.setScaleX(mMainPanle, mainScaleValue);
        ViewCompat.setScaleY(mMainPanle, mainScaleValue);

        //2.菜单缩放：  0.5---> 1
        float menuScaleValue = CommentUtil.evaluateFloat(percent, 0.5f, 1.0f);
        ViewCompat.setScaleX(mMeueView, menuScaleValue);
        ViewCompat.setScaleY(mMeueView, menuScaleValue);

        //3.菜单位置移动：-mWidth * 0.5f---->0
        float menuTranslationValue = CommentUtil.evaluateFloat(percent, -mWidth * 0.5f, 0);
        ViewCompat.setTranslationX(mMeueView, menuTranslationValue);

        //4.菜单渐变ALpha : 0---->1
        float menuAlphaValue = CommentUtil.evaluateFloat(percent, 0, 1);
        ViewCompat.setAlpha(mMeueView, menuAlphaValue);

        //5.背景：从黑色---->透明
        //Color.BLACK  ---->Color.TRANSPARENT
        Integer colorValue = (Integer) CommentUtil.evaluateColor(percent, Color.BLACK, Color.TRANSPARENT);
        //图像合成，覆盖PorterDuff.Mode.SRC_OVER
        getBackground().setColorFilter(colorValue, PorterDuff.Mode.SRC_OVER);

    }

    //按照文档要求在这里去不断的调用
    @Override
    public void computeScroll() {
        super.computeScroll();

        if (mViewDragHelper.continueSettling(true)) {
            //这个方法触发 computeScroll()调用
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void close() {

        //mMainPanle.layout(0, 0, mWidth, mHeight);
        if (mViewDragHelper.smoothSlideViewTo(mMainPanle, 0, 0)) {

            ViewCompat.postInvalidateOnAnimation(this);
        }

    }

    public void open() {

        //从新摆放位置
        //mMainPanle.layout(mDragRange, 0, mDragRange + mWidth, mHeight);
        //true if animation should continue through {@link #continueSettling(boolean)} calls
        //滑动的动画效果：这个方法如果返回true需要继续调用continueSettling去移动下一帧
        if (mViewDragHelper.smoothSlideViewTo(mMainPanle, mDragRange, 0)) {
            //如果这个方法返回true需要继续调用
            //while(mViewDragHelper.continueSettling(false));
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    //拖拽范围： 0 --->mDragRange
    private int fixDragLeftRange(int left) {

        if (left < 0) {

            return 0;
        }

        if (left > mDragRange) {


            return mDragRange;
        }

        return left;
    }

    private Callback mCallback;

    public void setCallback(Callback callback) {

        this.mCallback = callback;
    }

    public interface Callback {

        public boolean canOpenMenu();
    }

    /*
        要拦截事件的时候判断是否有侧拉删除是打开的，如果有打开就不拦截
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (mStatus == Status.Close) {

            //关闭状态的时候才去问外部是否可以打开侧滑菜单
            //不拦截事件交给子控件处理
            if (mCallback != null && !mCallback.canOpenMenu()) {

                return false;
            }
        }

        //把事件的是否拦截的控制权利交给ViewDragHelper
        //mViewDragHelper会判断事件是否为拖拽事件
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //把拖拽事件交给ViewDragHelper去处理
        try {
            mViewDragHelper.processTouchEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    //在onMeasure方法调用完成之后调用这个onSizeChanged方法
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        //计算拖拽的最大范围
        mDragRange = (int) (mWidth * 0.6f);
    }

    //获取子控件并校验
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        int childCount = getChildCount();

        if (childCount != 2) {

            throw new IllegalArgumentException("this Layout must has two child");
        }


        mMeueView = getChildAt(0);
        mMainPanle = getChildAt(1);

    }

    @Override
    public void requestLayout() {

        if (mStatus == Status.Close) {

            super.requestLayout();
        }
    }
}
