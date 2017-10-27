package com.example.sunlandedittext;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

/**
 * Created by 吴凡 on 2017/10/26.
 * 用户名：
 * 1. 在没有内容时，不显示清除按钮；有内容时，显示清除按钮
 * 2. 在有内容时，点击清除按钮可以删除内容
 * 密码：
 * 1. 在没有内容的时候，不显示清除按钮和密码可见按钮；有内容时，都可见
 * 2. 在有内容时，点击清除按钮可以删除内容，点击密码可见按钮即可显示密码
 */

public class SunlandEditText extends AppCompatEditText implements TextWatcher {
    private Context mContext;
    private Drawable mLeftDrawableFocus;
    private Drawable mLeftDrawableUnFocus;
    private Drawable mRightDrawable;
    private int mSize; // 左/右自定义图标大小
    private int mCurrentStatus = 0;
    private static final int STATUS_UNFOCUSED = 0;
    private static final int STATUS_FOCUSED = 1;
    private boolean hasFocus = false; // 是否获取焦点


    public SunlandEditText(Context context) {
        this(context, null);
    }

    public SunlandEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public SunlandEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initAttrs(mContext, attrs);
        init();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SunlandEditText);
        mLeftDrawableFocus = typedArray.getDrawable(R.styleable.SunlandEditText_leftDrawableFocus);
        mLeftDrawableUnFocus = typedArray.getDrawable(R.styleable.SunlandEditText_leftDrawableUnFocus);
        mRightDrawable = typedArray.getDrawable(R.styleable.SunlandEditText_rightDrawable);
        typedArray.recycle();
    }

    private void init() {
        setStatus(mCurrentStatus);
        // 设置左/右侧图标
        setLeftDrawable();
        setRightDrawable();

        // 设置文本改变监听器
        addTextChangedListener(this);
    }

    private void setStatus(int status) {
        mCurrentStatus = status;
        switch (status) {
            case STATUS_UNFOCUSED:
                setCompoundDrawablesWithIntrinsicBounds(mLeftDrawableUnFocus, null, null, null);
                break;
            case STATUS_FOCUSED:
                setCompoundDrawablesWithIntrinsicBounds(mLeftDrawableFocus, null, null, null);
                break;
            default:
                break;
        }

    }

    private void setRightDrawable() {
        if (length() < 1) {
            setRightIconVisible(false);
        } else {
            setRightIconVisible(true);
        }
    }

    /**
     * 根据是否有输入内容来控制右侧图标的显示
     *
     * @param isShow
     */
    private void setRightIconVisible(boolean isShow) {
        setCompoundDrawablesWithIntrinsicBounds(mLeftDrawableFocus, null,
                isShow ? mRightDrawable : null, null);
    }

    /**
     * 创建对象时直接显示左侧图标
     */
    private void setLeftDrawable() {
        setCompoundDrawablesWithIntrinsicBounds(mLeftDrawableFocus, null, null, null);
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        super.onTextChanged(s, start, before, count);
        setRightDrawable();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }



}
