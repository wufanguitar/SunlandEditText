package com.wufanguitar.pickerview.adapter;

/**
 * @Author: Frank Wu
 * @Time: 2017/12/24 on 23:06
 * @Email: wu.fanguitar@163.com
 * @Description: WheelView适配器
 */

public interface WheelAdapter<T> {
	/**
	 * 获取 items 的数量
	 */
	int getItemsCount();
	
	/**
	 * 通过给定位置获取 item 对象
	 */
	T getItem(int index);
	
	/**
     * 获取指定 item 对象的位置
     */
	int indexOf(T o);
}
