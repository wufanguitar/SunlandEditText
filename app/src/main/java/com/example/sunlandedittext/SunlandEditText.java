package com.example.sunlandedittext;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.AppCompatEditText;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;

import java.util.Locale;

/**
 * Created by 吴凡 on 2017/10/26.
 * 用户名：
 * 1. 在没有内容时，不显示清除按钮；有内容时，显示清除按钮
 * 2. 在有内容时，点击清除按钮可以删除内容
 * <p>
 * 密码：
 * 1. 在没有内容的时候，不显示清除按钮和密码可见按钮；有内容时，都可见
 * 2. 在有内容时，点击清除按钮可以删除内容，点击密码可见按钮即可显示密码
 * <p>
 * 图标显示为正方形，左右侧自定义图标均同一大小
 * <p>
 * 支持数字格式:
 * 1. 国内手机号码默认输入显示为 111 1111 1111
 * 2. 其他纯数字显示格式自定义（比如银行卡号）：待完成
 * 支持无限长度的密码输入：主要解决了在密码输入类型情况当超过可见区域时，绘制的清除图表位置移动的问题
 * 支持动态更新左侧图标
 */
public class SunlandEditText extends AppCompatEditText {
    private static final String TAG = "SunlandEditText";
    private static final int[] DEFAULT_PATTERN = new int[]{3, 4, 4};
    private static final String DEFAULT_PHONE_SEPARATOR = " ";
    private static final String DEFAULT_NUMBER_SEPARATOR = "";
    private Context mContext;
    private Drawable mLeftDrawableFocus;
    private Drawable mLeftDrawableUnFocus;
    private Drawable mClearDrawable;
    private Drawable mPwdToggleDrawable;
    private Bitmap mBitmap;
    private int mShowPwdResId;
    private int mHidePwdResId;
    private boolean isFocused = false; // 是否获取焦点
    private boolean isAllowUpdate = false;
    private int mMaxLength = Integer.MAX_VALUE;
    private boolean enableClear; // 设置是否启动清除功能
    private boolean isPwdInputType; // 输入类型是否为密码类型
    private boolean isPwdShow; // 是否显示密码
    private TextWatcher mTextWatcher;
    private OnSunlandTextChangeListener mTextChangeListener; // 提供接口回调处理自定义事务
    private OnTextNonNullListener mTextNonNullListener; // 输入非空时回调
    private int mIconSize; // 设置图标大小
    private boolean iconSizeSeted; // 是否设置图标大小
    private boolean isPhoneType; // 是否为手机号类型
    private String mSeparator; // 分隔符
    private boolean noSeparator; // 是否设置分隔符
    private int[] pattern; // [3, 4, 4]: xxx xxxx xxxx
    private int[] intervals; // 用于记录分隔符在字符串中的索引位置
    private int mPreLength; // 记录输入之前的字符串长度

    private float mEditaleWidth = 0F; // 可见输入区域宽度（在EditText有焦点/无输入/密码情形下）
    private float mInputedWidth; // 实际已经在EditText中输入字符串的长度
    private float mOutWidth; // 超出宽度值，主要用于密码输入类型中清除图标的重绘
    private boolean isOut; // 实际输入长度是否超出可见输入区域宽度

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
        mIconSize = typedArray.getDimensionPixelSize(R.styleable.SunlandEditText_drawableSize, -1);
        iconSizeSeted = mIconSize != -1;

        // 清除按钮
        enableClear = typedArray.getBoolean(R.styleable.SunlandEditText_enableClear, true);
        if (enableClear) {
            int clearId = typedArray.getResourceId(R.styleable.SunlandEditText_clearDrawable, -1);
            if (clearId == -1)
                clearId = R.drawable.sunland_et_svg_ic_clear_24dp;
            mClearDrawable = ContextCompat.getDrawable(context, clearId);
            setBounds(mClearDrawable);
            if (clearId == R.drawable.sunland_et_svg_ic_clear_24dp)
                DrawableCompat.setTint(mClearDrawable, getCurrentHintTextColor());
        }

        // 密码可见按钮
        int inputType = getInputType();
        int textPasswordType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD; // 129
        int textVisiblePasswordType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD; // 145
        int textWebPasswordType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD; // 225
        int numberPasswordType = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD; // 18
        if (inputType == textPasswordType || inputType == textVisiblePasswordType ||
                inputType == textWebPasswordType || inputType == numberPasswordType) {
            isPwdInputType = true;
            isPwdShow = inputType == 145;

            mShowPwdResId = typedArray.getResourceId(R.styleable.SunlandEditText_showPwdDrawable, -1);
            mHidePwdResId = typedArray.getResourceId(R.styleable.SunlandEditText_hidePwdDrawable, -1);
            if (mShowPwdResId == -1)
                mShowPwdResId = R.drawable.sunland_et_svg_ic_show_password_24dp;
            if (mHidePwdResId == -1)
                mHidePwdResId = R.drawable.sunland_et_svg_ic_hide_password_24dp;

            int pwdId = isPwdShow ? mShowPwdResId : mHidePwdResId;
            mPwdToggleDrawable = ContextCompat.getDrawable(context, pwdId);
            setBounds(mPwdToggleDrawable);
            if (mShowPwdResId == R.drawable.sunland_et_svg_ic_show_password_24dp ||
                    mHidePwdResId == R.drawable.sunland_et_svg_ic_hide_password_24dp) {
                DrawableCompat.setTint(mPwdToggleDrawable, getCurrentHintTextColor());
            }

            int clearId = typedArray.getResourceId(R.styleable.SunlandEditText_clearDrawable, -1);
            if (clearId == -1)
                clearId = R.drawable.sunland_et_svg_ic_clear_24dp;
            if (enableClear) {
                mBitmap = getBitmapFromVectorDrawable(context, clearId,
                        clearId == R.drawable.sunland_et_svg_ic_clear_24dp);
            }
        }

        // 左侧按钮
        mLeftDrawableFocus = typedArray.getDrawable(R.styleable.SunlandEditText_leftDrawableFocus);
        mLeftDrawableUnFocus = typedArray.getDrawable(R.styleable.SunlandEditText_leftDrawableUnFocus);
        setBounds(mLeftDrawableFocus, mLeftDrawableUnFocus);

        isAllowUpdate = typedArray.getBoolean(R.styleable.SunlandEditText_allowLeftUpdate, false);
        // 手机号输入类型
        isPhoneType = inputType == InputType.TYPE_CLASS_PHONE;

        typedArray.recycle();
    }


    private void setBounds(@NonNull Drawable... drawables) {
        int size = drawables.length;
        for (int i = 0; i < size; i++) {
            drawables[i].setBounds(0, 0, iconSizeSeted ? mIconSize : drawables[i].getIntrinsicWidth(),
                    iconSizeSeted ? mIconSize : drawables[i].getIntrinsicHeight());
        }
    }

    private Bitmap getBitmapFromVectorDrawable(Context context, int drawableId, boolean tint) {
        Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }
        if (tint) {
            DrawableCompat.setTint(drawable, getCurrentHintTextColor());
        }

        Bitmap bitmap = Bitmap.createBitmap(iconSizeSeted ? mIconSize : drawable.getIntrinsicWidth(),
                iconSizeSeted ? mIconSize : drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isFocused && mBitmap != null && isPwdInputType && !isTextEmpty()) {
            int pwdToggleDrawableWidth = iconSizeSeted ? mIconSize : mPwdToggleDrawable.getIntrinsicWidth();
            int leftClear = getMeasuredWidth() - getPaddingRight() -
                    pwdToggleDrawableWidth - mBitmap.getWidth() - dp2px(4);
            int topClear = (getMeasuredHeight() - mBitmap.getHeight()) >> 1;
            if (mEditaleWidth == 0F) {
                mEditaleWidth = getMeasuredWidth() - getPaddingRight() - pwdToggleDrawableWidth - getCompoundDrawablePadding() -
                        (getLeft() + pwdToggleDrawableWidth + getPaddingLeft() + getCompoundDrawablePadding());
                Log.i(TAG, "we hope input space size is: " + mEditaleWidth);
            }
            if (isOut) {
                leftClear += mOutWidth;
                canvas.drawBitmap(mBitmap, leftClear, topClear, null);
                return;
            }
            canvas.drawBitmap(mBitmap, leftClear, topClear, null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isFocused && isPwdInputType && event.getAction() == MotionEvent.ACTION_UP) {
            int width = iconSizeSeted ? mIconSize : mPwdToggleDrawable.getIntrinsicWidth();
            int height = iconSizeSeted ? mIconSize : mPwdToggleDrawable.getIntrinsicHeight();
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
                setBounds(mPwdToggleDrawable);
                if (mShowPwdResId == R.drawable.sunland_et_svg_ic_show_password_24dp ||
                        mHidePwdResId == R.drawable.sunland_et_svg_ic_hide_password_24dp) {
                    DrawableCompat.setTint(mPwdToggleDrawable, getCurrentHintTextColor());
                }

                // 注意与setCompoundDrawablesWithIntrinsicBounds的区别
                setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1],
                        mPwdToggleDrawable, getCompoundDrawables()[3]);
                // 优化1：看源码发现上面setCompoundDrawables方法会调用invalidate(), 故此处可注释掉
                // invalidate();
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
        if (isCNPhone()) {
            setPhoneSeparator(DEFAULT_PHONE_SEPARATOR);
            setPhonePattern();
            withSeparator(true);
        } else {
            setPhoneSeparator(DEFAULT_NUMBER_SEPARATOR);
            withSeparator(false);
        }
        // 设置左侧图标
        setLeftIconStatus();
        // 设置文本改变监听器
        mTextWatcher = new SunlandTextWatcher();
        addTextChangedListener(mTextWatcher);
    }

    private void setPhoneSeparator(String phoneSeparator) {
        this.mSeparator = phoneSeparator;
    }

    // 仅由setNumberPattern调用，执行前已排除国内手机号码
    private void setNumberSeparator(String numberSeparator) {
        this.mSeparator = numberSeparator;
    }

    private void setPhonePattern() {
        setUpPattern(DEFAULT_PATTERN);
    }

    /**
     * 根据定义的规则进行初始化操作
     *
     * @param pattern
     */
    private void setUpPattern(int[] pattern) {
        this.pattern = pattern;
        intervals = new int[pattern.length];
        int count = 0;
        int sum = 0;
        for (int i = 0; i < pattern.length; i++) {
            sum += pattern[i];
            intervals[i] = sum + count;
            if (i < pattern.length - 1) {
                count += mSeparator.length();
            }
        }
        mMaxLength = intervals[intervals.length - 1];

        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(mMaxLength);
        setFilters(filters);
    }

    /**
     * 左侧图标与右侧图标分开处理，避免复杂的逻辑代码
     * 解决：当密码类型/有焦点/无输入时无法显示密码可见图标
     */
    private void setLeftIconStatus() {
        if (isFocused) {
            if (isPwdInputType)
                setCompoundDrawables(mLeftDrawableFocus, getCompoundDrawables()[1],
                        mPwdToggleDrawable, getCompoundDrawables()[3]);
            else
                setCompoundDrawables(mLeftDrawableFocus, getCompoundDrawables()[1],
                        getCompoundDrawables()[2], getCompoundDrawables()[3]);
        } else {
            setCompoundDrawables(mLeftDrawableUnFocus, getCompoundDrawables()[1],
                    getCompoundDrawables()[2], getCompoundDrawables()[3]);
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
         *
         * 无焦点  均不显示
         */
        if (!isFocused || (isTextEmpty() && !isPwdInputType)) {
            setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1],
                    null, getCompoundDrawables()[3]);
            /* 优化2：这里当（无焦点/有输入/是密码）会走进来！
                      焦点切换后会触发setRightIconStatus调用，先调用setCompoundDrawables（源码会调用invalidate）
            if (!isTextEmpty() && isPwdInputType) {
                invalidate();
            }*/
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

    /* ==== begin 工具方法 ==== */
    private boolean isTextEmpty() {
        return getText().toString().trim().length() == 0;
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                Resources.getSystem().getDisplayMetrics());
    }

    private boolean isCNPhone() {
        return isCN() && isPhoneType;
    }

    private boolean isCN() {
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String countryIso = tm.getSimCountryIso();
        boolean isCN = false;
        if (!TextUtils.isEmpty(countryIso)) {
            countryIso = countryIso.toUpperCase(Locale.US);
            if (countryIso.contains("CN")) {
                isCN = true;
            }
        }
        return isCN;
    }

    /**
     * 通过TextPaint计算输入文本的宽度
     *
     * @param string
     * @return
     */
    private float getInputTextWidth(String string) {
        TextPaint textPaint = getPaint();
        return textPaint.measureText(string);
    }
    /* ==== 工具方法 end ==== */

    /* ==== begin SunlandTextWatcher 提供回调接口 ==== */
    private class SunlandTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            mPreLength = s.length();
            if (mTextChangeListener != null) {
                mTextChangeListener.beforeTextChanged(s, start, count, after);
            }
            int endCursor = getSelectionEnd();
            if (endCursor < mPreLength) {
                // to do
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mTextChangeListener != null) {
                mTextChangeListener.onTextChanged(s, start, before, count);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mTextChangeListener != null) {
                mTextChangeListener.afterTextChanged(s);
            }
            int currLength = s.length();
            if (currLength > 0 && mTextNonNullListener != null) {
                mTextNonNullListener.onTextNonNull(currLength >= mMaxLength, mMaxLength);
            }
            setRightIconStatus();
            // 计算超出宽度值
            // 如下代码不能在beforeTextChanged中执行，否则清除图标差一个输入字符的位移值
            isOut = false;
            mInputedWidth = getInputTextWidth(s.toString());
            if (mEditaleWidth != 0F && mEditaleWidth < mInputedWidth) {
                mOutWidth = mInputedWidth - mEditaleWidth;
                isOut = true;
                Log.i(TAG, "input text is out of range, the value is " + mOutWidth);
            }

            if (currLength > mMaxLength || isOut) {
                getText().delete(currLength - 1, currLength);
                return;
            }

            if (pattern == null) {
                return;
            }

            for (int i = 0; i < pattern.length; i++) {
                if (currLength - 1 == intervals[i]) {
                    if (currLength > mPreLength) {
                        if (currLength < mMaxLength) {
                            removeTextChangedListener(mTextWatcher);
                            getText().insert(currLength - 1, mSeparator);
                        }
                    } else if (mPreLength <= mMaxLength) {
                        removeTextChangedListener(mTextWatcher);
                        getText().delete(currLength - 1, currLength);
                    }
                    addTextChangedListener(mTextWatcher);
                    break;
                }
            }
        }
    }

    /* ==== SunlandTextWatcher 提供回调接口 end ==== */

    /* ==== begin 提供给开发者使用的方法 ==== */

    /**
     * 获取输入文本
     */
    public String getTrimmedString() {
        if (noSeparator) {
            return getText().toString().trim();
        } else {
            return getText().toString().replaceAll(mSeparator, "").trim();
        }
    }

    /**
     * 自定义数字格式
     */
    public void setNumberPattern(@NonNull int[] pattern) {
        setNumberPattern(pattern, DEFAULT_NUMBER_SEPARATOR);
    }

    public void setNumberPattern(@NonNull int[] pattern, @NonNull String numberSeparator) {
        if (isCNPhone()) {
            return;
        }
        setNumberSeparator(numberSeparator);
        setUpPattern(pattern);
    }

    /**
     * 拥有可“不采用分隔符”最高权限
     * 设置为true后，默认的国内11位手机号码模板格式[3, 4, 4]将无效
     */
    public SunlandEditText withSeparator(boolean withSeparator) {
        this.noSeparator = !withSeparator;
        if (noSeparator) {
            mSeparator = DEFAULT_NUMBER_SEPARATOR;
            pattern = null;
        }
        return this;
    }

    public boolean hasNoSeparator() {
        return noSeparator;
    }

    /**
     * 动态更新左侧图标
     */
    public void updateLeftIcons(@NonNull int leftFocus, @NonNull int leftUnFocus) {
        if (!isAllowUpdate) return;
        mLeftDrawableFocus = AppCompatDrawableManager.get().getDrawable(mContext, leftFocus);
        mLeftDrawableUnFocus = AppCompatDrawableManager.get().getDrawable(mContext, leftUnFocus);
        setBounds(mLeftDrawableFocus, mLeftDrawableUnFocus);
    }

    public void setOnSunlandTextChangeListener(OnSunlandTextChangeListener listener) {
        this.mTextChangeListener = listener;
    }

    public void setOnTextNonNullListener(OnTextNonNullListener listener) {
        this.mTextNonNullListener = listener;
    }

    public interface OnSunlandTextChangeListener {
        void beforeTextChanged(CharSequence s, int start, int count, int after);

        void onTextChanged(CharSequence s, int start, int before, int count);

        void afterTextChanged(Editable s);
    }

    public interface OnTextNonNullListener {
        void onTextNonNull(boolean isLimit, int limit);
    }
    /* ==== 提供给开发者回调的方法 end ==== */
}