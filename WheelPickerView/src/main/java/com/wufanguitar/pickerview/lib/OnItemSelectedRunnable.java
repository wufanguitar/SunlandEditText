package com.wufanguitar.pickerview.lib;

/**
 * @Author: Frank Wu
 * @Time: 2017/12/24 on 16:53
 * @Email: wu.fanguitar@163.com
 * @Description:
 */

final class OnItemSelectedRunnable implements Runnable {
    final WheelView mWheelView;

    public OnItemSelectedRunnable(WheelView wheelView) {
        mWheelView = wheelView;
    }

    @Override
    public final void run() {
        if (mWheelView == null || mWheelView.getOnItemSelectedListener() == null) {
            return;
        }
        mWheelView.getOnItemSelectedListener().onItemSelected(mWheelView.getCurrentItem());
    }
}
