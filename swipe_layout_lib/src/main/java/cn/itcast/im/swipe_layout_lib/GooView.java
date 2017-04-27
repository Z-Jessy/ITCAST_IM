package cn.itcast.im.swipe_layout_lib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import cn.itcast.im.commenlib.CommentUtil;
import cn.itcast.im.commenlib.GeometryUtil;

/**
 * Created by Administrator on 2016/4/11.
 * <p/>
 * 需求分析：
 * 1。拖拽超出最大拖拽范围的时候：1.连接曲线断开 ，2.固定圆消息
 * 2. 手指释放：
 * 释放的位置在最大范围之外：1.全部消息
 * 释放的位置在最大范围之内：1.一次拖拽动作离开过最大范围：直接弹出，2.没有离开过最大范围：动画的拉回
 */
public class GooView extends View {


    private Paint mPaint;
    private int mStatusBarHeight;
    private boolean mOverMaxDragRange;
    private boolean mDispear;
    private Paint mTextPaint;

    public GooView(Context context) {
        this(context, null);
    }

    public GooView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GooView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);

        //画线空心效果，默认是实心填充效果，会沿着起点到终点进行填充封闭封闭
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setStrokeWidth(3);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mStatusBarHeight = CommentUtil.getStatusBarHeight(this);
    }

    /*
          两个圆的圆心
         */
    private PointF mStickCircleCenter = new PointF(100, 100);
    private PointF mDragCircleCenter = new PointF(100, 100);

    /*
      圆的半径
     */
    private float mStickCircleRadius = 10f;
    private float mDragCircleRadius = 10f;

    //private PointF mCurveControlPoint = new PointF(150, 75);

    /*

            提供外部触摸位置的x , y相对于屏幕的(0, 0)位置
     */
    private int mInListViewPositon;

    public GooView initGooViewPosition(float x, float y, int mInListViewPositon) {

        mStickCircleCenter.set(x, y);
        mDragCircleCenter.set(x, y);
        this.mInListViewPositon = mInListViewPositon;
        return this;
    }


    /*

        外部传人文本显示
     */
    private String mText;

    public void setText(String text) {

        mText = text;
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(CommentUtil.dpToPx(getContext(), 10));
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(Color.WHITE);
    }


    @Override
    protected void onDraw(Canvas canvas) {

        //如果拖拽释放的位置在最大的范围之外就不画了
        if (mDispear) {

            return;
        }

        //移动画布
        canvas.translate(0, -mStatusBarHeight);


//        //画出控制点
//        canvas.drawPoint(mCurveControlPoint.x, mCurveControlPoint.y, mPaint);
//
//        //画线连接圆
//        Path path = new Path();
//        path.moveTo(mStickCircleCenter.x, mStickCircleCenter.y);
//        //path.lineTo(mDragCircleCenter.x, mDragCircleCenter.y);
//        //param1:x , param2:y是曲线的控制点：曲线切线的交点
//        path.quadTo(mCurveControlPoint.x, mCurveControlPoint.y, mDragCircleCenter.x, mDragCircleCenter.y);
//        canvas.drawPath(path, mPaint);
//
//        //画切线
//        canvas.drawLine(mStickCircleCenter.x, mStickCircleCenter.y, mCurveControlPoint.x, mCurveControlPoint.y,mPaint);
//        canvas.drawLine(mDragCircleCenter.x, mDragCircleCenter.y, mCurveControlPoint.x, mCurveControlPoint.y,mPaint);

        float changeStickRadiusByDrag = getChangeStickRadiusByDrag();

        if (!mOverMaxDragRange) {

            ////画两个圆的连接曲线
            drawLineCurveWith2Circle(canvas, changeStickRadiusByDrag);
            //画圆
            canvas.drawCircle(mStickCircleCenter.x, mStickCircleCenter.y, /*mStickCircleRadius*/changeStickRadiusByDrag, mPaint);
        }


        canvas.drawCircle(mDragCircleCenter.x, mDragCircleCenter.y, mDragCircleRadius, mPaint);


        //绘制文本
        if (!TextUtils.isEmpty(mText)) {

            Rect rect = new Rect();
//            mTextPaint.getTextBounds(mText, 0, mText.length(), rect);
//            float fontWidht = rect.width();
//            float fontHeight = rect.height();
//            float x = mDragCircleCenter.x - fontWidht * 0.5f;
//            float y = mDragCircleCenter.y + fontHeight * 0.5f;

            float x = mDragCircleCenter.x;
            float y = mDragCircleCenter.y + mDragCircleRadius * 0.5f;
            canvas.drawText(mText, x, y, mTextPaint);
        }


        canvas.save();
        canvas.restore();

    }

    //画两个圆的连接曲线
    private void drawLineCurveWith2Circle(Canvas canvas, float changeStickRadiusByDrag) {

        Double linK = computeLinK();
        //计算固定圆的,半径用变化的半径
        PointF[] intersectionStickCirclePoints = GeometryUtil.getIntersectionPoints(mStickCircleCenter, /*mStickCircleRadius*/changeStickRadiusByDrag, linK);
        //计算拖拽圆的
        PointF[] intersectionDragCirclePoints = GeometryUtil.getIntersectionPoints(mDragCircleCenter, mDragCircleRadius, linK);
        //计算两个圆的圆心连线的中点为曲线的控制点
        PointF middlePointBy2Circle = GeometryUtil.getMiddlePoint(mStickCircleCenter, mDragCircleCenter);
        //画曲线
        //起点是：固定圆的A点----->到固定圆的A'点
        Path path = new Path();
        path.moveTo(intersectionStickCirclePoints[0].x, intersectionStickCirclePoints[0].y);
        path.quadTo(middlePointBy2Circle.x, middlePointBy2Circle.y, intersectionDragCirclePoints[0].x, intersectionDragCirclePoints[0].y);
        path.lineTo(intersectionDragCirclePoints[1].x, intersectionDragCirclePoints[1].y);
        path.quadTo(middlePointBy2Circle.x, middlePointBy2Circle.y, intersectionStickCirclePoints[1].x, intersectionStickCirclePoints[1].y);

        canvas.drawPath(path, mPaint);
    }

    ////计算斜率Linke :过圆心与两个圆的连线垂直的直线
    private Double computeLinK() {

        float detalX = mStickCircleCenter.x - mDragCircleCenter.x;
        float detalY = mStickCircleCenter.y - mDragCircleCenter.y;

        Double lineK = null;
        if (detalY != 0) {

            lineK = Double.valueOf(detalX / detalY);
        }

        return lineK;
    }

    /*
        当按下、拖拽的时候改变的拖拽圆的位置

     */
    private void updateDragCirclePosition(float x, float y) {

        mDragCircleCenter.set(x, y);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    //最大的拖拽范围的值：
    private float MAX_DRAG_RANGE = 80f;

    /*

        拖拽的时候随着拖拽的范围变大；固定圆的半径变小:

        拖拽大小：       0 ----->MAX_DRAG_RANGE =======> 0----->1

        mStickRadius :  mStickRadius ------> mStickRadius * 0.33
     */
    private float getChangeStickRadiusByDrag() {

        //拖拽时候两个圆的距离 distanceBetween2Circle : 0------->MAX_DRAG_RANGE
        float distanceBetween2Circle = GeometryUtil.getDistanceBetween2Points(mStickCircleCenter, mDragCircleCenter);
        distanceBetween2Circle = Math.min(distanceBetween2Circle, MAX_DRAG_RANGE);
        float percent = distanceBetween2Circle / MAX_DRAG_RANGE;
        float changeStickRadius = GeometryUtil.evaluateValue(percent, mStickCircleRadius, mStickCircleRadius * 0.33f);
        return changeStickRadius;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getRawX();
        float y = event.getRawY();

        int action = event.getAction();

        switch (action) {

            case MotionEvent.ACTION_DOWN:
                updateDragCirclePosition(x, y);
                mOverMaxDragRange = false;
                mDispear = false;
                break;
            case MotionEvent.ACTION_MOVE:
                updateDragCirclePosition(x, y);
                //拖拽超出最大范围断开连接线
                float distanceBetween2Circle = GeometryUtil.getDistanceBetween2Points(mStickCircleCenter, mDragCircleCenter);
                if (distanceBetween2Circle > MAX_DRAG_RANGE) {

                    mOverMaxDragRange = true;
                }

                break;
            case MotionEvent.ACTION_UP:

                processTabUp();
                break;
            default:
                break;
        }


        return true;
    }

    private GooViewCallback mGooViewCallback;

    public void setGooViewCallback(GooViewCallback mGooViewCallback) {

        this.mGooViewCallback = mGooViewCallback;
    }

    /*

        接口回调，通知外部：1.消失通知，2.复位，退回到原来的位置
     */
    public interface GooViewCallback {

        //1.消失通知
        public void onDispear(int pos);

        //复位，退回到原来的位置
        public void onReset();
    }


    /*
        当松手释放的时候处理逻辑

          释放的位置在最大范围之外：1.全部消失
 *        释放的位置在最大范围之内：1.一次拖拽动作离开过最大范围：直接弹回，2.没有离开过最大范围：动画的拉回
 *
     */


    private void processTabUp() {

        //判断当前释放的时候两个圆的距离
        float distanceBetween2Circle = GeometryUtil.getDistanceBetween2Points(mStickCircleCenter, mDragCircleCenter);
        //释放的位置在最大范围之外：1.全部消失
        if (distanceBetween2Circle > MAX_DRAG_RANGE) {


            mDispear = true;
            ViewCompat.postInvalidateOnAnimation(this);

            //消失通知
            if (mGooViewCallback != null) {

                mGooViewCallback.onDispear(mInListViewPositon);
            }

            //释放位置在最大范围之内
        } else {
            //1.一次拖拽动作离开过最大范围：直接弹回
            if (mOverMaxDragRange) {

                //拖拽圆直拉回到固定圆的位置
                updateDragCirclePosition(mStickCircleCenter.x, mStickCircleCenter.y);
                //复位通知
                if (mGooViewCallback != null) {

                    mGooViewCallback.onReset();
                }


                //2.没有离开过最大范围：动画的拉回
            } else {

                //通过动画不断的改变拖拽圆的位置： 从释放的位置---->固定圆的位置
                //0----->1
                //releaseDragCirclePoint ----->mStickCircleCenter
                // 几下释放的位置
                final PointF releaseDragCirclePoint = new PointF(mDragCircleCenter.x, mDragCircleCenter.y);

                ValueAnimator animator = ObjectAnimator.ofFloat(1.0f);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    //启动动画onAnimationUpdate方法不断的执行：fraction值的变化：0------->1
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {

                        float fraction = animation.getAnimatedFraction();
                        //让拖拽圆的圆心的坐标从releaseDragCirclePoint ----->mStickCircleCenter
                        PointF pointByPercent = GeometryUtil.getPointByPercent(releaseDragCirclePoint, mStickCircleCenter, fraction);
                        updateDragCirclePosition(pointByPercent.x, pointByPercent.y);
                    }
                });
                animator.addListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {

                        //复位通知
                        if (mGooViewCallback != null) {

                            mGooViewCallback.onReset();
                        }
                    }
                });

                animator.setDuration(500);
                animator.setInterpolator(new OvershootInterpolator(3f));
                animator.start();
            }
        }

    }
}
