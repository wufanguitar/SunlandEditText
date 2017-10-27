package com.example.sunlandedittext;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.renderscript.ScriptGroup;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

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
    private Drawable mClearDrawable;
    private Drawable mPwdToggleDrawable;
    private Bitmap mBitmap;
    private int mShowPwdResId;
    private int mHidePwdResId;
    private static final int STATUS_UNFOCUSED = 0;
    private static final int STATUS_FOCUSED = 1;
    private boolean isFocused = false; // 是否获取焦点
    private int mMaxLength = Integer.MAX_VALUE;
    private boolean enableClear; // 设置是否启动清除功能
    private boolean isPwdInputType; // 输入类型是否为密码类型
    private boolean isPwdShow; // 是否显示密码


    public SunlandEditText(Context context) {
        this(context, null);
    }

    public SunlandEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public SunlandEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs, defStyleAttr);
        init();
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SunlandEditText, defStyleAttr, 0);
        enableClear = typedArray.getBoolean(R.styleable.SunlandEditText_enableClear, true);
        if (enableClear) {
            int clearId = typedArray.getResourceId(R.styleable.SunlandEditText_clearDrawable, -1);
            if (clearId == -1)
                clearId = R.drawable.sunland_et_svg_ic_clear_24dp;
            mClearDrawable = ContextCompat.getDrawable(context, clearId);
            mClearDrawable.setBounds(0, 0, mClearDrawable.getIntrinsicWidth(),
                    mClearDrawable.getIntrinsicHeight());
            if (clearId == R.drawable.sunland_et_svg_ic_clear_24dp)
                DrawableCompat.setTint(mClearDrawable, getCurrentHintTextColor());
        }

        int inputType = getInputType();
        int textPasswordType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD; // 129
        int textVisiblePasswordType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD; // 145
        int textWebPasswordType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD; // 225
        int numberPasswordType = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD; // 18
        if (inputType == textPasswordType || inputType == textVisiblePasswordType ||
                inputType == textWebPasswordType || inputType == numberPasswordType) {
            isPwdInputType = true;
            isPwdShow = inputType == 145;
            mMaxLength = 20;

            mShowPwdResId = typedArray.getResourceId(R.styleable.SunlandEditText_showPwdDrawable, -1);
            mHidePwdResId = typedArray.getResourceId(R.styleable.SunlandEditText_hidePwdDrawable, -1);
            if (mShowPwdResId == -1)
                mShowPwdResId = R.drawable.sunland_et_svg_ic_show_password_24dp;
            if (mHidePwdResId == -1)
                mHidePwdResId = R.drawable.sunland_et_svg_ic_hide_password_24dp;

            int pwdId = isPwdShow ? mShowPwdResId : mHidePwdResId;
            mPwdToggleDrawable = ContextCompat.getDrawable(context, pwdId);
            if (mShowPwdResId == R.drawable.sunland_et_svg_ic_show_password_24dp ||
                    mHidePwdResId == R.drawable.sunland_et_svg_ic_hide_password_24dp) {
                DrawableCompat.setTint(mPwdToggleDrawable, getCurrentHintTextColor());
            }
            mPwdToggleDrawable.setBounds(0, 0, mPwdToggleDrawable.getIntrinsicWidth(),
                    mPwdToggleDrawable.getIntrinsicHeight());

            int clearId = typedArray.getResourceId(R.styleable.SunlandEditText_clearDrawable, -1);
            if (clearId == -1)
                clearId = R.drawable.sunland_et_svg_ic_clear_24dp;
            if (enableClear) {
                mBitmap = getBitmapFromVectorDrawable(context, clearId,
                        clearId == R.drawable.sunland_et_svg_ic_clear_24dp); // clearDrawable
            }

        }

        mLeftDrawableFocus = typedArray.getDrawable(R.styleable.SunlandEditText_leftDrawableFocus);
        mLeftDrawableUnFocus = typedArray.getDrawable(R.styleable.SunlandEditText_leftDrawableUnFocus);
        mRightDrawable = typedArray.getDrawable(R.styleable.SunlandEditText_rightDrawable);
        typedArray.recycle();
    }

    private Bitmap getBitmapFromVectorDrawable(Context context, int drawableId, boolean tint) {
        Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }
        if (tint) {
            DrawableCompat.setTint(drawable, getCurrentHintTextColor());
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isFocused && mBitmap != null && isPwdInputType && !isTextEmpty()) {
            int left = getMeasuredWidth() - getPaddingRight() -
                    mPwdToggleDrawable.getIntrinsicWidth() - mBitmap.getWidth() - dp2px(4);
            int top = (getMeasuredHeight() - mBitmap.getHeight()) >> 1;
            canvas.drawBitmap(mBitmap, left, top, null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isFocused && isPwdInputType && event.getAction() == MotionEvent.ACTION_UP) {
            int width = mPwdToggleDrawable.getIntrinsicWidth();
            int height = mPwdToggleDrawable.getIntrinsicHeight();
            int top = (getMeasuredHeight() - height) >> 1;
            int right = getMeasuredWidth() - getPaddingRight();
            boolean isAreaX = event.getX() <= right && event.getX() >= right - width;
            boolean isAreaY = event.getY() >= top && event.getY() <= top + height;
            if (isAreaX && isAreaY) { // 点击在密码是否可见区域内
                isPwdShow = !isPwdShow;
                if (isPwdShow) {
                    setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                setSelection(getSelectionStart(), getSelectionEnd());
                mPwdToggleDrawable = ContextCompat.getDrawable(getContext(), isPwdShow ?
                        mShowPwdResId : mHidePwdResId);
                if (mShowPwdResId == R.drawable.sunland_et_svg_ic_show_password_24dp ||
                        mHidePwdResId == R.drawable.sunland_et_svg_ic_hide_password_24dp) {
                    DrawableCompat.setTint(mPwdToggleDrawable, getCurrentHintTextColor());
                }
                mPwdToggleDrawable.setBounds(0, 0, mPwdToggleDrawable.getIntrinsicWidth(),
                        mPwdToggleDrawable.getIntrinsicHeight());
                // 注意与setCompoundDrawablesWithIntrinsicBounds的区别
                setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1],
                        mPwdToggleDrawable, getCompoundDrawables()[3]);
                invalidate();
            }

            if (enableClear) {
                right -= width + dp2px(4);
                isAreaX = event.getX() <= right && event.getX() >= right - mBitmap.getWidth();
                if (isAreaX && isAreaY) {
                    setError(null);
                    setText("");
                }
            }
        }

        if (isFocused && enableClear && !isPwdInputType && event.getAction() == MotionEvent.ACTION_UP) {
            Rect rect = mClearDrawable.getBounds();
            int rectW = rect.width();
            int rectH = rect.height();
            int top = (getMeasuredHeight() - rectH) >> 1;
            int right = getMeasuredWidth() - getPaddingRight();
            boolean isAreaX = event.getX() <= right && event.getX() >= right - rectW;
            boolean isAreaY = event.getY() >= top && event.getY() <= (top + rectH);
            if (isAreaX && isAreaY) {
                setError(null);
                setText("");
            }
        }

        return super.onTouchEvent(event);
    }

    private void init() {
        // 设置左侧图标
        setLeftIconStatus();
        // 设置文本改变监听器
        addTextChangedListener(this);
    }

    /**
     * 左侧图标与右侧图标分开处理，避免复杂的逻辑代码
     */
    private void setLeftIconStatus() {
        if (isFocused) {
            if (isPwdInputType)
                setCompoundDrawablesWithIntrinsicBounds(mLeftDrawableFocus, null, mPwdToggleDrawable, null);
            else
                setCompoundDrawablesWithIntrinsicBounds(mLeftDrawableFocus, null, null, null);
        } else {
            setCompoundDrawablesWithIntrinsicBounds(mLeftDrawableUnFocus, null, null, null);
        }
    }


    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        this.isFocused = focused;
        setRightIconStatus();
        setLeftIconStatus();
    }

    private void setRightIconStatus() {
        /**
         *                 是密码：显示密码图标
         *         空输入
         *                 非密码：不显示密码图标和清除图标
         * 有焦点
         *                 是密码：显示密码图标和清除图标
         *         有输入
         *                 非密码：不显示密码图标，显示清除图标
         */
        if (!isFocused || (isTextEmpty() && !isPwdInputType)) {
            setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1],
                    null, getCompoundDrawables()[3]);

            if (!isTextEmpty() && isPwdInputType) {
                invalidate();
            }
        } else {
            if (isPwdInputType) {
                if (mShowPwdResId == R.drawable.sunland_et_svg_ic_show_password_24dp ||
                        mHidePwdResId == R.drawable.sunland_et_svg_ic_hide_password_24dp) {
                    DrawableCompat.setTint(mPwdToggleDrawable, getCurrentHintTextColor());
                }
                setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1],
                        mPwdToggleDrawable, getCompoundDrawables()[3]);
            } else if (!isTextEmpty() && enableClear) {
                setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1],
                        mClearDrawable, getCompoundDrawables()[3]);
            }
        }
    }

    // ===== 工具方法 begin ======
    private boolean isTextEmpty() {
        return getText().toString().trim().length() == 0;
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                Resources.getSystem().getDisplayMetrics());
    }
    // ===== 工具方法 end ======

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        int currLength = s.length();
        setRightIconStatus();

        if (currLength > mMaxLength) {
            getText().delete(currLength - 1, currLength);
            return;
        }
    }
}
