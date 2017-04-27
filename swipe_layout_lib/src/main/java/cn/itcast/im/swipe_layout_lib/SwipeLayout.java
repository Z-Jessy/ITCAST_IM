package cn.itcast.im.swipe_layout_lib;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by Administrator on 2016/4/10.
 */
public class SwipeLayout extends FrameLayout {


    private ViewDragHelper mViewDragHelper;
    private View mFrontView;
    private View mMenuView;
    private int mWidth;
    private int mHeight;
    private int mDragRange;
    private GestureDetector mGestureDetector;

    public SwipeLayout(Context context) {
        this(context, null);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mViewDragHelper = ViewDragHelper.create(this, mViewDragHelperCallback);
        mGestureDetector = new GestureDetector(context, mSimpleOnGestureListener);
    }


    private GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {


        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            //水平滑动侧拉删除
            if (Math.abs(distanceX) > Math.abs(distanceY)) {

                requestDisallowInterceptTouchEvent(true);
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    };

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        try {
            //这样会导致Listview不能上下滑动
            //requestDisallowInterceptTouchEvent(true);
            mGestureDetector.onTouchEvent(event);
            mViewDragHelper.processTouchEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private ViewDragHelper.Callback mViewDragHelperCallback = new ViewDragHelper.Callback() {


        @Override
        public boolean tryCaptureView(View child, int pointerId) {

            return true;
        }

        /*

            修正限制拖拽范围
         */
        public int clampViewPositionHorizontal(View child, int left, int dx) {

            if (child == mFrontView) {

                left = fixFrontDragLeft(left);

            } else if (child == mMenuView) {

                left = fixMenuDragLeft(left);
            }

            return left;
        }

        //如果不去设置就会和子控件的触摸冲突
        @Override
        public int getViewHorizontalDragRange(View child) {


            return mDragRange;
        }

        /*
                  拖拽FrontView--->拉动Menu  ,拖拽menuView--->拉动FrontView
                 */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {

            if (changedView == mMenuView) {

                mFrontView.offsetLeftAndRight(dx);
            } else if (changedView == mFrontView) {

                mMenuView.offsetLeftAndRight(dx);
                //拖拽Front强制从绘界面mMenuView.offsetLeftAndRight(dx);
                ViewCompat.postInvalidateOnAnimation(SwipeLayout.this);
            }

            //处理拖拽状态回调
            dispatchSwipeDragStatus();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {

            int left = mFrontView.getLeft();
            if (xvel == 0 && left < -mDragRange * 0.5f) {

                open();
            } else if (xvel < 0) {

                open();
            } else {

                close();
            }
        }
    };


    /*

        拖拽状态回调
     */

    public enum SwipeDragStatus {

        Close, Open, Draging;
    }


    public interface OnSwipeDragStatusChangeListener {

        public void onClose(SwipeLayout swipeLayout);

        public void onOpen(SwipeLayout swipeLayout);
    }

    private OnSwipeDragStatusChangeListener mOnSwipeDragStatusChangeListener;

    public void setOnSwipeDragStatusChangeListener(OnSwipeDragStatusChangeListener onSwipeDragStatusChangeListener) {

        this.mOnSwipeDragStatusChangeListener = onSwipeDragStatusChangeListener;
    }

    private SwipeDragStatus mSwipeDragStatus = SwipeDragStatus.Close;

    private SwipeDragStatus updateSwipeDragStatus() {

        int left = mFrontView.getLeft();

        if (left == 0) {

            return SwipeDragStatus.Close;
        } else if (left == -mDragRange) {

            return SwipeDragStatus.Open;
        } else {

            return SwipeDragStatus.Draging;
        }
    }

    public SwipeDragStatus getCurrentSwipeDragStatus() {

        return mSwipeDragStatus;
    }

    private void dispatchSwipeDragStatus() {

        SwipeDragStatus preSwipeDragStatus = mSwipeDragStatus;
        mSwipeDragStatus = updateSwipeDragStatus();

        if (mOnSwipeDragStatusChangeListener == null) return;

        if (preSwipeDragStatus == mSwipeDragStatus) return;

        switch (mSwipeDragStatus) {

            case Close:
                mOnSwipeDragStatusChangeListener.onClose(this);
                break;
            case Open:
                mOnSwipeDragStatusChangeListener.onOpen(this);
                break;
            default:
                break;
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        if (mViewDragHelper.continueSettling(true)) {

            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void close() {

        if (mViewDragHelper.smoothSlideViewTo(mFrontView, 0, 0)) {

            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void open() {

        if (mViewDragHelper.smoothSlideViewTo(mFrontView, -mDragRange, 0)) {

            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private int fixMenuDragLeft(int left) {

        if (left < mWidth - mDragRange) {

            return mWidth - mDragRange;
        }

        return left;
    }

    private int fixFrontDragLeft(int left) {

        if (left < -mDragRange) {

            return -mDragRange;
        }

        if (left > 0) {

            return 0;
        }

        return left;
    }

    /*
    摆放位置 菜单摆放到右边
     */

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mMenuView.layout(mWidth, 0, mWidth + mDragRange, mHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        mDragRange = mMenuView.getMeasuredWidth();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mFrontView = getChildAt(0);
        mMenuView = getChildAt(1);
    }
}
