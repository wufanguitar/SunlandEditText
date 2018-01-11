package com.wufanguitar.pickerview.view;

import android.view.View;

import com.wufanguitar.pickerview.R;
import com.wufanguitar.pickerview.adapter.ArrayWheelAdapter;
import com.wufanguitar.pickerview.adapter.NumericWheelAdapter;
import com.wufanguitar.pickerview.lib.DividerType;
import com.wufanguitar.pickerview.lib.WheelView;
import com.wufanguitar.pickerview.listener.OnItemSelectedListener;
import com.wufanguitar.pickerview.utils.ChinaDate;
import com.wufanguitar.pickerview.utils.LunarCalendar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * @Author: Frank Wu
 * @Time: 2017/12/27 on 23:11
 * @Email: wu.fanguitar@163.com
 * @Description:
 */

public class WheelTime {
    private static final int DEFAULT_START_YEAR = 1900;
    private static final int DEFAULT_END_YEAR = 2100;
    private static final int DEFAULT_START_MONTH = 1;
    private static final int DEFAULT_END_MONTH = 12;
    private static final int DEFAULT_START_DAY = 1;
    private static final int DEFAULT_END_DAY = 31;
    public static final DateFormat DEFAULT_DATA_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private View mView;
    private WheelView mYearWheelView;
    private WheelView mMonthWheelView;
    private WheelView mDayWheelView;
    private WheelView mHourWheelView;
    private WheelView mMinuteWheelView;
    private WheelView mSecondWheelView;
    private int mGravity;

    private boolean[] mType;

    private int mStartYear = DEFAULT_START_YEAR;
    private int mEndYear = DEFAULT_END_YEAR;
    private int mStartMonth = DEFAULT_START_MONTH;
    private int mEndMonth = DEFAULT_END_MONTH;
    private int mStartDay = DEFAULT_START_DAY;
    private int mEndDay = DEFAULT_END_DAY; // 表示31天的
    private int mCurrentYear;

    // 根据屏幕密度来指定选择器字体的大小(不同屏幕可能不同)
    private int mTextSize = 18;
    // 文字的颜色和分割线的颜色
    int mOutTextColor;
    int mCenterTextColor;
    int mDividerColor;
    // 条目间距倍数
    float mLineSpacingMultiplier = 1.6F;

    private DividerType mDividerType;

    private boolean mIsLunarCalendar = false;

    public WheelTime(View view) {
        super();
        this.mView = view;
        mType = new boolean[]{true, true, true, true, true, true};
        setView(view);
    }

    public WheelTime(View view, boolean[] type, int gravity, int textSize) {
        super();
        this.mView = view;
        this.mType = type;
        this.mGravity = gravity;
        this.mTextSize = textSize;
        setView(view);
    }


    public void setLunarCalendar(boolean isLunarCalendar) {
        this.mIsLunarCalendar = isLunarCalendar;
    }

    public boolean isLunarCalendar() {
        return mIsLunarCalendar;
    }

    public void setPicker(int year, int month, int day) {
        this.setPicker(year, month, day, 0, 0, 0);
    }

    public void setPicker(int year, final int month, int day, int hour, int minute, int second) {
        if (mIsLunarCalendar) {
            int[] lunar = LunarCalendar.solarToLunar(year, month + 1, day);
            setLunar(lunar[0], lunar[1], lunar[2], lunar[3] == 1, hour, minute, second);
        } else {
            setSolar(year, month, day, hour, minute, second);
        }
    }

    /**
     * 设置农历
     */
    private void setLunar(int year, final int month, int day, boolean isLeap, int hour, int minute, int second) {
        // 年
        mYearWheelView = (WheelView) mView.findViewById(R.id.year);
        mYearWheelView.setAdapter(new ArrayWheelAdapter(ChinaDate.getYears(mStartYear, mEndYear))); // 设置“年”的显示数据
        mYearWheelView.setLabel(""); // 添加文字
        mYearWheelView.setCurrentItem(year - mStartYear); // 初始化时显示的数据
        mYearWheelView.setGravity(mGravity);

        // 月
        mMonthWheelView = (WheelView) mView.findViewById(R.id.month);
        mMonthWheelView.setAdapter(new ArrayWheelAdapter(ChinaDate.getMonths(year)));
        mMonthWheelView.setLabel("");
        mMonthWheelView.setCurrentItem(month);
        mMonthWheelView.setGravity(mGravity);

        // 日
        mDayWheelView = (WheelView) mView.findViewById(R.id.day);
        // 判断大小月及是否闰年，用来确定“日”的数据
        if (ChinaDate.leapMonth(year) == 0) {
            mDayWheelView.setAdapter(new ArrayWheelAdapter(ChinaDate.getLunarDays(ChinaDate.monthDays(year, month))));
        } else {
            mDayWheelView.setAdapter(new ArrayWheelAdapter(ChinaDate.getLunarDays(ChinaDate.leapDays(year))));
        }
        mDayWheelView.setLabel("");
        mDayWheelView.setCurrentItem(day - 1);
        mDayWheelView.setGravity(mGravity);

        mHourWheelView = (WheelView) mView.findViewById(R.id.hour);
        mHourWheelView.setAdapter(new NumericWheelAdapter(0, 23));
        mHourWheelView.setCurrentItem(hour);
        mHourWheelView.setGravity(mGravity);

        mMinuteWheelView = (WheelView) mView.findViewById(R.id.min);
        mMinuteWheelView.setAdapter(new NumericWheelAdapter(0, 59));
        mMinuteWheelView.setCurrentItem(minute);
        mMinuteWheelView.setGravity(mGravity);

        mSecondWheelView = (WheelView) mView.findViewById(R.id.second);
        mSecondWheelView.setAdapter(new NumericWheelAdapter(0, 59));
        mSecondWheelView.setCurrentItem(minute);
        mSecondWheelView.setGravity(mGravity);

        // 添加“年”监听
        OnItemSelectedListener wheelListener_year = new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                int year_num = index + mStartYear;
                // 判断是不是闰年，来确定月和日的选择
                mMonthWheelView.setAdapter(new ArrayWheelAdapter(ChinaDate.getMonths(year_num)));
                if (ChinaDate.leapMonth(year_num) != 0 && mMonthWheelView.getCurrentItem() > ChinaDate.leapMonth(year_num) - 1) {
                    mMonthWheelView.setCurrentItem(mMonthWheelView.getCurrentItem() + 1);
                } else {
                    mMonthWheelView.setCurrentItem(mMonthWheelView.getCurrentItem());
                }

                int maxItem = 29;
                if (ChinaDate.leapMonth(year_num) != 0 && mMonthWheelView.getCurrentItem() > ChinaDate.leapMonth(year_num) - 1) {
                    if (mMonthWheelView.getCurrentItem() == ChinaDate.leapMonth(year_num) + 1) {
                        mDayWheelView.setAdapter(new ArrayWheelAdapter(ChinaDate.getLunarDays(ChinaDate.leapDays(year_num))));
                        maxItem = ChinaDate.leapDays(year_num);
                    } else {
                        mDayWheelView.setAdapter(new ArrayWheelAdapter(ChinaDate.getLunarDays(ChinaDate.monthDays(year_num, mMonthWheelView.getCurrentItem()))));
                        maxItem = ChinaDate.monthDays(year_num, mMonthWheelView.getCurrentItem());
                    }
                } else {
                    mDayWheelView.setAdapter(new ArrayWheelAdapter(ChinaDate.getLunarDays(ChinaDate.monthDays(year_num, mMonthWheelView.getCurrentItem() + 1))));
                    maxItem = ChinaDate.monthDays(year_num, mMonthWheelView.getCurrentItem() + 1);
                }

                if (mDayWheelView.getCurrentItem() > maxItem - 1) {
                    mDayWheelView.setCurrentItem(maxItem - 1);
                }
            }
        };
        // 添加"“月”监听
        OnItemSelectedListener wheelListener_month = new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                int month_num = index;
                int year_num = mYearWheelView.getCurrentItem() + mStartYear;
                int maxItem = 29;
                if (ChinaDate.leapMonth(year_num) != 0 && month_num > ChinaDate.leapMonth(year_num) - 1) {
                    if (mMonthWheelView.getCurrentItem() == ChinaDate.leapMonth(year_num) + 1) {
                        mDayWheelView.setAdapter(new ArrayWheelAdapter(ChinaDate.getLunarDays(ChinaDate.leapDays(year_num))));
                        maxItem = ChinaDate.leapDays(year_num);
                    } else {
                        mDayWheelView.setAdapter(new ArrayWheelAdapter(ChinaDate.getLunarDays(ChinaDate.monthDays(year_num, month_num))));
                        maxItem = ChinaDate.monthDays(year_num, month_num);
                    }
                } else {
                    mDayWheelView.setAdapter(new ArrayWheelAdapter(ChinaDate.getLunarDays(ChinaDate.monthDays(year_num, month_num + 1))));
                    maxItem = ChinaDate.monthDays(year_num, month_num + 1);
                }

                if (mDayWheelView.getCurrentItem() > maxItem - 1) {
                    mDayWheelView.setCurrentItem(maxItem - 1);
                }

            }
        };
        mYearWheelView.setOnItemSelectedListener(wheelListener_year);
        mMonthWheelView.setOnItemSelectedListener(wheelListener_month);

        if (mType.length != 6) {
            throw new RuntimeException("mType[] length is not 6");
        }
        mYearWheelView.setVisibility(mType[0] ? View.VISIBLE : View.GONE);
        mMonthWheelView.setVisibility(mType[1] ? View.VISIBLE : View.GONE);
        mDayWheelView.setVisibility(mType[2] ? View.VISIBLE : View.GONE);
        mHourWheelView.setVisibility(mType[3] ? View.VISIBLE : View.GONE);
        mMinuteWheelView.setVisibility(mType[4] ? View.VISIBLE : View.GONE);
        mSecondWheelView.setVisibility(mType[5] ? View.VISIBLE : View.GONE);
        setContentTextSize();
    }

    /**
     * 设置公历
     */
    private void setSolar(int year, final int month, int day, int hour, int minute, int second) {
        // 添加大小月月份并将其转换为list，方便之后的判断
        String[] months_big = {"1", "3", "5", "7", "8", "10", "12"};
        String[] months_little = {"4", "6", "9", "11"};

        final List<String> list_big = Arrays.asList(months_big);
        final List<String> list_little = Arrays.asList(months_little);

        /*  final Context context = view.getContext(); */
        mCurrentYear = year;
        // 年
        mYearWheelView = (WheelView) mView.findViewById(R.id.year);
        mYearWheelView.setAdapter(new NumericWheelAdapter(mStartYear, mEndYear)); // 设置"年"的显示数据
        /* mYearWheelView.setLabel(context.getString(R.string.pickerview_year)); // 添加文字 */
        int yea = year - mStartYear;

        mYearWheelView.setCurrentItem(year - mStartYear); // 初始化时显示的数据
        mYearWheelView.setGravity(mGravity);
        // 月
        mMonthWheelView = (WheelView) mView.findViewById(R.id.month);
        if (mStartYear == mEndYear) { // 开始年等于终止年
            mMonthWheelView.setAdapter(new NumericWheelAdapter(mStartMonth, mEndMonth));
            mMonthWheelView.setCurrentItem(month + 1 - mStartMonth);
        } else if (year == mStartYear) {
            // 起始日期的月份控制
            mMonthWheelView.setAdapter(new NumericWheelAdapter(mStartMonth, 12));
            mMonthWheelView.setCurrentItem(month + 1 - mStartMonth);
        } else if (year == mEndYear) {
            // 终止日期的月份控制
            mMonthWheelView.setAdapter(new NumericWheelAdapter(1, mEndMonth));
            mMonthWheelView.setCurrentItem(month);
        } else {
            mMonthWheelView.setAdapter(new NumericWheelAdapter(1, 12));
            mMonthWheelView.setCurrentItem(month);
        }
        /* mMonthWheelView.setLabel(context.getString(R.string.pickerview_month)); */

        mMonthWheelView.setGravity(mGravity);
        // 日
        mDayWheelView = (WheelView) mView.findViewById(R.id.day);

        if (mStartYear == mEndYear && mStartMonth == mEndMonth) {
            if (list_big.contains(String.valueOf(month + 1))) {
                if (mEndDay > 31) {
                    mEndDay = 31;
                }
                mDayWheelView.setAdapter(new NumericWheelAdapter(mStartDay, mEndDay));
            } else if (list_little.contains(String.valueOf(month + 1))) {
                if (mEndDay > 30) {
                    mEndDay = 30;
                }
                mDayWheelView.setAdapter(new NumericWheelAdapter(mStartDay, mEndDay));
            } else {
                // 闰年
                if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
                    if (mEndDay > 29) {
                        mEndDay = 29;
                    }
                    mDayWheelView.setAdapter(new NumericWheelAdapter(mStartDay, mEndDay));
                } else {
                    if (mEndDay > 28) {
                        mEndDay = 28;
                    }
                    mDayWheelView.setAdapter(new NumericWheelAdapter(mStartDay, mEndDay));
                }
            }
            mDayWheelView.setCurrentItem(day - mStartDay);
        } else if (year == mStartYear && month + 1 == mStartMonth) {
            // 起始日期的天数控制
            if (list_big.contains(String.valueOf(month + 1))) {

                mDayWheelView.setAdapter(new NumericWheelAdapter(mStartDay, 31));
            } else if (list_little.contains(String.valueOf(month + 1))) {

                mDayWheelView.setAdapter(new NumericWheelAdapter(mStartDay, 30));
            } else {
                // 闰年
                if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {

                    mDayWheelView.setAdapter(new NumericWheelAdapter(mStartDay, 29));
                } else {

                    mDayWheelView.setAdapter(new NumericWheelAdapter(mStartDay, 28));
                }
            }
            mDayWheelView.setCurrentItem(day - mStartDay);
        } else if (year == mEndYear && month + 1 == mEndMonth) {
            // 终止日期的天数控制
            if (list_big.contains(String.valueOf(month + 1))) {
                if (mEndDay > 31) {
                    mEndDay = 31;
                }
                mDayWheelView.setAdapter(new NumericWheelAdapter(1, mEndDay));
            } else if (list_little.contains(String.valueOf(month + 1))) {
                if (mEndDay > 30) {
                    mEndDay = 30;
                }
                mDayWheelView.setAdapter(new NumericWheelAdapter(1, mEndDay));
            } else {
                // 闰年
                if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
                    if (mEndDay > 29) {
                        mEndDay = 29;
                    }
                    mDayWheelView.setAdapter(new NumericWheelAdapter(1, mEndDay));
                } else {
                    if (mEndDay > 28) {
                        mEndDay = 28;
                    }
                    mDayWheelView.setAdapter(new NumericWheelAdapter(1, mEndDay));
                }
            }
            mDayWheelView.setCurrentItem(day - 1);
        } else {
            // 判断大小月及是否闰年,用来确定"日"的数据
            if (list_big.contains(String.valueOf(month + 1))) {
                mDayWheelView.setAdapter(new NumericWheelAdapter(1, 31));
            } else if (list_little.contains(String.valueOf(month + 1))) {
                mDayWheelView.setAdapter(new NumericWheelAdapter(1, 30));
            } else {
                // 闰年
                if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
                    mDayWheelView.setAdapter(new NumericWheelAdapter(1, 29));
                } else {
                    mDayWheelView.setAdapter(new NumericWheelAdapter(1, 28));
                }
            }
            mDayWheelView.setCurrentItem(day - 1);
        }

       /* mDayWheelView.setLabel(context.getString(R.string.pickerview_day)); */

        mDayWheelView.setGravity(mGravity);
        // 时
        mHourWheelView = (WheelView) mView.findViewById(R.id.hour);
        mHourWheelView.setAdapter(new NumericWheelAdapter(0, 23));
        mHourWheelView.setCurrentItem(hour);
        mHourWheelView.setGravity(mGravity);
        // 分
        mMinuteWheelView = (WheelView) mView.findViewById(R.id.min);
        mMinuteWheelView.setAdapter(new NumericWheelAdapter(0, 59));
        mMinuteWheelView.setCurrentItem(minute);
        mMinuteWheelView.setGravity(mGravity);
        // 秒
        mSecondWheelView = (WheelView) mView.findViewById(R.id.second);
        mSecondWheelView.setAdapter(new NumericWheelAdapter(0, 59));
        mSecondWheelView.setCurrentItem(second);
        mSecondWheelView.setGravity(mGravity);

        // 添加“年”监听
        OnItemSelectedListener wheelListener_year = new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                int year_num = index + mStartYear;
                mCurrentYear = year_num;
                int currentMonthItem = mMonthWheelView.getCurrentItem(); // 记录上一次的item位置
                // 判断大小月及是否闰年,用来确定"日"的数据
                if (mStartYear == mEndYear) {
                    // 重新设置月份
                    mMonthWheelView.setAdapter(new NumericWheelAdapter(mStartMonth, mEndMonth));

                    if (currentMonthItem > mMonthWheelView.getAdapter().getItemsCount() - 1) {
                        currentMonthItem = mMonthWheelView.getAdapter().getItemsCount() - 1;
                        mMonthWheelView.setCurrentItem(currentMonthItem);
                    }

                    int monthNum = currentMonthItem + mStartMonth;

                    if (mStartMonth == mEndMonth) {
                        // 重新设置日
                        setReDay(year_num, monthNum, mStartDay, mEndDay, list_big, list_little);
                    } else if (monthNum == mStartMonth) {
                        // 重新设置日
                        setReDay(year_num, monthNum, mStartDay, 31, list_big, list_little);
                    } else if (monthNum == mEndMonth) {
                        setReDay(year_num, monthNum, 1, mEndDay, list_big, list_little);
                    } else { // 重新设置日
                        setReDay(year_num, monthNum, 1, 31, list_big, list_little);
                    }
                } else if (year_num == mStartYear) { // 等于开始的年
                    // 重新设置月份
                    mMonthWheelView.setAdapter(new NumericWheelAdapter(mStartMonth, 12));

                    if (currentMonthItem > mMonthWheelView.getAdapter().getItemsCount() - 1) {
                        currentMonthItem = mMonthWheelView.getAdapter().getItemsCount() - 1;
                        mMonthWheelView.setCurrentItem(currentMonthItem);
                    }

                    int month = currentMonthItem + mStartMonth;
                    if (month == mStartMonth) {
                        // 重新设置日
                        setReDay(year_num, month, mStartDay, 31, list_big, list_little);
                    } else {
                        // 重新设置日
                        setReDay(year_num, month, 1, 31, list_big, list_little);
                    }

                } else if (year_num == mEndYear) {
                    // 重新设置月份
                    mMonthWheelView.setAdapter(new NumericWheelAdapter(1, mEndMonth));
                    if (currentMonthItem > mMonthWheelView.getAdapter().getItemsCount() - 1) {
                        currentMonthItem = mMonthWheelView.getAdapter().getItemsCount() - 1;
                        mMonthWheelView.setCurrentItem(currentMonthItem);
                    }
                    int monthNum = currentMonthItem + 1;

                    if (monthNum == mEndMonth) {
                        // 重新设置日
                        setReDay(year_num, monthNum, 1, mEndDay, list_big, list_little);
                    } else {
                        // 重新设置日
                        setReDay(year_num, monthNum, 1, 31, list_big, list_little);
                    }

                } else {
                    // 重新设置月份
                    mMonthWheelView.setAdapter(new NumericWheelAdapter(1, 12));
                    // 重新设置日
                    setReDay(year_num, mMonthWheelView.getCurrentItem() + 1, 1, 31, list_big, list_little);

                }

            }
        };
        // 添加“月”监听
        OnItemSelectedListener wheelListener_month = new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                int month_num = index + 1;

                if (mStartYear == mEndYear) {
                    month_num = month_num + mStartMonth - 1;
                    if (mStartMonth == mEndMonth) {
                        // 重新设置日
                        setReDay(mCurrentYear, month_num, mStartDay, mEndDay, list_big, list_little);
                    } else if (mStartMonth == month_num) {
                        // 重新设置日
                        setReDay(mCurrentYear, month_num, mStartDay, 31, list_big, list_little);
                    } else if (mEndMonth == month_num) {
                        setReDay(mCurrentYear, month_num, 1, mEndDay, list_big, list_little);
                    } else {
                        setReDay(mCurrentYear, month_num, 1, 31, list_big, list_little);
                    }
                } else if (mCurrentYear == mStartYear) {
                    month_num = month_num + mStartMonth - 1;
                    if (month_num == mStartMonth) {
                        // 重新设置日
                        setReDay(mCurrentYear, month_num, mStartDay, 31, list_big, list_little);
                    } else {
                        // 重新设置日
                        setReDay(mCurrentYear, month_num, 1, 31, list_big, list_little);
                    }
                } else if (mCurrentYear == mEndYear) {
                    if (month_num == mEndMonth) {
                        // 重新设置日
                        setReDay(mCurrentYear, mMonthWheelView.getCurrentItem() + 1, 1, mEndDay, list_big, list_little);
                    } else {
                        setReDay(mCurrentYear, mMonthWheelView.getCurrentItem() + 1, 1, 31, list_big, list_little);
                    }
                } else {
                    // 重新设置日
                    setReDay(mCurrentYear, month_num, 1, 31, list_big, list_little);
                }
            }
        };
        mYearWheelView.setOnItemSelectedListener(wheelListener_year);
        mMonthWheelView.setOnItemSelectedListener(wheelListener_month);
        if (mType.length != 6) {
            throw new IllegalArgumentException("type[] length is not 6");
        }
        mYearWheelView.setVisibility(mType[0] ? View.VISIBLE : View.GONE);
        mMonthWheelView.setVisibility(mType[1] ? View.VISIBLE : View.GONE);
        mDayWheelView.setVisibility(mType[2] ? View.VISIBLE : View.GONE);
        mHourWheelView.setVisibility(mType[3] ? View.VISIBLE : View.GONE);
        mMinuteWheelView.setVisibility(mType[4] ? View.VISIBLE : View.GONE);
        mSecondWheelView.setVisibility(mType[5] ? View.VISIBLE : View.GONE);
        setContentTextSize();
    }

    private void setReDay(int year_num, int monthNum, int startD, int endD, List<String> list_big, List<String> list_little) {
        int currentItem = mDayWheelView.getCurrentItem();
//        int maxItem;
        if (list_big.contains(String.valueOf(monthNum))) {
            if (endD > 31) {
                endD = 31;
            }
            mDayWheelView.setAdapter(new NumericWheelAdapter(startD, endD));
//            maxItem = endD;
        } else if (list_little.contains(String.valueOf(monthNum))) {
            if (endD > 30) {
                endD = 30;
            }
            mDayWheelView.setAdapter(new NumericWheelAdapter(startD, endD));
//            maxItem = endD;
        } else {
            if ((year_num % 4 == 0 && year_num % 100 != 0)
                    || year_num % 400 == 0) {
                if (endD > 29) {
                    endD = 29;
                }
                mDayWheelView.setAdapter(new NumericWheelAdapter(startD, endD));
//                maxItem = endD;
            } else {
                if (endD > 28) {
                    endD = 28;
                }
                mDayWheelView.setAdapter(new NumericWheelAdapter(startD, endD));
//                maxItem = endD;
            }
        }
        if (currentItem > mDayWheelView.getAdapter().getItemsCount() - 1) {
            currentItem = mDayWheelView.getAdapter().getItemsCount() - 1;
            mDayWheelView.setCurrentItem(currentItem);
        }
    }

    private void setContentTextSize() {
        mDayWheelView.setTextSize(mTextSize);
        mMonthWheelView.setTextSize(mTextSize);
        mYearWheelView.setTextSize(mTextSize);
        mHourWheelView.setTextSize(mTextSize);
        mMinuteWheelView.setTextSize(mTextSize);
        mSecondWheelView.setTextSize(mTextSize);
    }

    private void setOutTextColor() {
        mDayWheelView.setOutTextColor(mOutTextColor);
        mMonthWheelView.setOutTextColor(mOutTextColor);
        mYearWheelView.setOutTextColor(mOutTextColor);
        mHourWheelView.setOutTextColor(mOutTextColor);
        mMinuteWheelView.setOutTextColor(mOutTextColor);
        mSecondWheelView.setOutTextColor(mOutTextColor);
    }

    private void setCenterTextColor() {
        mDayWheelView.setCenterTextColor(mCenterTextColor);
        mMonthWheelView.setCenterTextColor(mCenterTextColor);
        mYearWheelView.setCenterTextColor(mCenterTextColor);
        mHourWheelView.setCenterTextColor(mCenterTextColor);
        mMinuteWheelView.setCenterTextColor(mCenterTextColor);
        mSecondWheelView.setCenterTextColor(mCenterTextColor);
    }

    private void setDividerColor() {
        mDayWheelView.setDividerColor(mDividerColor);
        mMonthWheelView.setDividerColor(mDividerColor);
        mYearWheelView.setDividerColor(mDividerColor);
        mHourWheelView.setDividerColor(mDividerColor);
        mMinuteWheelView.setDividerColor(mDividerColor);
        mSecondWheelView.setDividerColor(mDividerColor);
    }

    private void setDividerType() {
        mDayWheelView.setDividerType(mDividerType);
        mMonthWheelView.setDividerType(mDividerType);
        mYearWheelView.setDividerType(mDividerType);
        mHourWheelView.setDividerType(mDividerType);
        mMinuteWheelView.setDividerType(mDividerType);
        mSecondWheelView.setDividerType(mDividerType);
    }

    private void setLineSpacingMultiplier() {
        mDayWheelView.setLineSpacingMultiplier(mLineSpacingMultiplier);
        mMonthWheelView.setLineSpacingMultiplier(mLineSpacingMultiplier);
        mYearWheelView.setLineSpacingMultiplier(mLineSpacingMultiplier);
        mHourWheelView.setLineSpacingMultiplier(mLineSpacingMultiplier);
        mMinuteWheelView.setLineSpacingMultiplier(mLineSpacingMultiplier);
        mSecondWheelView.setLineSpacingMultiplier(mLineSpacingMultiplier);
    }

    public void setLabels(String label_year, String label_month, String label_day, String label_hours, String label_mins, String label_seconds) {
        if (mIsLunarCalendar) {
            return;
        }
        if (label_year != null) {
            mYearWheelView.setLabel(label_year);
        } else {
            mYearWheelView.setLabel(mView.getContext().getString(R.string.pickerview_year));
        }
        if (label_month != null) {
            mMonthWheelView.setLabel(label_month);
        } else {
            mMonthWheelView.setLabel(mView.getContext().getString(R.string.pickerview_month));
        }
        if (label_day != null) {
            mDayWheelView.setLabel(label_day);
        } else {
            mDayWheelView.setLabel(mView.getContext().getString(R.string.pickerview_day));
        }
        if (label_hours != null) {
            mHourWheelView.setLabel(label_hours);
        } else {
            mHourWheelView.setLabel(mView.getContext().getString(R.string.pickerview_hours));
        }
        if (label_mins != null) {
            mMinuteWheelView.setLabel(label_mins);
        } else {
            mMinuteWheelView.setLabel(mView.getContext().getString(R.string.pickerview_minutes));
        }
        if (label_seconds != null) {
            mSecondWheelView.setLabel(label_seconds);
        } else {
            mSecondWheelView.setLabel(mView.getContext().getString(R.string.pickerview_seconds));
        }
    }

    public void setTextXOffset(int xoffset_year, int xoffset_month, int xoffset_day, int xoffset_hours, int xoffset_mins, int xoffset_seconds) {
        mDayWheelView.setTextXOffset(xoffset_year);
        mMonthWheelView.setTextXOffset(xoffset_month);
        mYearWheelView.setTextXOffset(xoffset_day);
        mHourWheelView.setTextXOffset(xoffset_hours);
        mMinuteWheelView.setTextXOffset(xoffset_mins);
        mSecondWheelView.setTextXOffset(xoffset_seconds);
    }

    /**
     * 设置是否循环滚动
     */
    public void setLoop(boolean isLoop) {
        mYearWheelView.setLoop(isLoop);
        mMonthWheelView.setLoop(isLoop);
        mDayWheelView.setLoop(isLoop);
        mHourWheelView.setLoop(isLoop);
        mMinuteWheelView.setLoop(isLoop);
        mSecondWheelView.setLoop(isLoop);
    }

    public String getTime() {
        if (mIsLunarCalendar) {
            // 如果是农历，返回对应的公历时间
            return getLunarTime();
        }
        StringBuffer sb = new StringBuffer();
        if (mCurrentYear == mStartYear) {
           /* int i = mMonthWheelView.getCurrentItem() + startMonth;
            System.out.println("i:" + i); */
            if ((mMonthWheelView.getCurrentItem() + mStartMonth) == mStartMonth) {
                sb.append((mYearWheelView.getCurrentItem() + mStartYear)).append("-")
                        .append((mMonthWheelView.getCurrentItem() + mStartMonth)).append("-")
                        .append((mDayWheelView.getCurrentItem() + mStartDay)).append(" ")
                        .append(mHourWheelView.getCurrentItem()).append(":")
                        .append(mMinuteWheelView.getCurrentItem()).append(":")
                        .append(mSecondWheelView.getCurrentItem());
            } else {
                sb.append((mYearWheelView.getCurrentItem() + mStartYear)).append("-")
                        .append((mMonthWheelView.getCurrentItem() + mStartMonth)).append("-")
                        .append((mDayWheelView.getCurrentItem() + 1)).append(" ")
                        .append(mHourWheelView.getCurrentItem()).append(":")
                        .append(mMinuteWheelView.getCurrentItem()).append(":")
                        .append(mSecondWheelView.getCurrentItem());
            }
        } else {
            sb.append((mYearWheelView.getCurrentItem() + mStartYear)).append("-")
                    .append((mMonthWheelView.getCurrentItem() + 1)).append("-")
                    .append((mDayWheelView.getCurrentItem() + 1)).append(" ")
                    .append(mHourWheelView.getCurrentItem()).append(":")
                    .append(mMinuteWheelView.getCurrentItem()).append(":")
                    .append(mSecondWheelView.getCurrentItem());
        }
        return sb.toString();
    }

    /**
     * 农历返回对应的公历时间
     */
    private String getLunarTime() {
        StringBuffer sb = new StringBuffer();
        int year = mYearWheelView.getCurrentItem() + mStartYear;
        int month = 1;
        boolean isLeapMonth = false;
        if (ChinaDate.leapMonth(year) == 0) {
            month = mMonthWheelView.getCurrentItem() + 1;
        } else {
            if ((mMonthWheelView.getCurrentItem() + 1) - ChinaDate.leapMonth(year) <= 0) {
                month = mMonthWheelView.getCurrentItem() + 1;
            } else if ((mMonthWheelView.getCurrentItem() + 1) - ChinaDate.leapMonth(year) == 1) {
                month = mMonthWheelView.getCurrentItem();
                isLeapMonth = true;
            } else {
                month = mMonthWheelView.getCurrentItem();
            }
        }
        int day = mDayWheelView.getCurrentItem() + 1;
        int[] solar = LunarCalendar.lunarToSolar(year, month, day, isLeapMonth);

        sb.append(solar[0]).append("-")
                .append(solar[1]).append("-")
                .append(solar[2]).append(" ")
                .append(mHourWheelView.getCurrentItem()).append(":")
                .append(mMinuteWheelView.getCurrentItem()).append(":")
                .append(mSecondWheelView.getCurrentItem());
        return sb.toString();
    }

    public View getView() {
        return mView;
    }

    public void setView(View view) {
        this.mView = view;
    }

    public int getStartYear() {
        return mStartYear;
    }

    public void setStartYear(int startYear) {
        this.mStartYear = startYear;
    }

    public int getEndYear() {
        return mEndYear;
    }

    public void setEndYear(int endYear) {
        this.mEndYear = endYear;
    }

    public void setRangDate(Calendar startDate, Calendar endDate) {
        if (startDate == null && endDate != null) {
            int year = endDate.get(Calendar.YEAR);
            int month = endDate.get(Calendar.MONTH) + 1;
            int day = endDate.get(Calendar.DAY_OF_MONTH);
            if (year > mStartYear) {
                this.mEndYear = year;
                this.mEndMonth = month;
                this.mEndDay = day;
            } else if (year == mStartYear) {
                if (month > mStartMonth) {
                    this.mEndYear = year;
                    this.mEndMonth = month;
                    this.mEndDay = day;
                } else if (month == mStartMonth) {
                    if (day > mStartDay) {
                        this.mEndYear = year;
                        this.mEndMonth = month;
                        this.mEndDay = day;
                    }
                }
            }
        } else if (startDate != null && endDate == null) {
            int year = startDate.get(Calendar.YEAR);
            int month = startDate.get(Calendar.MONTH) + 1;
            int day = startDate.get(Calendar.DAY_OF_MONTH);
            if (year < mEndYear) {
                this.mStartMonth = month;
                this.mStartDay = day;
                this.mStartYear = year;
            } else if (year == mEndYear) {
                if (month < mEndMonth) {
                    this.mStartMonth = month;
                    this.mStartDay = day;
                    this.mStartYear = year;
                } else if (month == mEndMonth) {
                    if (day < mEndDay) {
                        this.mStartMonth = month;
                        this.mStartDay = day;
                        this.mStartYear = year;
                    }
                }
            }
        } else if (startDate != null && endDate != null) {
            this.mStartYear = startDate.get(Calendar.YEAR);
            this.mEndYear = endDate.get(Calendar.YEAR);
            this.mStartMonth = startDate.get(Calendar.MONTH) + 1;
            this.mEndMonth = endDate.get(Calendar.MONTH) + 1;
            this.mStartDay = startDate.get(Calendar.DAY_OF_MONTH);
            this.mEndDay = endDate.get(Calendar.DAY_OF_MONTH);
        }
    }

    /**
     * 设置间距倍数,但是只能在1.0-2.0f之间
     */
    public void setLineSpacingMultiplier(float lineSpacingMultiplier) {
        this.mLineSpacingMultiplier = lineSpacingMultiplier;
        setLineSpacingMultiplier();
    }

    /**
     * 设置分割线的颜色
     */
    public void setDividerColor(int dividerColor) {
        this.mDividerColor = dividerColor;
        setDividerColor();
    }

    /**
     * 设置分割线的类型
     */
    public void setDividerType(DividerType dividerType) {
        this.mDividerType = dividerType;
        setDividerType();
    }

    /**
     * 设置分割线之间的文字的颜色
     */
    public void setCenterTextColor(int centerTextColor) {
        this.mCenterTextColor = centerTextColor;
        setCenterTextColor();
    }

    /**
     * 设置分割线以外文字的颜色
     */
    public void setOutTextColor(int outTextColor) {
        this.mOutTextColor = outTextColor;
        setOutTextColor();
    }

    /**
     * Label 是否只显示中间选中项
     */
    public void setCenterLabel(Boolean isCenterLabel) {
        mDayWheelView.setCenterLabel(isCenterLabel);
        mMonthWheelView.setCenterLabel(isCenterLabel);
        mYearWheelView.setCenterLabel(isCenterLabel);
        mHourWheelView.setCenterLabel(isCenterLabel);
        mMinuteWheelView.setCenterLabel(isCenterLabel);
        mSecondWheelView.setCenterLabel(isCenterLabel);
    }
}
