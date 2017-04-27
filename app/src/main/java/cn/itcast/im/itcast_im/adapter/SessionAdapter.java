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
public class SessionAdapter extends BaseAdapter {

    private Context mContext;
    private List<Msg> mMsges;

    public SessionAdapter(Context mContext, List<Msg> mMsges) {
        this.mContext = mContext;
        this.mMsges = mMsges;
    }

    public void update(List<Msg> mMsges) {

        this.mMsges = mMsges;
        notifyDataSetChanged();
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
    public View getView(int position, View convertView, ViewGroup parent) {

        int type = getItemViewType(position);
        CommonViewHolder viewHolder = null;

        viewHolder = CommonViewHolder.getCommonViewHolder(convertView, mContext, R.layout.item_session);

        TextView viewName = viewHolder.getView(R.id.account, TextView.class);
        TextView viewContent = viewHolder.getView(R.id.content, TextView.class);

        Msg msg = mMsges.get(position);

        viewName.setText(msg.msgWithJid);
        viewContent.setText(msg.msgContent);


        return viewHolder.convertView;
    }
}
