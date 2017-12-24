package com.wufanguitar.pickerview.adapter;

import java.util.List;

/**
 * @Author: Frank Wu
 * @Time: 2017/12/24 on 23:14
 * @Email: wu.fanguitar@163.com
 * @Description: 简单数组的滚动适配器
 */

public class ArrayWheelAdapter<T> implements WheelAdapter {
	public static final int DEFAULT_LENGTH = 4;
	private List<T> mItems;
	private int mLength;

	public ArrayWheelAdapter(List<T> items, int length) {
		this.mItems = items;
		this.mLength = length;
	}

	public ArrayWheelAdapter(List<T> items) {
		this(items, DEFAULT_LENGTH);
	}

	@Override
	public Object getItem(int index) {
		if (index >= 0 && index < mItems.size()) {
			return mItems.get(index);
		}
		return "";
	}

	@Override
	public int getItemsCount() {
		return mItems.size();
	}

	@Override
	public int indexOf(Object o){
		return mItems.indexOf(o);
	}
}
