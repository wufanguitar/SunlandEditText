package com.wufanguitar.pickerview.view;

import android.graphics.Typeface;
import android.view.View;

import com.wufanguitar.pickerview.R;
import com.wufanguitar.pickerview.adapter.ArrayWheelAdapter;
import com.wufanguitar.pickerview.lib.DividerType;
import com.wufanguitar.pickerview.lib.WheelView;
import com.wufanguitar.pickerview.listener.OnItemSelectedListener;

import java.util.List;

public class WheelOptions<T> {
    private View mView;
    private WheelView mOptionFirst;
    private WheelView mOptionSecond;
    private WheelView mOptionThird;

    private List<T> mOptionFirstItems;
    private List<List<T>> mRelatedOptionSecondItems;
    private List<T> mOptionSecondItems;
    private List<List<List<T>>> mRelatedOptionThirdItems;
    private List<T> mOptionThirdItems;
    private boolean mLinkage;
    private OnItemSelectedListener mOptionFirstListener;
    private OnItemSelectedListener mOptionSecondListener;

    // 外部文字的颜色
    int mOutTextColor;
    // 选项中文字的颜色
    int mCenterTextColor;
    // 分割线的颜色
    int mDividerColor;

    private DividerType mDividerType;

    // 条目间距倍数
    float mLineSpacingMultiplier = 1.6F;

    public View getView() {
        return mView;
    }

    public void setView(View view) {
        this.mView = view;
    }

    public WheelOptions(View view, Boolean linkage) {
        super();
        this.mLinkage = linkage;
        this.mView = view;
        // 初始化时显示的数据
        mOptionFirst = (WheelView) view.findViewById(R.id.options1);
        mOptionSecond = (WheelView) view.findViewById(R.id.options2);
        mOptionThird = (WheelView) view.findViewById(R.id.options3);
    }

    // 联动情况下
    public void setRelatedPicker(List<T> options1Items,
                          List<List<T>> options2Items,
                          List<List<List<T>>> options3Items) {
        this.mOptionFirstItems = options1Items;
        this.mRelatedOptionSecondItems = options2Items;
        this.mRelatedOptionThirdItems = options3Items;
        int len = ArrayWheelAdapter.DEFAULT_LENGTH;
        if (this.mRelatedOptionThirdItems == null) {
            len = 8;
        }
        if (this.mRelatedOptionSecondItems == null) {
            len = 12;
        }
        // 设置选项一显示数据
        mOptionFirst.setAdapter(new ArrayWheelAdapter(mOptionFirstItems, len));
        // 初始化时显示的数据
        mOptionFirst.setCurrentItem(0);

        // 设置选项二显示数据
        if (mRelatedOptionSecondItems != null) {
            mOptionSecond.setAdapter(new ArrayWheelAdapter(mRelatedOptionSecondItems.get(0))); // 设置显示数据
        }
        mOptionSecond.setCurrentItem(mOptionFirst.getCurrentItem());// 初始化时显示的数据
        // 设置选项三显示数据
        if (mRelatedOptionThirdItems != null)
            mOptionThird.setAdapter(new ArrayWheelAdapter(mRelatedOptionThirdItems.get(0).get(0))); // 设置显示数据
        mOptionThird.setCurrentItem(mOptionThird.getCurrentItem());
        mOptionFirst.setIsOptions(true);
        mOptionSecond.setIsOptions(true);
        mOptionThird.setIsOptions(true);

        if (this.mRelatedOptionSecondItems == null) {
            mOptionSecond.setVisibility(View.GONE);
        } else {
            mOptionSecond.setVisibility(View.VISIBLE);
        }
        if (this.mRelatedOptionThirdItems == null) {
            mOptionThird.setVisibility(View.GONE);
        } else {
            mOptionThird.setVisibility(View.VISIBLE);
        }

        // 联动监听器
        mOptionFirstListener = new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                int opt2Select = 0;
                if (mRelatedOptionSecondItems != null) {
                    opt2Select = mOptionSecond.getCurrentItem();//上一个opt2的选中位置
                    //新opt2的位置，判断如果旧位置没有超过数据范围，则沿用旧位置，否则选中最后一项
                    opt2Select = opt2Select >= mRelatedOptionSecondItems.get(index).size() - 1 ? mRelatedOptionSecondItems.get(index).size() - 1 : opt2Select;

                    mOptionSecond.setAdapter(new ArrayWheelAdapter(mRelatedOptionSecondItems.get(index)));
                    mOptionSecond.setCurrentItem(opt2Select);
                }
                if (mRelatedOptionThirdItems != null) {
                    mOptionSecondListener.onItemSelected(opt2Select);
                }
            }
        };

        mOptionSecondListener = new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                if (mRelatedOptionThirdItems != null) {
                    int opt1Select = mOptionFirst.getCurrentItem();
                    opt1Select = opt1Select >= mRelatedOptionThirdItems.size() - 1 ? mRelatedOptionThirdItems.size() - 1 : opt1Select;
                    index = index >= mRelatedOptionSecondItems.get(opt1Select).size() - 1 ? mRelatedOptionSecondItems.get(opt1Select).size() - 1 : index;
                    int opt3 = mOptionThird.getCurrentItem(); // 上一个 第三选项 的选中位置
                    // 新 第三选项 的位置，判断如果旧位置没有超过数据范围，则沿用旧位置，否则选中最后一项
                    opt3 = opt3 >= mRelatedOptionThirdItems.get(opt1Select).get(index).size() - 1 ? mRelatedOptionThirdItems.get(opt1Select).get(index).size() - 1 : opt3;

                    mOptionThird.setAdapter(new ArrayWheelAdapter(mRelatedOptionThirdItems.get(mOptionFirst.getCurrentItem()).get(index)));
                    mOptionThird.setCurrentItem(opt3);

                }
            }
        };

        // 添加联动监听
        if (options2Items != null && mLinkage)
            mOptionFirst.setOnItemSelectedListener(mOptionFirstListener);
        if (options3Items != null && mLinkage)
            mOptionSecond.setOnItemSelectedListener(mOptionSecondListener);
    }


    // 不联动情况下
    public void setNPicker(List<T> options1Items,
                           List<T> options2Items,
                           List<T> options3Items) {
        this.mOptionFirstItems = options1Items;
        this.mOptionSecondItems = options2Items;
        this.mOptionThirdItems = options3Items;
        int len = ArrayWheelAdapter.DEFAULT_LENGTH;
        if (this.mOptionThirdItems == null)
            len = 8;
        if (this.mOptionSecondItems == null)
            len = 12;

        // 设置选项一显示数据
        mOptionFirst.setAdapter(new ArrayWheelAdapter(mOptionFirstItems, len));
        /// 初始化时显示的数据
        mOptionFirst.setCurrentItem(0);

        // 选项2
        if (mOptionSecondItems != null)
            mOptionSecond.setAdapter(new ArrayWheelAdapter(mOptionSecondItems));// 设置显示数据
        mOptionSecond.setCurrentItem(mOptionFirst.getCurrentItem());// 初始化时显示的数据
        // 选项3
        if (mOptionThirdItems != null)
            mOptionThird.setAdapter(new ArrayWheelAdapter(mOptionThirdItems));// 设置显示数据
        mOptionThird.setCurrentItem(mOptionThird.getCurrentItem());
        mOptionFirst.setIsOptions(true);
        mOptionSecond.setIsOptions(true);
        mOptionThird.setIsOptions(true);

        if (this.mOptionSecondItems == null) {
            mOptionSecond.setVisibility(View.GONE);
        } else {
            mOptionSecond.setVisibility(View.VISIBLE);
        }
        if (this.mOptionThirdItems == null) {
            mOptionThird.setVisibility(View.GONE);
        } else {
            mOptionThird.setVisibility(View.VISIBLE);
        }
    }

    public void setTextContentSize(int textSize) {
        mOptionFirst.setTextSize(textSize);
        mOptionSecond.setTextSize(textSize);
        mOptionThird.setTextSize(textSize);
    }

    private void setOutTextColor() {
        mOptionFirst.setOutTextColor(mOutTextColor);
        mOptionSecond.setOutTextColor(mOutTextColor);
        mOptionThird.setOutTextColor(mOutTextColor);
    }

    private void setCenterTextColor() {
        mOptionFirst.setCenterTextColor(mCenterTextColor);
        mOptionSecond.setCenterTextColor(mCenterTextColor);
        mOptionThird.setCenterTextColor(mCenterTextColor);
    }

    private void setDividerColor() {
        mOptionFirst.setDividerColor(mDividerColor);
        mOptionSecond.setDividerColor(mDividerColor);
        mOptionThird.setDividerColor(mDividerColor);
    }

    private void setDividerType() {
        mOptionFirst.setDividerType(mDividerType);
        mOptionSecond.setDividerType(mDividerType);
        mOptionThird.setDividerType(mDividerType);
    }

    private void setLineSpacingMultiplier() {
        mOptionFirst.setLineSpacingMultiplier(mLineSpacingMultiplier);
        mOptionSecond.setLineSpacingMultiplier(mLineSpacingMultiplier);
        mOptionThird.setLineSpacingMultiplier(mLineSpacingMultiplier);
    }

    /**
     * 设置选项的单位
     *
     * @param label1 单位
     * @param label2 单位
     * @param label3 单位
     */
    public void setLabels(String label1, String label2, String label3) {
        if (label1 != null)
            mOptionFirst.setLabel(label1);
        if (label2 != null)
            mOptionSecond.setLabel(label2);
        if (label3 != null)
            mOptionThird.setLabel(label3);
    }

    /**
     * 设置x轴偏移量
     */
    public void setTextXOffset(int xoffset_one, int xoffset_two, int xoffset_three){
        mOptionFirst.setTextXOffset(xoffset_one);
        mOptionSecond.setTextXOffset(xoffset_two);
        mOptionThird.setTextXOffset(xoffset_three);
    }

    /**
     * 设置是否循环滚动
     *
     * @param cyclic 是否循环
     */
    public void setCyclic(boolean cyclic) {
        mOptionFirst.setCyclic(cyclic);
        mOptionSecond.setCyclic(cyclic);
        mOptionThird.setCyclic(cyclic);
    }

    /**
     * 设置字体样式
     *
     * @param font 系统提供的几种样式
     */
    public void setTypeface(Typeface font) {
        mOptionFirst.setTypeface(font);
        mOptionSecond.setTypeface(font);
        mOptionThird.setTypeface(font);
    }

    /**
     * 分别设置第一二三级是否循环滚动
     *
     * @param cyclic1,cyclic2,cyclic3 是否循环
     */
    public void setCyclic(boolean cyclic1, boolean cyclic2, boolean cyclic3) {
        mOptionFirst.setCyclic(cyclic1);
        mOptionSecond.setCyclic(cyclic2);
        mOptionThird.setCyclic(cyclic3);
    }

    /**
     * 返回当前选中的结果对应的位置数组 因为支持三级联动效果，分三个级别索引: 0、1、2。
     * 在快速滑动未停止时，点击确定按钮，会进行判断，如果匹配数据越界，则设为0，防止index出错导致崩溃。
     *
     * @return 索引数组
     */
    public int[] getCurrentItems() {
        int[] currentItems = new int[3];
        currentItems[0] = mOptionFirst.getCurrentItem();

        if (mRelatedOptionSecondItems != null && mRelatedOptionSecondItems.size() > 0) {//非空判断
            currentItems[1] = mOptionSecond.getCurrentItem() > (mRelatedOptionSecondItems.get(currentItems[0]).size() - 1) ? 0 : mOptionSecond.getCurrentItem();
        } else {
            currentItems[1] = mOptionSecond.getCurrentItem();
        }

        if (mRelatedOptionThirdItems != null && mRelatedOptionThirdItems.size() > 0) {//非空判断
            currentItems[2] = mOptionThird.getCurrentItem() > (mRelatedOptionThirdItems.get(currentItems[0]).get(currentItems[1]).size() - 1) ? 0 : mOptionThird.getCurrentItem();
        } else {
            currentItems[2] = mOptionThird.getCurrentItem();
        }

        return currentItems;
    }

    public void setCurrentItems(int option1, int option2, int option3) {
        if (mLinkage) {
            itemSelected(option1, option2, option3);
        }
        mOptionFirst.setCurrentItem(option1);
        mOptionSecond.setCurrentItem(option2);
        mOptionThird.setCurrentItem(option3);
    }

    private void itemSelected(int opt1Select, int opt2Select, int opt3Select) {
        if (mRelatedOptionSecondItems != null) {
            mOptionSecond.setAdapter(new ArrayWheelAdapter(mRelatedOptionSecondItems.get(opt1Select)));
            mOptionSecond.setCurrentItem(opt2Select);
        }
        if (mRelatedOptionThirdItems != null) {
            mOptionThird.setAdapter(new ArrayWheelAdapter(mRelatedOptionThirdItems.get(opt1Select).get(opt2Select)));
            mOptionThird.setCurrentItem(opt3Select);
        }
    }

    /**
     * 设置间距倍数,但是只能在1.2-2.0f之间
     *
     * @param lineSpacingMultiplier
     */
    public void setLineSpacingMultiplier(float lineSpacingMultiplier) {
        this.mLineSpacingMultiplier = lineSpacingMultiplier;
        setLineSpacingMultiplier();
    }

    /**
     * 设置分割线的颜色
     *
     * @param dividerColor
     */
    public void setDividerColor(int dividerColor) {
        this.mDividerColor = dividerColor;
        setDividerColor();
    }

    /**
     * 设置分割线的类型
     *
     * @param dividerType
     */
    public void setDividerType(DividerType dividerType) {
        this.mDividerType = dividerType;
        setDividerType();
    }

    /**
     * 设置两条分割线之间的文字的颜色
     */
    public void setCenterTextColor(int centerTextColor) {
        this.mCenterTextColor = centerTextColor;
        setCenterTextColor();
    }

    /**
     * 设置分割线以外文字的颜色
     */
    public void setOutTextColor(int textOutColor) {
        this.mOutTextColor = textOutColor;
        setOutTextColor();
    }

    /**
     * Label 是否只显示中间选中项
     *
     * @param isCenterLabel
     */
    public void setIsCenterLabel(Boolean isCenterLabel) {
        mOptionFirst.setIsCenterLabel(isCenterLabel);
        mOptionSecond.setIsCenterLabel(isCenterLabel);
        mOptionThird.setIsCenterLabel(isCenterLabel);
    }
}
