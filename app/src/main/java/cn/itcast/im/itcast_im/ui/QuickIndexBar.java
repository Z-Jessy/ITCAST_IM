package cn.itcast.im.itcast_im.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import cn.itcast.im.commenlib.Cheeses;
import cn.itcast.im.commenlib.CommentUtil;

/**
 * Created by Administrator on 2016/4/8.
 * 将字母绘制到View控件
 * 1.onDraw方法绘制：画笔
 * <p/>
 * mWidth:    控件的宽度
 * mHeight ： 控件的高度
 * mFontWeight： 字母的宽度
 * <p/>
 * cellHeight:每个单元格子的高度：cellHeight = mHeight / 字母的个数
 * <p/>
 * x : 每个字母绘制的距离控件左边的距离：x =  mWidth * 0.5 - mFontWeight * 0.5
 * y:  每个字母绘制的底部距离每个格子顶部距离  y = cellHeight * 0.5 + fontHeight  + i * cellHeight;
 * <p/>
 * y：是绘制的字母屁股距离顶部位置
 * public void drawText(@NonNull String text, float x, float y, @NonNull Paint paint)
 * <p/>
 * //Paint对象的方法，测量字符串的宽度和高度，，传人矩形Rect得到宽高的值
 * public void getTextBounds(char[] text, int index, int count, Rect bounds)
 * <p/>
 * <p/>
 * 触摸的时候：触摸到哪个字母就显示哪个字母
 */
public class QuickIndexBar extends View {


    private Paint mPaint;
    private int mWidth;
    private int mHeight;
    private float mCellHeight;

    public QuickIndexBar(Context context) {
        this(context, null);
    }

    public QuickIndexBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QuickIndexBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(CommentUtil.dpToPx(context, 10));
        mPaint.setColor(Color.GRAY);
        //字体设置为粗体
        mPaint.setTypeface(Typeface.DEFAULT_BOLD);
    }


    //绘制字母
    @Override
    protected void onDraw(Canvas canvas) {


        for (int i = 0; i < Cheeses.LETTERS.length; i++) {

            String letter = Cheeses.LETTERS[i];

            //获取字母的宽度和高度：画笔
            Rect rect = new Rect();
            mPaint.getTextBounds(letter, 0, 1, rect);


            float fontWidth = rect.width();
            float fontHeight = rect.height();

            float x = mWidth * 0.5f - fontWidth * 0.5f;
            float y = mCellHeight * 0.5f + fontHeight * 0.5f + mCellHeight * i;
            canvas.drawText(letter, x, y, mPaint);
        }
    }

    private OnTouchLetterListener mOnTouchLetterListener;

    public void setOnTouchLetterListener(OnTouchLetterListener onTouchLetterListener) {

        this.mOnTouchLetterListener = onTouchLetterListener;
    }

    //触摸字母的时候回调接口，使用者要知道触摸的字母
    public interface OnTouchLetterListener {

        public void onTouchLetter(String letter);
    }


    //触摸计算的字母的索引位置
    private int mTouchLetterIndex = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();

        //触摸的时候请求父控件不要骚扰
        getParent().requestDisallowInterceptTouchEvent(true);

        //获取字母的索引位置：触摸高度  / cellHeight

        float y = event.getY();
        switch (action) {

            case MotionEvent.ACTION_DOWN:

                //按下的透明背景
                setBackgroundColor(0x44000000);

                mTouchLetterIndex = (int) (y / mCellHeight);

                if (0 <= mTouchLetterIndex && mTouchLetterIndex < Cheeses.LETTERS.length) {

                    String letter = Cheeses.LETTERS[mTouchLetterIndex];
                    //CommentUtil.showSingleToast(getContext(), letter);
                    if (mOnTouchLetterListener != null) {

                        mOnTouchLetterListener.onTouchLetter(letter);
                    }
                }


                break;
            case MotionEvent.ACTION_MOVE:

                mTouchLetterIndex = (int) (y / mCellHeight);

                if (0 <= mTouchLetterIndex && mTouchLetterIndex < Cheeses.LETTERS.length) {

                    String letter = Cheeses.LETTERS[mTouchLetterIndex];
                    //CommentUtil.showSingleToast(getContext(), letter);
                    if (mOnTouchLetterListener != null) {

                        mOnTouchLetterListener.onTouchLetter(letter);
                    }
                }

                break;

            case MotionEvent.ACTION_UP:


                setBackgroundColor(Color.TRANSPARENT);
                break;

            default:
                break;
        }


        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        mCellHeight = mHeight * 1.0f / Cheeses.LETTERS.length;
    }
}
