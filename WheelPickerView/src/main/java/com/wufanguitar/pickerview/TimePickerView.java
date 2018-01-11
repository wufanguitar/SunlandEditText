package com.wufanguitar.pickerview;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.wufanguitar.pickerview.callback.ICustomLayout;
import com.wufanguitar.pickerview.lib.DividerType;
import com.wufanguitar.pickerview.view.BasePickerView;
import com.wufanguitar.pickerview.view.WheelTime;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * @Author: Frank Wu
 * @Time: 2017/12/28 on 18:14
 * @Email: wu.fanguitar@163.com
 * @Description: 时间选择器
 */

public class TimePickerView extends BasePickerView implements View.OnClickListener {
    private static final String TAG_SUBMIT = "submit";
    private static final String TAG_CANCEL = "cancel";
    // 时间选择器布局
    private int mLayoutRes;
    // 显示时的外部背景色颜色，默认是灰色
    private int mBackgroundColor;
    // 自定义布局回调接口
    private ICustomLayout mICustomLayout;

    protected WheelTime mWheelTime;

    // 顶部左侧/右侧按钮
    private AppCompatButton mLeftBtn, mRightBtn;
    // 顶部左侧按钮文字
    private String mLeftBtnStr;
    // 顶部左侧按钮文字颜色
    private int mLeftBtnStrColor;
    // 顶部右侧按钮文字
    private String mRightBtnStr;
    // 顶部右侧按钮文字颜色
    private int mRightBtnStrColor;
    // 顶部左侧/右侧按钮文字大小
    private int mLeftRightBtnStrSize;

    // 顶部标题
    private AppCompatTextView mTitleTv;
    // 顶部标题文字
    private String mTitleStr;
    // 顶部标题文字颜色
    private int mTitleStrColor;
    // 顶部标题文字大小
    private int mTitleStrSize;

    // 时间选择器确认的时候回调的接口
    private OnTimeSelectListener mTimeSelectListener;
    // 每个 Item 中内容显示的位置，默认居中
    private int mGravity = Gravity.CENTER;
    // 时间显示类型（年/月/日/时/分/秒）
    private boolean[] mTimeType;

    // 滚轮背景颜色
    private int mWheelViewBgColor;
    // 顶部标题栏背景颜色
    private int mTopBarBgColor;

    // 分割线以外的文字颜色
    private int mOutTextColor;
    // 分割线之间及选中项的文字颜色
    private int mCenterTextColor;
    // 滚轮中的文字大小
    private int mContentTextSize;

    // 分割线的颜色
    private int mDividerColor;
    // 分隔线类型
    private DividerType mDividerType;

    // 当前选中时间
    private Calendar mSelectDate;
    // 开始时间
    private Calendar mStartDate;
    // 终止时间
    private Calendar mEndDate;
    // 开始年份
    private int mStartYear;
    // 结尾年份
    private int mEndYear;

    // 是否循环
    private boolean mIsLoop;
    // 是否能取消
    private boolean mCancelable;
    // 是否只显示中间的单位标签
    private boolean mIsCenterLabel;
    // 是否显示农历
    private boolean mIsLunarCalendar;

    // 条目间距倍数，默认1.6F
    private float mLineSpacingMultiplier = 1.6F;
    // 是否是对话框模式
    private boolean mIsDialog;

    private String mYearLabel, mMonthLabel, mDayLabel, mHourLabel, mMinuteLabel, mSecondLabel;
    private int mYearXOffset, mMonthXOffset, mDayXOffset, mHourXOffset, mMinuteXOffset, mSecondXOffset;

    public TimePickerView(Builder builder) {
        super(builder.mContext);
        this.mTimeSelectListener = builder.mTimeSelectListener;
        this.mGravity = builder.mGravity;
        this.mTimeType = builder.mTimeType;
        this.mRightBtnStr = builder.mRightBtnStr;
        this.mLeftBtnStr = builder.mLeftBtnStr;
        this.mTitleStr = builder.mTitleStr;
        this.mRightBtnStrColor = builder.mRightBtnStrColor;
        this.mLeftBtnStrColor = builder.mLeftBtnStrColor;
        this.mTitleStrColor = builder.mTitleStrColor;
        this.mWheelViewBgColor = builder.mWheelViewBgColor;
        this.mTopBarBgColor = builder.mTopBarBgColor;
        this.mLeftRightBtnStrSize = builder.mLeftRightBtnStrSize;
        this.mTitleStrSize = builder.mTitleStrSize;
        this.mContentTextSize = builder.mContentTextSize;
        this.mStartYear = builder.mStartYear;
        this.mEndYear = builder.mEndYear;
        this.mStartDate = builder.mStartDate;
        this.mEndDate = builder.mEndDate;
        this.mSelectDate = builder.mSelectDate;
        this.mIsLoop = builder.mIsLoop;
        this.mIsCenterLabel = builder.mIsCenterLabel;
        this.mIsLunarCalendar = builder.mIsLunarCalendar;
        this.mCancelable = builder.mCancelable;
        this.mYearLabel = builder.mYearLabel;
        this.mMonthLabel = builder.mMonthLabel;
        this.mDayLabel = builder.mDayLabel;
        this.mHourLabel = builder.mHourLabel;
        this.mMinuteLabel = builder.mMinuteLabel;
        this.mSecondLabel = builder.mSecondLabel;
        this.mYearXOffset = builder.mYearXOffset;
        this.mMonthXOffset = builder.mMonthXOffset;
        this.mDayXOffset = builder.mDayXOffset;
        this.mHourXOffset = builder.mHourXOffset;
        this.mMinuteXOffset = builder.mMinuteXOffset;
        this.mSecondXOffset = builder.mSecondXOffset;
        this.mCenterTextColor = builder.mCenterTextColor;
        this.mOutTextColor = builder.mOutTextColor;
        this.mDividerColor = builder.mDividerColor;
        this.mICustomLayout = builder.mICustomLayout;
        this.mLayoutRes = builder.mLayoutRes;
        this.mLineSpacingMultiplier = builder.mLineSpacingMultiplier;
        this.mIsDialog = builder.mIsDialog;
        this.mDividerType = builder.mDividerType;
        this.mBackgroundColor = builder.mBackgroundColor;
        this.mDecorView = builder.mDecorView;
        initView(builder.mContext);
    }

    public static class Builder {
        private static final int DEFAULT_TEXT_SIZE = 17;
        private static final float DEFAULT_LINE_SPACING_MULTIPLIER = 1.6F;
        private int mLayoutRes = R.layout.pickerview_time;
        private ICustomLayout mICustomLayout;
        private Context mContext;
        private OnTimeSelectListener mTimeSelectListener;
        // 显示类型，默认全部显示
        private boolean[] mTimeType = new boolean[]{true, true, true, true, true, true};
        private int mGravity = Gravity.CENTER;

        private String mRightBtnStr;
        private String mLeftBtnStr;
        private String mTitleStr;

        private int mRightBtnStrColor;
        private int mLeftBtnStrColor;
        private int mTitleStrColor;

        private int mWheelViewBgColor;
        private int mTopBarBgColor;

        private int mLeftRightBtnStrSize = DEFAULT_TEXT_SIZE;;
        private int mTitleStrSize = DEFAULT_TEXT_SIZE + 1;
        private int mContentTextSize = DEFAULT_TEXT_SIZE + 1;

        private Calendar mSelectDate;
        private Calendar mStartDate;
        private Calendar mEndDate;
        private int mStartYear;
        private int mEndYear;

        private boolean mIsLoop = false;
        private boolean mCancelable = true;
        private boolean mIsCenterLabel = true;
        private boolean mIsLunarCalendar = false;

        public ViewGroup mDecorView;

        private int mOutTextColor;
        private int mCenterTextColor;
        private int mDividerColor;
        private int mBackgroundColor;

        private DividerType mDividerType;

        private float mLineSpacingMultiplier = DEFAULT_LINE_SPACING_MULTIPLIER;

        private boolean mIsDialog;

        private String mYearLabel, mMonthLabel, mDayLabel,
                mHourLabel, mMinuteLabel, mSecondLabel;
        private int mYearXOffset, mMonthXOffset, mDayXOffset,
                mHourXOffset, mMinuteXOffset, mSecondXOffset;

        public Builder(Context context, OnTimeSelectListener listener) {
            this.mContext = context;
            this.mTimeSelectListener = listener;
        }

        /**
         * 设置时间显示类型（年/月/日/时/分/秒）
         */
        public Builder setTimeType(boolean[] timeType) {
            this.mTimeType = timeType;
            return this;
        }

        public Builder setGravity(int gravity) {
            this.mGravity = gravity;
            return this;
        }

        public Builder setRightBtnStr(String rightBtnStr) {
            this.mRightBtnStr = rightBtnStr;
            return this;
        }

        public Builder setDialog(boolean isDialog) {
            this.mIsDialog = isDialog;
            return this;
        }

        public Builder setLeftBtnStr(String leftBtnStr) {
            this.mLeftBtnStr = leftBtnStr;
            return this;
        }

        public Builder setTitleStr(String titleStr) {
            this.mTitleStr = titleStr;
            return this;
        }

        public Builder setRightBtnStrColor(int rightBtnStrColor) {
            this.mRightBtnStrColor = rightBtnStrColor;
            return this;
        }

        public Builder setLeftBtnStrColor(int leftBtnStrColor) {
            this.mLeftBtnStrColor = leftBtnStrColor;
            return this;
        }

        /**
         * 必须是viewgroup
         * 设置要将 pickerview 显示到的容器id
         */
        public Builder setDecorView(ViewGroup decorView) {
            this.mDecorView = decorView;
            return this;
        }

        public Builder setWheelViewBgColor(int wheelViewBgColor) {
            this.mWheelViewBgColor = wheelViewBgColor;
            return this;
        }

        public Builder setTopBarBgColor(int Color_Background_Title) {
            this.mTopBarBgColor = Color_Background_Title;
            return this;
        }

        public Builder setTitleStrColor(int titleStrColor) {
            this.mTitleStrColor = titleStrColor;
            return this;
        }

        public Builder setLeftRightBtnStrSize(int leftRightBtnStrSize) {
            this.mLeftRightBtnStrSize = leftRightBtnStrSize;
            return this;
        }

        public Builder setTitleStrSize(int titleStrSize) {
            this.mTitleStrSize = titleStrSize;
            return this;
        }

        public Builder setContentTextSize(int contentTextSize) {
            this.mContentTextSize = contentTextSize;
            return this;
        }

        /**
         * 因为系统 Calendar 的月份是从 0-11 的
         * 所以如果是调用 Calendar 的 set 方法来设置时间，月份的范围也要是从 0-11
         */
        public Builder setSelectDate(Calendar selectDate) {
            this.mSelectDate = selectDate;
            return this;
        }

        public Builder setLayoutRes(int layoutRes, ICustomLayout customLayout) {
            this.mLayoutRes = layoutRes;
            this.mICustomLayout = customLayout;
            return this;
        }

        public Builder setRange(int startYear, int endYear) {
            this.mStartYear = startYear;
            this.mEndYear = endYear;
            return this;
        }

        /**
         * 设置起始时间
         * 因为系统 Calendar 的月份是从 0-11 的,所以如果是调用 Calendar 的 set 方法来设置时间，月份的范围也要是从 0-11
         */
        public Builder setRangDate(Calendar startDate, Calendar endDate) {
            this.mStartDate = startDate;
            this.mEndDate = endDate;
            return this;
        }

        /**
         * 设置间距倍数,但是只能在1.2-2.0f之间
         */
        public Builder setLineSpacingMultiplier(float lineSpacingMultiplier) {
            this.mLineSpacingMultiplier = lineSpacingMultiplier;
            return this;
        }

        /**
         * 设置分割线的颜色
         */
        public Builder setDividerColor(int dividerColor) {
            this.mDividerColor = dividerColor;
            return this;
        }

        /**
         * 设置分割线的类型
         */
        public Builder setDividerType(DividerType dividerType) {
            this.mDividerType = dividerType;
            return this;
        }

        /**
         * 显示时的外部背景色颜色，默认是灰色
         */
        public Builder setBackgroundColor(int backgroundColor) {
            this.mBackgroundColor = backgroundColor;
            return this;
        }

        /**
         * 设置分割线之间的文字的颜色
         */
        public Builder setCenterTextColor(int centerTextColor) {
            this.mCenterTextColor = centerTextColor;
            return this;
        }

        /**
         * 设置分割线以外文字的颜色
         */
        public Builder setOutTextColor(int outTextColor) {
            this.mOutTextColor = outTextColor;
            return this;
        }

        public Builder setLoop(boolean isLoop) {
            this.mIsLoop = isLoop;
            return this;
        }

        public Builder setOutSideCancelable(boolean cancelable) {
            this.mCancelable = cancelable;
            return this;
        }

        public Builder setLunarCalendar(boolean lunarCalendar) {
            mIsLunarCalendar = lunarCalendar;
            return this;
        }

        public Builder setLabel(String yearLabel, String monthLabel, String dayLabel, String hourLabel, String minuteLabel, String secondLabel) {
            this.mYearLabel = yearLabel;
            this.mMonthLabel = monthLabel;
            this.mDayLabel = dayLabel;
            this.mHourLabel = hourLabel;
            this.mMinuteLabel = minuteLabel;
            this.mSecondLabel = secondLabel;
            return this;
        }

        /**
         * 设置 X 轴倾斜角度[ -90 , 90°]
         */
        public Builder setTimeXOffset(int yearXOffset, int monthXOffset, int dayXOffset, int hourXOffset, int minuteXOffset, int secondXOffset) {
            this.mYearXOffset = yearXOffset;
            this.mMonthXOffset = monthXOffset;
            this.mDayXOffset = dayXOffset;
            this.mHourXOffset = hourXOffset;
            this.mMinuteXOffset = minuteXOffset;
            this.mSecondXOffset = secondXOffset;
            return this;
        }

        public Builder setCenterLabel(boolean isCenterLabel) {
            this.mIsCenterLabel = isCenterLabel;
            return this;
        }

        public TimePickerView build() {
            return new TimePickerView(this);
        }
    }

    private void initView(Context context) {
        setDialogOutSideCancelable(mCancelable);
        initViews(mBackgroundColor);
        init();
        initEvents();
        if (mICustomLayout == null) {
            LayoutInflater.from(context).inflate(R.layout.pickerview_time, mContentContainer);

            // 顶部标题
            mTitleTv = (AppCompatTextView) findViewById(R.id.tv_title);

            // 顶部左侧/右侧按钮
            mLeftBtn = (AppCompatButton) findViewById(R.id.btn_left);
            mRightBtn = (AppCompatButton) findViewById(R.id.btn_right);

            mLeftBtn.setTag(TAG_CANCEL);
            mRightBtn.setTag(TAG_SUBMIT);

            mLeftBtn.setOnClickListener(this);
            mRightBtn.setOnClickListener(this);

            // 设置文字
            mLeftBtn.setText(TextUtils.isEmpty(mLeftBtnStr) ? context.getResources().getString(R.string.pickerview_cancel) : mLeftBtnStr);
            mRightBtn.setText(TextUtils.isEmpty(mRightBtnStr) ? context.getResources().getString(R.string.pickerview_submit) : mRightBtnStr);
            mTitleTv.setText(TextUtils.isEmpty(mTitleStr) ? "" : mTitleStr);

            // 设置文字颜色
            mLeftBtn.setTextColor(mLeftBtnStrColor == 0 ? DEFAULT_LEFT_RIGHT_BUTTON_NORMAL_COLOR : mLeftBtnStrColor);
            mRightBtn.setTextColor(mRightBtnStrColor == 0 ? DEFAULT_LEFT_RIGHT_BUTTON_NORMAL_COLOR : mRightBtnStrColor);
            mTitleTv.setTextColor(mTitleStrColor == 0 ? DEFAULT_TOPBAR_TITLE_STRING_COLOR : mTitleStrColor);

            // 设置文字大小
            mLeftBtn.setTextSize(mLeftRightBtnStrSize);
            mRightBtn.setTextSize(mLeftRightBtnStrSize);
            mTitleTv.setTextSize(mTitleStrSize);

            RelativeLayout rv_top_bar = (RelativeLayout) findViewById(R.id.rl_topbar);
            rv_top_bar.setBackgroundColor(mTopBarBgColor == 0 ? DEFAULT_TOPBAR_BACKGROUND_COLOR : mTopBarBgColor);
        } else {
            mICustomLayout.customLayout(LayoutInflater.from(context).inflate(mLayoutRes, mContentContainer));
        }
        // 时间转轮 自定义控件
        LinearLayout timePickerView = (LinearLayout) findViewById(R.id.time_picker);

        timePickerView.setBackgroundColor(mWheelViewBgColor == 0 ? DEFAULT_WHEEL_VIEW_BACKGROUND_COLOR : mWheelViewBgColor);

        mWheelTime = new WheelTime(timePickerView, mTimeType, mGravity, mContentTextSize);
        mWheelTime.setLunarCalendar(mIsLunarCalendar);

        if (mStartYear != 0 && mEndYear != 0 && mStartYear <= mEndYear) {
            setRange();
        }

        if (mStartDate != null && mEndDate != null) {
            if (mStartDate.getTimeInMillis() <= mEndDate.getTimeInMillis()) {
                setRangDate();
            }
        } else if (mStartDate != null && mEndDate == null) {
            setRangDate();
        } else if (mStartDate == null && mEndDate != null) {
            setRangDate();
        }

        setTime();
        mWheelTime.setLabels(mYearLabel, mMonthLabel, mDayLabel, mHourLabel, mMinuteLabel, mSecondLabel);
        mWheelTime.setTextXOffset(mYearXOffset, mMonthXOffset, mDayXOffset, mHourXOffset, mMinuteXOffset, mSecondXOffset);

        setOutSideCancelable(mCancelable);
        mWheelTime.setLoop(mIsLoop);
        mWheelTime.setDividerColor(mDividerColor);
        mWheelTime.setDividerType(mDividerType);
        mWheelTime.setLineSpacingMultiplier(mLineSpacingMultiplier);
        mWheelTime.setOutTextColor(mOutTextColor);
        mWheelTime.setCenterTextColor(mCenterTextColor);
        mWheelTime.setCenterLabel(mIsCenterLabel);
    }

    /**
     * 设置默认时间
     */
    public void setSelectDate(Calendar selectDate) {
        this.mSelectDate = selectDate;
        setTime();
    }

    /**
     * 设置可以选择的时间范围, 要在setTime之前调用才有效果
     */
    private void setRange() {
        mWheelTime.setStartYear(mStartYear);
        mWheelTime.setEndYear(mEndYear);
    }

    /**
     * 设置可以选择的时间范围, 要在setTime之前调用才有效果
     */
    private void setRangDate() {
        mWheelTime.setRangDate(mStartDate, mEndDate);
        // 如果设置了时间范围
        if (mStartDate != null && mEndDate != null) {
            // 判断一下默认时间是否设置了，或者是否在起始终止时间范围内
            if (mSelectDate == null || mSelectDate.getTimeInMillis() < mStartDate.getTimeInMillis()
                    || mSelectDate.getTimeInMillis() > mEndDate.getTimeInMillis()) {
                mSelectDate = mStartDate;
            }
        } else if (mStartDate != null) {
            // 没有设置默认选中时间,那就拿开始时间当默认时间
            mSelectDate = mStartDate;
        } else if (mEndDate != null) {
            mSelectDate = mEndDate;
        }
    }

    /**
     * 设置选中时间，默认选中当前时间
     */
    private void setTime() {
        int year, month, day, hour, minute, second;
        Calendar calendar = Calendar.getInstance();

        if (mSelectDate == null) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
            second = calendar.get(Calendar.SECOND);
        } else {
            year = mSelectDate.get(Calendar.YEAR);
            month = mSelectDate.get(Calendar.MONTH);
            day = mSelectDate.get(Calendar.DAY_OF_MONTH);
            hour = mSelectDate.get(Calendar.HOUR_OF_DAY);
            minute = mSelectDate.get(Calendar.MINUTE);
            second = mSelectDate.get(Calendar.SECOND);
        }

        mWheelTime.setPicker(year, month, day, hour, minute, second);
    }

    @Override
    public void onClick(View v) {
        String tag = (String) v.getTag();
        if (tag.equals(TAG_SUBMIT)) {
            returnData();
        }
        dismiss();
    }

    public void returnData() {
        if (mTimeSelectListener != null) {
            try {
                Date date = WheelTime.DEFAULT_DATA_FORMAT.parse(mWheelTime.getTime());
                mTimeSelectListener.onTimeSelect(date, mClickView);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public void setLunarCalendar(boolean lunar) {
        try {
            int year, month, day, hour, minute, second;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(WheelTime.DEFAULT_DATA_FORMAT.parse(mWheelTime.getTime()));
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
            second = calendar.get(Calendar.SECOND);

            mWheelTime.setLunarCalendar(lunar);
            mWheelTime.setLabels(mYearLabel, mMonthLabel, mDayLabel, mHourLabel, mMinuteLabel, mSecondLabel);
            mWheelTime.setPicker(year, month, day, hour, minute, second);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public boolean isLunarCalendar() {
        return mWheelTime.isLunarCalendar();
    }

    public interface OnTimeSelectListener {
        void onTimeSelect(Date date, View view);
    }

    @Override
    public boolean isDialog() {
        return mIsDialog;
    }
}
