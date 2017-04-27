package cn.itcast.im.itcast_im.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import cn.itcast.im.commenlib.CommonViewHolder;
import cn.itcast.im.itcast_im.R;
import cn.itcast.im.itcast_im.bean.Msg;

/**
 * Created by Administrator on 2016/4/10.
 */
public class ChatAdapter extends BaseAdapter {

    private Context mContext;
    private List<Msg> mMsges;

    public ChatAdapter(Context mContext, List<Msg> mMsges) {
        this.mContext = mContext;
        this.mMsges = mMsges;
    }

    @Override
    public int getCount() {

        return mMsges == null ? 0 : mMsges.size();
    }

    @Override
    public Msg getItem(int position) {


        return mMsges.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {


        return mMsges.get(position).msgType;
    }

    @Override
    public int getViewTypeCount() {


        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        int type = getItemViewType(position);
        CommonViewHolder viewHolder = null;
        switch (type) {
            case Msg.MSG_TYPE_SEND:
                viewHolder = CommonViewHolder.getCommonViewHolder(convertView, mContext, R.layout.item_send_layout);

                break;
            case Msg.MSG_TYPE_RECEIVE:

                viewHolder = CommonViewHolder.getCommonViewHolder(convertView, mContext, R.layout.item_receive_layout);
                break;
            default:
                break;
        }

        TextView viewTime = viewHolder.getView(R.id.tv_msg_time, TextView.class);
        TextView viewContent = viewHolder.getView(R.id.tv_msg_content, TextView.class);

        Msg msg = mMsges.get(position);

        viewTime.setText(msg.msgTime);
        viewContent.setText(msg.msgContent);


        return viewHolder.convertView;
    }
}
