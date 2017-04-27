package cn.itcast.im.swipe_layout_lib;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * Created by Administrator on 2016/4/11.
 * <p/>
 * 粘性控件集成到消息数量的红点：1.只有用户触摸红点的时候才去显示粘性控件，
 * 2.通过WidowManager去把GooView显示出来
 * 3.红点的textView监听触摸事件的时机去显示粘性控件
 * 4.把红点的textView触摸事件转交给粘性控件
 * <p/>
 * 粘性控件的生命周期存在与触摸的拖拽的时候，拖拽手指抬起的时候就应该移除
 */
public class TouchShowGooViewListener implements View.OnTouchListener, GooView.GooViewCallback {

    private final GooView mGooView;
    private final WindowManager mWindowManager;
    private final WindowManager.LayoutParams params;

    public TouchShowGooViewListener(Context context) {

        mGooView = new GooView(context);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        //窗体是透明的
        params.format = PixelFormat.TRANSLUCENT;
        mGooView.setGooViewCallback(this);
    }

    private View mView;

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        mView = v;

        //侧拉控件不要干扰
        v.getParent().requestDisallowInterceptTouchEvent(true);

        float x = event.getRawX();
        float y = event.getRawY();
        //红点按下触摸的时候去现象粘性控件:
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            //粘性控件显示的时候就把原来的红点消失
            v.setVisibility(View.INVISIBLE);

            String text = ((TextView) v).getText().toString();
            //现象粘性控件的位置
            int pos = (int) v.getTag();
            mGooView.initGooViewPosition(x, y, pos).setText(text);
            mWindowManager.addView(mGooView, params);
        }

        //把红点的textView触摸事件转交给粘性控件
        mGooView.onTouchEvent(event);
        return true;
    }


    //当消失的时候从窗体移除GooView
    @Override
    public void onDispear(int pos) {

        /*
          mWindowManager移除的时候需要判断view有没有父容器
         */
        if (mGooView.getParent() != null) {

            mWindowManager.removeView(mGooView);
        }

    }

    ////当复位的时候的时候从窗体移除GooView
    @Override
    public void onReset() {

        if (mGooView.getParent() != null) {

            mWindowManager.removeView(mGooView);
        }

        //原理的红点从新显示回来
        if (mView != null) {

            mView.setVisibility(View.VISIBLE);
        }

    }
}
