package com.wufanguitar.pickerview.lib;

import java.util.TimerTask;

/**
 * @Author: Frank Wu
 * @Time: 2017/12/24 on 22:34
 * @Email: wu.fanguitar@163.com
 * @Description:
 */

final class InertiaTimerTask extends TimerTask {
    // 用来控制 Y 轴滑动速率
    float mSlipRate;
    final float mVelocityY;
    final WheelView mWheelView;

    InertiaTimerTask(WheelView wheelView, float velocityY) {
        super();
        mWheelView = wheelView;
        this.mVelocityY = velocityY;
        mSlipRate = Integer.MAX_VALUE;
    }

    @Override
    public final void run() {
        if (mWheelView.getHandler() == null) {
            return;
        }
        if (mSlipRate == Integer.MAX_VALUE) {
            if (Math.abs(mVelocityY) > 2000F) {
                if (mVelocityY > 0.0F) {
                    mSlipRate = 2000F;
                } else {
                    mSlipRate = -2000F;
                }
            } else {
                mSlipRate = mVelocityY;
            }
        }
        if (Math.abs(mSlipRate) >= 0.0F && Math.abs(mSlipRate) <= 20F) {
            mWheelView.cancelFuture();
            mWheelView.getHandler().sendEmptyMessage(MessageHandler.WHAT_SMOOTH_SCROLL);
            return;
        }
        int i = (int) ((mSlipRate * 10F) / 1000F);
        mWheelView.setTotalScrollY(mWheelView.getTotalScrollY() - i);
        if (!mWheelView.isLoop()) {
            float itemHeight = mWheelView.getItemHeight();
            float top = (-mWheelView.getInitPosition()) * itemHeight;
            float bottom = (mWheelView.getItemsCount() - 1 - mWheelView.getInitPosition()) * itemHeight;
            if (mWheelView.getTotalScrollY() - itemHeight * 0.25 < top) {
                top = mWheelView.getTotalScrollY() + i;
            } else if (mWheelView.getTotalScrollY() + itemHeight * 0.25 > bottom) {
                bottom = mWheelView.getTotalScrollY() + i;
            }

            if (mWheelView.getTotalScrollY() <= top) {
                mSlipRate = 40F;
                mWheelView.setTotalScrollY(top);
            } else if (mWheelView.getTotalScrollY() >= bottom) {
                mWheelView.setTotalScrollY(bottom);
                mSlipRate = -40F;
            }
        }
        if (mSlipRate < 0.0F) {
            mSlipRate = mSlipRate + 20F;
        } else {
            mSlipRate = mSlipRate - 20F;
        }
        mWheelView.getHandler().sendEmptyMessage(MessageHandler.WHAT_INVALIDATE_LOOP_VIEW);
    }

}
