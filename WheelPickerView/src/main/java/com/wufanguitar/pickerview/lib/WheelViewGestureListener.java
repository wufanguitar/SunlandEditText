package com.wufanguitar.pickerview.lib;

import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

final class WheelViewGestureListener extends SimpleOnGestureListener {

    final WheelView mWheelView;

    WheelViewGestureListener(WheelView wheelView) {
        mWheelView = wheelView;
    }

    @Override
    public final boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        mWheelView.scrollBy(velocityY);
        return true;
    }
}
