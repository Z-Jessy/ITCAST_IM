package cn.itcast.im.itcast_im.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.jivesoftware.smack.RosterEntry;

import java.util.List;

import cn.itcast.im.commenlib.CommentUtil;
import cn.itcast.im.commenlib.CommonViewHolder;
import cn.itcast.im.itcast_im.R;

/**
 * Created by Administrator on 2016/4/8.
 */
public class ContactAdapter extends BaseAdapter {


    private Context mContext;

    private List<RosterEntry> mRosterEntries;

    public ContactAdapter(Context mContext, List<RosterEntry> mRosterEntries) {

        this.mContext = mContext;
        this.mRosterEntries = mRosterEntries;
    }

    @Override
    public int getCount() {

        return mRosterEntries == null ? 0 : mRosterEntries.size();
    }

    @Override
    public RosterEntry getItem(int position) {

        return mRosterEntries.get(position);
    }

    @Override
    public long getItemId(int position) {

        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        CommonViewHolder commonViewHolder = CommonViewHolder.getCommonViewHolder(convertView, mContext, R.layout.contact_item);

        TextView textViewLetterGroup = commonViewHolder.getView(R.id.tv_letter_group, TextView.class);
        TextView textViewName = commonViewHolder.getView(R.id.tv_name, TextView.class);

        RosterEntry rosterEntry = mRosterEntries.get(position);
        String showBuddyName = CommentUtil.getShowBuddyName(rosterEntry);


        textViewName.setText(showBuddyName);

        //当前
        String currentPinyin = CommentUtil.hanziTextToPinyinFormat(showBuddyName);
        String currentFirstLetter = currentPinyin.charAt(0) + "";

        //位置position == 0 显示分组字符
        if (position == 0) {

            textViewLetterGroup.setVisibility(View.VISIBLE);

            textViewLetterGroup.setText(currentFirstLetter);
        } else {

            //获取当前条目的showName的首字母和上一个条目showName的首字母比较：
            //如果不一样就说明是每个字母分组的第一个条目：要显示，和上一个一样：不显示
            String preShowBuddyName = CommentUtil.getShowBuddyName(mRosterEntries.get(position - 1));
            String pretPinyin = CommentUtil.hanziTextToPinyinFormat(preShowBuddyName);
            String preFirstLetter = pretPinyin.charAt(0) + "";

            if (TextUtils.equals(currentFirstLetter, preFirstLetter)) {

                textViewLetterGroup.setVisibility(View.GONE);
            } else {

                textViewLetterGroup.setVisibility(View.VISIBLE);
                textViewLetterGroup.setText(currentFirstLetter);
            }

        }


        return commonViewHolder.convertView;
    }
}
