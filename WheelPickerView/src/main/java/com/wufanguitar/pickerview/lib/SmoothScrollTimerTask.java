package com.wufanguitar.pickerview.lib;

import java.util.TimerTask;

/**
 * @Author: Frank Wu
 * @Time: 2017/12/24 on 22:04
 * @Email: wu.fanguitar@163.com
 * @Description: 利用计时器实现平滑滚动
 */

final class SmoothScrollTimerTask extends TimerTask {

    int mRealTotalOffset;
    int mRealOffset;
    int mOffset;
    final WheelView mWheelView;

    SmoothScrollTimerTask(WheelView wheelView, int offset) {
        this.mWheelView = wheelView;
        this.mOffset = offset;
        this.mRealTotalOffset = Integer.MAX_VALUE;
        this.mRealOffset = 0;
    }

    @Override
    public final void run() {
        if (mWheelView.getHandler() == null) {
            return;
        }
        if (mRealTotalOffset == Integer.MAX_VALUE) {
            mRealTotalOffset = mOffset;
        }
        // 把要滚动的范围细分成10小份，按10小份单位来重绘
        mRealOffset = (int) ((float) mRealTotalOffset * 0.1F);

        if (mRealOffset == 0) {
            if (mRealTotalOffset < 0) {
                mRealOffset = -1;
            } else {
                mRealOffset = 1;
            }
        }

        if (Math.abs(mRealTotalOffset) <= 1) {
            mWheelView.cancelFuture();
            mWheelView.getHandler().sendEmptyMessage(MessageHandler.WHAT_ITEM_SELECTED);
        } else {
            mWheelView.setTotalScrollY(mWheelView.getTotalScrollY() + mRealOffset);

            // 这里如果不是循环模式，则点击空白位置需要回滚，不然就会出现选到 -1 item 的情况
            if (!mWheelView.isLoop()) {
                float itemHeight = mWheelView.getItemHeight();
                float top = (float) (-mWheelView.getInitPosition()) * itemHeight;
                float bottom = (float) (mWheelView.getItemsCount() - 1 - mWheelView.getInitPosition()) * itemHeight;
                if (mWheelView.getTotalScrollY() <= top || mWheelView.getTotalScrollY() >= bottom) {
                    mWheelView.setTotalScrollY(mWheelView.getTotalScrollY() - mRealOffset);
                    mWheelView.cancelFuture();
                    mWheelView.getHandler().sendEmptyMessage(MessageHandler.WHAT_ITEM_SELECTED);
                    return;
                }
            }
            mWheelView.getHandler().sendEmptyMessage(MessageHandler.WHAT_INVALIDATE_LOOP_VIEW);
            mRealTotalOffset = mRealTotalOffset - mRealOffset;
        }
    }
}
