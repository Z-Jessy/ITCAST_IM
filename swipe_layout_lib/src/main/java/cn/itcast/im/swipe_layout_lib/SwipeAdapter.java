package cn.itcast.im.swipe_layout_lib;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.itcast.im.commenlib.CommonViewHolder;

/**
 * Created by Administrator on 2016/4/10.
 * 粘性控件集成到消息数量的红点：1.只有用户触摸红点的时候才去显示粘性控件，
 *                             2.通过WidowManager去把GooView显示出来
 *                             3.红点的textView监听触摸事件的时机去显示粘性控件
 *                             4.把红点的textView触摸事件转交给粘性控件
 *
 *
 */
public class SwipeAdapter<T extends SwipeData> extends BaseAdapter implements SwipeLayout.OnSwipeDragStatusChangeListener {

    private Set<SwipeLayout> mOpenedSwipeLayouts = new HashSet<>();

    private Context mContext;

    private List<T> mDatas;


    /*
            提供接口回调：告诉使用者：1.点击了条目，2.点击了删除

     */
    private Callback mCallback;
    private final TouchShowGooViewListener mTouchShowGooViewListener;

    public void setCallback(Callback callback) {

        this.mCallback = callback;
    }

    public interface Callback {

        public void onItemClickListener(SwipeData swipeData);

        public void onClickDelete(SwipeData swipeData);

        //拉动红点让红点消失的时候去通知使用者
        public void onUnReadNewMsgCount(SwipeData swipeData);
    }


    public SwipeAdapter(Context mContext, List<T> datas) {
        this.mContext = mContext;
        this.mDatas = datas;
        mTouchShowGooViewListener = new TouchShowGooViewListener(mContext) {

            @Override
            public void onDispear(int pos) {
                super.onDispear(pos);
                if (mCallback!=null) {

                    mCallback.onUnReadNewMsgCount(mDatas.get(pos));
                }
            }
        };
    }

    @Override
    public int getCount() {

        return mDatas == null ? 0 : mDatas.size();
    }

    @Override
    public T getItem(int position) {

        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {

        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        CommonViewHolder viewHolder = CommonViewHolder.getCommonViewHolder(convertView, mContext, R.layout.item_swipe_layout);
        SwipeLayout swipeLayout = viewHolder.getView(R.id.sll, SwipeLayout.class);
        RelativeLayout relativeLayoutFront = viewHolder.getView(R.id.rl_swip_front, RelativeLayout.class);
        TextView textViewName = viewHolder.getView(R.id.tv_name, TextView.class);
        TextView textViewContent = viewHolder.getView(R.id.tv_content, TextView.class);
        TextView textViewNewMsgCount = viewHolder.getView(R.id.tv_count, TextView.class);
        TextView textViewDelete = viewHolder.getView(R.id.tv_delete, TextView.class);

        //关闭
        swipeLayout.close();

        T t = mDatas.get(position);

        //数据绑定
        textViewName.setText(t.getUserJid());
        textViewContent.setText(t.getConten());

        int msgNewCount = t.getNewMsgCount();
        if (msgNewCount==0) {

            textViewNewMsgCount.setVisibility(View.INVISIBLE);
        } else {

            textViewNewMsgCount.setVisibility(View.VISIBLE);
            textViewNewMsgCount.setTag(position);
            textViewNewMsgCount.setText(t.getNewMsgCount()+"");
            //每个红点注册触摸显示粘性控件的监听
            textViewNewMsgCount.setOnTouchListener(mTouchShowGooViewListener);
        }



        //打开关闭的监听及打开的数量统计
        swipeLayout.setOnSwipeDragStatusChangeListener(this);


        //监听每个条目的点击事件：点击条目的时候跳转到聊天界面
        relativeLayoutFront.setTag(position);
        relativeLayoutFront.setOnClickListener(mItemOnClickListener);

        //点击删除的监听
        textViewDelete.setTag(position);
        textViewDelete.setOnClickListener(mItemDeleteClickListener);


        return viewHolder.convertView;
    }

    /*
    递归的找父控件：目的找SwipeLayout
     */
    private View getParentView(View view) {

        View parent = (View) view.getParent();
        if (parent instanceof SwipeLayout) {

            return parent;
        } else {

            return getParentView(parent);
        }
    }


    /*
    监听每个条目的点击事件：点击条目的时候跳转到聊天界面
     */

    private View.OnClickListener mItemOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            Integer position = (Integer) v.getTag();
            //CommentUtil.showSingleToast(v.getContext(), "onClick");
            if (mCallback != null) {

                mCallback.onItemClickListener(mDatas.get(position));
            }
        }
    };

    /*

    //点击删除的监听:1.通知实现的接口；2.删除条目：3.当点击删除的时候要把存放到集合mOpenedSwipeLayouts的当前的SwipeLayout 删除
     */
    private View.OnClickListener mItemDeleteClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            //1.通知实现的接口
            Integer position = (Integer) v.getTag();
            //CommentUtil.showSingleToast(v.getContext(), "onClick");
            if (mCallback != null) {

                mCallback.onClickDelete(mDatas.get(position));
            }
            //2.删除条目
            mDatas.remove(/*position*/mDatas.get(position));
            notifyDataSetChanged();

            //3.SwipeLayout 从集合mOpenedSwipeLayouts删除
            SwipeLayout swipeLayout = (SwipeLayout) getParentView(v);
            mOpenedSwipeLayouts.remove(swipeLayout);

        }
    };




    /*

        监听每个条目的点击事件：点击条目的时候跳转到聊天界面
    */

    /*





        提供方法获取打开的侧拉菜单数量
     */

    public int getOpenedSwipeCount() {


        return mOpenedSwipeLayouts.size();
    }

    public void closeAllSwipeLayout() {

        for (SwipeLayout swipLayout : mOpenedSwipeLayouts) {

            swipLayout.close();
        }
    }


    @Override
    public void onClose(SwipeLayout swipeLayout) {

        mOpenedSwipeLayouts.remove(swipeLayout);
    }

    @Override
    public void onOpen(SwipeLayout swipeLayout) {

        mOpenedSwipeLayouts.add(swipeLayout);
    }
}
