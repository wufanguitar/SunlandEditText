package com.wufanguitar.pickerview.adapter;

/**
 * @Author: Frank Wu
 * @Time: 2017/12/24 on 23:12
 * @Email: wu.fanguitar@163.com
 * @Description: 纯数字的滚动适配器
 */

public class NumericWheelAdapter implements WheelAdapter {
    public static final int DEFAULT_MAX_VALUE = 9;
    private static final int DEFAULT_MIN_VALUE = 0;
    private int mMinValue;
    private int mMaxValue;

    public NumericWheelAdapter() {
        this(DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
    }

    public NumericWheelAdapter(int minValue, int maxValue) {
        this.mMinValue = minValue;
        this.mMaxValue = maxValue;
    }

    @Override
    public Integer getItem(int index) {
        if (index >= 0 && index < getItemsCount()) {
            int value = mMinValue + index;
            return value;
        }
        return 0;
    }

    @Override
    public int getItemsCount() {
        return mMaxValue - mMinValue + 1;
    }

    @Override
    public int indexOf(Object o) {
        try {
            return (int) o - mMinValue;
        } catch (Exception e) {
            return -1;
        }
    }
}
