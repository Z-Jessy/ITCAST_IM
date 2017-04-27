package cn.itcast.im.swipe_layout_lib;

import cn.itcast.im.commenlib.Cheeses;

/**
 * Created by Administrator on 2016/4/10.
 */
public class Data extends SwipeData {

    private int index;

    public Data(int index) {

        this.index = index;
    }

    @Override
    public String getUserJid() {

        return Cheeses.NAMES[index];
    }

    @Override
    public String getConten() {

        return Cheeses.sCheeseStrings[index];
    }

    @Override
    public int getNewMsgCount() {

        return index;
    }
}
