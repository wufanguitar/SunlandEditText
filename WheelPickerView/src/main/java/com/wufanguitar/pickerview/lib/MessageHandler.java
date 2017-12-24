package com.wufanguitar.pickerview.lib;

import android.os.Handler;
import android.os.Message;

/**
 * @Author: Frank Wu
 * @Time: 2017/12/24 on 22:00
 * @Email: wu.fanguitar@163.com
 * @Description: 利用Handler操作WheelView的刷新/滑动/选中回调
 */

final class MessageHandler extends Handler {
    public static final int WHAT_INVALIDATE_LOOP_VIEW = 1000;
    public static final int WHAT_SMOOTH_SCROLL = 2000;
    public static final int WHAT_ITEM_SELECTED = 3000;

    final WheelView mWheelView;

    MessageHandler(WheelView wheelView) {
        this.mWheelView = wheelView;
    }

    @Override
    public final void handleMessage(Message msg) {
        switch (msg.what) {
            case WHAT_INVALIDATE_LOOP_VIEW:
                mWheelView.invalidate();
                break;

            case WHAT_SMOOTH_SCROLL:
                mWheelView.smoothScroll(Action.FLING);
                break;

            case WHAT_ITEM_SELECTED:
                mWheelView.onItemSelected();
                break;
        }
    }

}
