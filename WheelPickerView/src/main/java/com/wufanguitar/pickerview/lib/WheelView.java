package com.wufanguitar.pickerview.lib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import com.wufanguitar.pickerview.R;
import com.wufanguitar.pickerview.adapter.WheelAdapter;
import com.wufanguitar.pickerview.callback.IPickerViewData;
import com.wufanguitar.pickerview.listener.OnItemSelectedListener;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Frank Wu
 * @Time: 2017/12/24 on 16:53
 * @Email: wu.fanguitar@163.com
 * @Description: 滚动控件
 */

public class WheelView extends View {
    // 修改这个值可以改变滑行速度
    private static final int DEFAULT_FLING_VELOCITY = 5;
    // 外部文字用此控制高度，压扁形成 3d 错觉
    private static final float DEFAULT_CONTENT_SCALE = 0.8F;
    // 水平方向的倾斜
    private static final float DEFAULT_TEXT_TARGET_SKEW_X = 0.5f;
    private static final int DEFAULT_OUT_TEXT_COLOR = 0xFFa8a8a8;
    private static final int DEFAULT_CENTER_TEXT_COLOR = 0xFF2a2a2a;
    private static final int DEFAULT_DIVIDER_LINE_COLOR = 0xFFd5d5d5;

    protected Context mContext;
    private Handler mHandler;
    protected ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();
    protected ScheduledFuture<?> mFuture;

    protected GestureDetector mGestureDetector;
    protected OnItemSelectedListener mOnItemSelectedListener;

    private boolean mIsOptions = false;
    private boolean mIsCenterLabel = true;

    // 外部文字画笔
    Paint mPaintOuterText;
    // 选项中的文字画笔
    Paint mPaintCenterText;
    // 分割线画笔
    Paint mPaintIndicator;
    // 分隔线类型
    protected DividerType mDividerType;

    WheelAdapter mAdapter;
    // 附加单位
    protected String mLabel;
    // 选项中的文字大小
    protected int mTextSize;
    protected int mMaxTextWidth;
    protected int mMaxTextHeight;
    protected int mTextXOffset;
    // 每行高度
    private float mItemHeight;
    // 字体样式，默认是等宽字体
    protected Typeface mTypeface = Typeface.MONOSPACE;

    protected int mOutTextColor = DEFAULT_OUT_TEXT_COLOR;
    protected int mCenterTextColor = DEFAULT_CENTER_TEXT_COLOR;
    protected int mDividerColor = DEFAULT_DIVIDER_LINE_COLOR;

    // 条目间距倍数
    protected float mLineSpacingMultiplier = 1.6F;
    // items 是否循环
    protected boolean mIsLoop;

    // 第一条线 Y 坐标值
    private float mUpLineY;
    // 第二条线 Y 坐标
    private float mDownLineY;
    // 中间 label 绘制的 Y 坐标
    private float mCenterLabelY;

    // 滚动总高度 y 值
    private float mTotalScrollY;
    // 初始化默认选中项
    private int mInitPosition;
    // 选中的 item 是第几个
    private int mSelectedItem;
    // 预选中的 item 的位置
    private int mPreCurrentIndex;
    // 滚动偏移值，用于记录滚动了多少个item
    private int mChangeItems;

    // 绘制几个条目，实际上第一项和最后一项Y轴压缩成 0% 了，所以可见的数目实际为9
    protected int mVisibleItems = 11;
    // WheelView 控件高度
    private int mMeasuredHeight;
    // WheelView 控件宽度
    private int mMeasuredWidth;

    // 半圆周长
    private int mHalfCircumference;
    // 半径
    private int mRadius;
    // item 在 Y 轴上的偏移量
    private int mOffsetY = 0;
    // 滑动时记录上一次 Y 的坐标
    private float mPreviousY = 0;
    // 记录滑动的开始时间
    private long mStartTime = 0;

    protected int mWidthMeasureSpec;

    protected int mGravity = Gravity.CENTER;
    // 中间选中文字开始绘制的位置
    protected int mDrawCenterContentStart = 0;
    // 外部文字开始绘制的位置
    protected int mDrawOutContentStart = 0;
    // 选中文字的偏移量
    private float mCenterContentOffset;

    public WheelView(Context context) {
        this(context, null);
    }

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 默认大小
        mTextSize = getResources().getDimensionPixelSize(R.dimen.pickerview_textsize);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        // 屏幕密度比（0.75/1.0/1.5/2.0/3.0）
        float density = dm.density;
        // 根据密度不同进行适配
        if (density < 1) {
            mCenterContentOffset = 2.4F;
        } else if (1 <= density && density < 2) {
            mCenterContentOffset = 3.6F;
        } else if (1 <= density && density < 2) {
            mCenterContentOffset = 4.5F;
        } else if (2 <= density && density < 3) {
            mCenterContentOffset = 6.0F;
        } else if (density >= 3) {
            mCenterContentOffset = density * 2.5F;
        }


        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.wufangutiar, 0, 0);
            mGravity = a.getInt(R.styleable.wufangutiar_pickerview_gravity, Gravity.CENTER);
            mOutTextColor = a.getColor(R.styleable.wufangutiar_pickerview_outTextColor, mOutTextColor);
            mCenterTextColor = a.getColor(R.styleable.wufangutiar_pickerview_centerTextColor, mCenterTextColor);
            mDividerColor = a.getColor(R.styleable.wufangutiar_pickerview_dividerColor, mDividerColor);
            mTextSize = a.getDimensionPixelOffset(R.styleable.wufangutiar_pickerview_textSize, mTextSize);
            mLineSpacingMultiplier = a.getFloat(R.styleable.wufangutiar_pickerview_lineSpacingMultiplier, mLineSpacingMultiplier);
            a.recycle();
        }

        judgeLineSpace();

        initLoopView(context);
    }

    /**
     * 判断间距是否在1.0-2.0之间
     */
    private void judgeLineSpace() {
        if (mLineSpacingMultiplier < 1.2f) {
            mLineSpacingMultiplier = 1.2f;
        } else if (mLineSpacingMultiplier > 2.0f) {
            mLineSpacingMultiplier = 2.0f;
        }
    }

    private void initLoopView(Context context) {
        this.mContext = context;
        mHandler = new MessageHandler(this);
        mGestureDetector = new GestureDetector(context, new WheelViewGestureListener(this));
        mGestureDetector.setIsLongpressEnabled(false);
        mIsLoop = true;

        mTotalScrollY = 0;
        mInitPosition = -1;
        initPaints();
    }

    private void initPaints() {
        mPaintOuterText = new Paint();
        mPaintOuterText.setColor(mOutTextColor);
        mPaintOuterText.setAntiAlias(true);
        mPaintOuterText.setTypeface(mTypeface);
        mPaintOuterText.setTextSize(mTextSize);

        mPaintCenterText = new Paint();
        mPaintCenterText.setColor(mCenterTextColor);
        mPaintCenterText.setAntiAlias(true);
        mPaintCenterText.setTextScaleX(1.1F);
        mPaintCenterText.setTypeface(mTypeface);
        mPaintCenterText.setTextSize(mTextSize);

        mPaintIndicator = new Paint();
        mPaintIndicator.setColor(mDividerColor);
        mPaintIndicator.setAntiAlias(true);

        if (android.os.Build.VERSION.SDK_INT >= 11) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
    }

    private void remeasure() {//重新测量
        if (mAdapter == null) {
            return;
        }

        measureTextWidthHeight();

        // 半圆的周长 = item 高度 乘以 item 数目 - 1
        mHalfCircumference = (int) (mItemHeight * (mVisibleItems - 1));
        // 整个圆的周长除以 PI 得到直径，这个直径用作控件的总高度
        mMeasuredHeight = (int) ((mHalfCircumference * 2) / Math.PI);
        // 求出半径
        mRadius = (int) (mHalfCircumference / Math.PI);
        // 控件宽度，这里支持weight
        mMeasuredWidth = MeasureSpec.getSize(mWidthMeasureSpec);
        // 计算两条横线 和 选中项画笔的基线 Y 位置
        mUpLineY = (mMeasuredHeight - mItemHeight) / 2.0F;
        mDownLineY = (mMeasuredHeight + mItemHeight) / 2.0F;
        mCenterLabelY = mDownLineY - (mItemHeight - mMaxTextHeight) / 2.0f - mCenterContentOffset;

        // 初始化显示的 item 的position
        if (mInitPosition == -1) {
            if (mIsLoop) {
                mInitPosition = (mAdapter.getItemsCount() + 1) / 2;
            } else {
                mInitPosition = 0;
            }
        }
        mPreCurrentIndex = mInitPosition;
    }

    /**
     * 计算最大 length 的 Text 的宽高度
     */
    private void measureTextWidthHeight() {
        Rect rect = new Rect();
        for (int i = 0; i < mAdapter.getItemsCount(); i++) {
            String s1 = getContentText(mAdapter.getItem(i));
            mPaintCenterText.getTextBounds(s1, 0, s1.length(), rect);
            int textWidth = rect.width();
            if (textWidth > mMaxTextWidth) {
                mMaxTextWidth = textWidth;
            }
            mPaintCenterText.getTextBounds("\u661F\u671F", 0, 2, rect); // 星期的字符编码（以它为标准高度）
            mMaxTextHeight = rect.height() + 2;
        }
        mItemHeight = mLineSpacingMultiplier * mMaxTextHeight;
    }

    // 平滑滚动的实现
    protected void smoothScroll(Action action) {
        cancelFuture();
        if (action == Action.FLING || action == Action.DAGGLE) {
            mOffsetY = (int) ((mTotalScrollY % mItemHeight + mItemHeight) % mItemHeight);
            // 如果超过 item 高度的一半，滚动到下一个 item 去
            if ((float) mOffsetY > mItemHeight / 2.0F) {
                mOffsetY = (int) (mItemHeight - (float) mOffsetY);
            } else {
                mOffsetY = -mOffsetY;
            }
        }
        //停止的时候，位置有偏移，不是全部都能正确停止到中间位置的，这里把文字位置挪回中间去
        mFuture = mExecutor.scheduleWithFixedDelay(new SmoothScrollTimerTask(this, mOffsetY), 0, 10, TimeUnit.MILLISECONDS);
    }

    protected final void scrollBy(float velocityY) {//滚动惯性的实现
        cancelFuture();
        mFuture = mExecutor.scheduleWithFixedDelay(new InertiaTimerTask(this, velocityY), 0, DEFAULT_FLING_VELOCITY, TimeUnit.MILLISECONDS);
    }

    public void cancelFuture() {
        if (mFuture != null && !mFuture.isCancelled()) {
            mFuture.cancel(true);
            mFuture = null;
        }
    }

    /**
     * 设置是否循环滚动
     *
     * @param cyclic 是否循环
     */
    public final void setCyclic(boolean cyclic) {
        mIsLoop = cyclic;
    }

    public final void setTypeface(Typeface font) {
        mTypeface = font;
        mPaintOuterText.setTypeface(mTypeface);
        mPaintCenterText.setTypeface(mTypeface);
    }

    public final void setTextSize(float size) {
        if (size > 0.0F) {
            mTextSize = (int) (mContext.getResources().getDisplayMetrics().density * size);
            mPaintOuterText.setTextSize(mTextSize);
            mPaintCenterText.setTextSize(mTextSize);
        }
    }

    public final void setCurrentItem(int currentItem) {
        // 不添加这句，当这个 wheelview 不可见时，默认都是0，会导致获取到的时间错误
        this.mSelectedItem = currentItem;
        this.mInitPosition = currentItem;
        mTotalScrollY = 0; // 回归顶部，不然重设 setCurrentItem 的话位置会偏移的，就会显示出不对位置的数据
        invalidate();
    }

    public final void setOnItemSelectedListener(OnItemSelectedListener OnItemSelectedListener) {
        this.mOnItemSelectedListener = OnItemSelectedListener;
    }

    public final OnItemSelectedListener getOnItemSelectedListener() {
        return mOnItemSelectedListener;
    }

    public final void setAdapter(WheelAdapter adapter) {
        this.mAdapter = adapter;
        remeasure();
        invalidate();
    }

    public final WheelAdapter getAdapter() {
        return mAdapter;
    }

    public final int getCurrentItem() {
        return mSelectedItem;
    }

    protected final void onItemSelected() {
        if (mOnItemSelectedListener != null) {
            postDelayed(new OnItemSelectedRunnable(this), 200L);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mAdapter == null) {
            return;
        }
        // mInitPosition 越界会造成 mPreCurrentIndex 的值不正确
        if (mInitPosition < 0) {
            mInitPosition = 0;
        }
        if (mInitPosition >= mAdapter.getItemsCount()) {
            mInitPosition = mAdapter.getItemsCount() - 1;
        }
        // 可见的 item 数组
        Object visibles[] = new Object[mVisibleItems];
        // 滚动的 Y 值高度除去每行 item 的高度，得到滚动了多少个item，即change数
        mChangeItems = (int) (mTotalScrollY / mItemHeight);

        try {
            // 滚动中实际的预选中的item(即经过了中间位置的item) ＝ 滑动前的位置 ＋ 滑动相对位置
            mPreCurrentIndex = mInitPosition + mChangeItems % mAdapter.getItemsCount();

        } catch (ArithmeticException e) {
            Log.e("WheelView", "出错了！mAdapter.getItemsCount() == 0，联动数据不匹配");
        }
        if (!mIsLoop) { // 不循环的情况
            if (mPreCurrentIndex < 0) {
                mPreCurrentIndex = 0;
            }
            if (mPreCurrentIndex > mAdapter.getItemsCount() - 1) {
                mPreCurrentIndex = mAdapter.getItemsCount() - 1;
            }
        } else { // 循环
            if (mPreCurrentIndex < 0) {
                // 举个例子：如果总数是5，mPreCurrentIndex ＝ －1，那么mPreCurrentIndex按循环来说，其实是0的上面，也就是4的位置
                mPreCurrentIndex = mAdapter.getItemsCount() + mPreCurrentIndex;
            }
            if (mPreCurrentIndex > mAdapter.getItemsCount() - 1) {
                // 同理上面,自己脑补一下
                mPreCurrentIndex = mPreCurrentIndex - mAdapter.getItemsCount();
            }
        }
        // 跟滚动流畅度有关，总滑动距离与每个item高度取余，即并不是一格格的滚动，每个item不一定滚到对应Rect里的，这个item对应格子的偏移值
        float itemHeightOffset = (mTotalScrollY % mItemHeight);

        // 设置数组中每个元素的值
        int counter = 0;
        while (counter < mVisibleItems) {
            int index = mPreCurrentIndex - (mVisibleItems / 2 - counter); // 索引值，即当前在控件中间的item看作数据源的中间，计算出相对源数据源的index值
            // 判断是否循环，如果是循环数据源也使用相对循环的position获取对应的 item 值
            // 如果不是循环则超出数据源范围使用"" 空白字符串填充，在界面上形成空白无数据的 item 项
            if (mIsLoop) {
                index = getLoopMappingIndex(index);
                visibles[counter] = mAdapter.getItem(index);
            } else if (index < 0) {
                visibles[counter] = "";
            } else if (index > mAdapter.getItemsCount() - 1) {
                visibles[counter] = "";
            } else {
                visibles[counter] = mAdapter.getItem(index);
            }
            counter++;
        }

        // 绘制中间两条横线
        if (mDividerType == DividerType.WRAP) { // 横线长度仅包裹内容
            float startX;
            float endX;
            // 隐藏 Label 的情况
            if (TextUtils.isEmpty(mLabel)) {
                startX = (mMeasuredWidth - mMaxTextWidth) / 2 - 12;
            } else {
                startX = (mMeasuredWidth - mMaxTextWidth) / 4 - 12;
            }
            // 如果超过了 WheelView 的边缘
            if (startX <= 0) {
                startX = 10;
            }
            endX = mMeasuredWidth - startX;
            canvas.drawLine(startX, mUpLineY, endX, mUpLineY, mPaintIndicator);
            canvas.drawLine(startX, mDownLineY, endX, mDownLineY, mPaintIndicator);
        } else {
            canvas.drawLine(0.0F, mUpLineY, mMeasuredWidth, mUpLineY, mPaintIndicator);
            canvas.drawLine(0.0F, mDownLineY, mMeasuredWidth, mDownLineY, mPaintIndicator);
        }

        // 只显示选中项 Label 文字的模式，并且 Label 文字不为空，则进行绘制
        if (!TextUtils.isEmpty(mLabel) && mIsCenterLabel) {
            // 绘制文字，靠右并留出空隙
            int drawRightContentStart = mMeasuredWidth - getTextWidth(mPaintCenterText, mLabel);
            canvas.drawText(mLabel, drawRightContentStart - mCenterContentOffset, mCenterLabelY, mPaintCenterText);
        }

        counter = 0;
        while (counter < mVisibleItems) {
            canvas.save();
            // 弧长 L = itemHeight * counter - itemHeightOffset
            // 求弧度 α = L / r  (弧长/半径) [0,π]
            double radian = ((mItemHeight * counter - itemHeightOffset)) / mRadius;
            // 弧度转换成角度(把半圆以Y轴为轴心向右转90度，使其处于第一象限及第四象限
            // angle [-90°,90°]
            float angle = (float) (90D - (radian / Math.PI) * 180D); // item 第一项，从90度开始，逐渐递减到 -90 度

            // 计算取值可能有细微偏差，保证负90°到90°以外的不绘制
            if (angle >= 90F || angle <= -90F) {
                canvas.restore();
            } else {
                // 根据当前角度计算出偏差系数，用以在绘制时控制文字的 水平移动 透明度 倾斜程度
                float offsetCoefficient = (float) Math.pow(Math.abs(angle) / 90f, 2.2);
                // 获取内容文字
                String contentText;

                // 如果是 label 每项都显示的模式，并且item 内容不为空、label 也不为空
                if (!mIsCenterLabel && !TextUtils.isEmpty(mLabel) && !TextUtils.isEmpty(getContentText(visibles[counter]))) {
                    contentText = getContentText(visibles[counter]) + mLabel;
                } else {
                    contentText = getContentText(visibles[counter]);
                }

                reMeasureTextSize(contentText);
                // 计算开始绘制的位置
                measuredCenterContentStart(contentText);
                measuredOutContentStart(contentText);
                float translateY = (float) (mRadius - Math.cos(radian) * mRadius - (Math.sin(radian) * mMaxTextHeight) / 2D);
                // 根据 Math.sin(radian) 来更改 canvas 坐标系原点，然后缩放画布，使得文字高度进行缩放，形成弧形 3d 视觉差
                canvas.translate(0.0F, translateY);
                // canvas.scale(1.0F, (float) Math.sin(radian));
                if (translateY <= mUpLineY && mMaxTextHeight + translateY >= mUpLineY) {
                    // 条目经过第一条线
                    canvas.save();
                    canvas.clipRect(0, 0, mMeasuredWidth, mUpLineY - translateY);
                    canvas.scale(1.0F, (float) Math.sin(radian) * DEFAULT_CONTENT_SCALE);
                    canvas.drawText(contentText, mDrawOutContentStart, mMaxTextHeight, mPaintOuterText);
                    canvas.restore();
                    canvas.save();
                    canvas.clipRect(0, mUpLineY - translateY, mMeasuredWidth, (int) (mItemHeight));
                    canvas.scale(1.0F, (float) Math.sin(radian) * 1.0F);
                    canvas.drawText(contentText, mDrawCenterContentStart, mMaxTextHeight - mCenterContentOffset, mPaintCenterText);
                    canvas.restore();
                } else if (translateY <= mDownLineY && mMaxTextHeight + translateY >= mDownLineY) {
                    // 条目经过第二条线
                    canvas.save();
                    canvas.clipRect(0, 0, mMeasuredWidth, mDownLineY - translateY);
                    canvas.scale(1.0F, (float) Math.sin(radian) * 1.0F);
                    canvas.drawText(contentText, mDrawCenterContentStart, mMaxTextHeight - mCenterContentOffset, mPaintCenterText);
                    canvas.restore();
                    canvas.save();
                    canvas.clipRect(0, mDownLineY - translateY, mMeasuredWidth, (int) (mItemHeight));
                    canvas.scale(1.0F, (float) Math.sin(radian) * DEFAULT_CONTENT_SCALE);
                    canvas.drawText(contentText, mDrawOutContentStart, mMaxTextHeight, mPaintOuterText);
                    canvas.restore();
                } else if (translateY >= mUpLineY && mMaxTextHeight + translateY <= mDownLineY) {
                    // 中间条目
                    // canvas.clipRect(0, 0, measuredWidth,   maxTextHeight);
                    // 让文字居中
                    // 因为圆弧角换算的向下取值，导致角度稍微有点偏差，加上画笔的基线会偏上，因此需要偏移量修正一下
                    float Y = mMaxTextHeight - mCenterContentOffset;
                    canvas.drawText(contentText, mDrawCenterContentStart, Y, mPaintCenterText);

                    int preSelectedItem = mAdapter.indexOf(visibles[counter]);
                    mSelectedItem = preSelectedItem;
                } else {
                    // 其他条目
                    canvas.save();
                    canvas.clipRect(0, 0, mMeasuredWidth, (int) (mItemHeight));
                    canvas.scale(1.0F, (float) Math.sin(radian) * DEFAULT_CONTENT_SCALE);
                    // 控制文字倾斜角度
                    mPaintOuterText.setTextSkewX((mTextXOffset == 0 ? 0 :
                            (mTextXOffset > 0 ? 1 : -1)) * (angle > 0 ? -1 : 1) * DEFAULT_TEXT_TARGET_SKEW_X * offsetCoefficient);
                    // 控制透明度
                    mPaintOuterText.setAlpha((int) ((1 - offsetCoefficient) * 255));
                    // 控制文字水平便宜距离
                    canvas.drawText(contentText, mDrawOutContentStart + mTextXOffset * offsetCoefficient, mMaxTextHeight, mPaintOuterText);
                    canvas.restore();
                }
                canvas.restore();
                mPaintCenterText.setTextSize(mTextSize);
            }
            counter++;
        }
    }

    /**
     * 根据文字的长度 重新设置文字的大小 让其能完全显示
     *
     * @param contentText
     */
    private void reMeasureTextSize(String contentText) {
        Rect rect = new Rect();
        mPaintCenterText.getTextBounds(contentText, 0, contentText.length(), rect);
        int width = rect.width();
        int size = mTextSize;
        while (width > mMeasuredWidth) {
            size--;
            // 设置两条横线中间的文字大小
            mPaintCenterText.setTextSize(size);
            mPaintCenterText.getTextBounds(contentText, 0, contentText.length(), rect);
            width = rect.width();
        }
        // 设置两条横线外面的文字大小
        mPaintOuterText.setTextSize(size);
    }


    // 递归计算出对应的index
    private int getLoopMappingIndex(int index) {
        if (index < 0) {
            index = index + mAdapter.getItemsCount();
            index = getLoopMappingIndex(index);
        } else if (index > mAdapter.getItemsCount() - 1) {
            index = index - mAdapter.getItemsCount();
            index = getLoopMappingIndex(index);
        }
        return index;
    }

    /**
     * 根据传进来的对象获取getPickerViewText()方法，来获取需要显示的值
     *
     * @param item 数据源的item
     * @return 对应显示的字符串
     */
    private String getContentText(Object item) {
        if (item == null) {
            return "";
        } else if (item instanceof IPickerViewData) {
            return (String) ((IPickerViewData) item).getPickerViewData();
        } else if (item instanceof Integer) {
            // 如果为整形则最少保留两位数.
            return String.format(Locale.getDefault(), "%02d", (int) item);
        }
        return item.toString();
    }

    private void measuredCenterContentStart(String content) {
        Rect rect = new Rect();
        mPaintCenterText.getTextBounds(content, 0, content.length(), rect);
        switch (mGravity) {
            case Gravity.CENTER:
                // 显示内容居中
                if (mIsOptions || mLabel == null || mLabel.equals("") || !mIsCenterLabel) {
                    mDrawCenterContentStart = (int) ((mMeasuredWidth - rect.width()) * 0.5);
                } else {
                    // 只显示中间 label 时，时间选择器内容偏左一点，留出空间绘制单位标签
                    mDrawCenterContentStart = (int) ((mMeasuredWidth - rect.width()) * 0.25);
                }
                break;
            case Gravity.LEFT:
                mDrawCenterContentStart = 0;
                break;
            case Gravity.RIGHT:
                // 添加偏移量
                mDrawCenterContentStart = mMeasuredWidth - rect.width() - (int) mCenterContentOffset;
                break;
        }
    }

    private void measuredOutContentStart(String content) {
        Rect rect = new Rect();
        mPaintOuterText.getTextBounds(content, 0, content.length(), rect);
        switch (mGravity) {
            case Gravity.CENTER:
                if (mIsOptions || mLabel == null || mLabel.equals("") || !mIsCenterLabel) {
                    mDrawOutContentStart = (int) ((mMeasuredWidth - rect.width()) * 0.5);
                } else {
                    // 只显示中间 label 时，时间选择器内容偏左一点，留出空间绘制单位标签
                    mDrawOutContentStart = (int) ((mMeasuredWidth - rect.width()) * 0.25);
                }
                break;
            case Gravity.LEFT:
                mDrawOutContentStart = 0;
                break;
            case Gravity.RIGHT:
                mDrawOutContentStart = mMeasuredWidth - rect.width() - (int) mCenterContentOffset;
                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.mWidthMeasureSpec = widthMeasureSpec;
        remeasure();
        setMeasuredDimension(mMeasuredWidth, mMeasuredHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean eventConsumed = mGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            // 按下
            case MotionEvent.ACTION_DOWN:
                mStartTime = System.currentTimeMillis();
                cancelFuture();
                mPreviousY = event.getRawY();
                break;
            // 滑动中
            case MotionEvent.ACTION_MOVE:
                float dy = mPreviousY - event.getRawY();
                mPreviousY = event.getRawY();
                mTotalScrollY = mTotalScrollY + dy;
                // 边界处理
                if (!mIsLoop) {
                    float top = -mInitPosition * mItemHeight;
                    float bottom = (mAdapter.getItemsCount() - 1 - mInitPosition) * mItemHeight;
                    if (mTotalScrollY - mItemHeight * 0.25 < top) {
                        top = mTotalScrollY - dy;
                    } else if (mTotalScrollY + mItemHeight * 0.25 > bottom) {
                        bottom = mTotalScrollY - dy;
                    }
                    if (mTotalScrollY < top) {
                        mTotalScrollY = (int) top;
                    } else if (mTotalScrollY > bottom) {
                        mTotalScrollY = (int) bottom;
                    }
                }
                break;
            // 完成滑动，手指离开屏幕
            case MotionEvent.ACTION_UP:
            default:
                if (!eventConsumed) { // 未消费掉事件
                    /**
                     * TODO<关于弧长的计算>
                     *
                     * 弧长公式： L = α*R
                     * 反余弦公式：arccos(cosα) = α
                     * 由于之前是有顺时针偏移90度，
                     * 所以实际弧度范围α2的值 ：α2 = π/2-α    （α=[0,π] α2 = [-π/2,π/2]）
                     * 根据正弦余弦转换公式 cosα = sin(π/2-α)
                     * 代入，得： cosα = sin(π/2-α) = sinα2 = (R - y) / R
                     * 所以弧长 L = arccos(cosα)*R = arccos((R - y) / R)*R
                     */
                    float y = event.getY();
                    double L = Math.acos((mRadius - y) / mRadius) * mRadius;
                    // item 0 有一半是在不可见区域，所以需要加上 itemHeight / 2
                    int circlePosition = (int) ((L + mItemHeight / 2) / mItemHeight);
                    float extraOffset = (mTotalScrollY % mItemHeight + mItemHeight) % mItemHeight;
                    // 已滑动的弧长值
                    mOffsetY = (int) ((circlePosition - mVisibleItems / 2) * mItemHeight - extraOffset);
                    if ((System.currentTimeMillis() - mStartTime) > 120) {
                        // 处理拖拽事件
                        smoothScroll(Action.DAGGLE);
                    } else {
                        // 处理条目点击事件
                        smoothScroll(Action.CLICK);
                    }
                }
                break;
        }
        invalidate();
        return true;
    }

    /**
     * 获取Item个数
     *
     * @return item个数
     */
    public int getItemsCount() {
        return mAdapter != null ? mAdapter.getItemsCount() : 0;
    }

    /**
     * 附加在右边的单位字符串
     *
     * @param label 单位
     */
    public void setLabel(String label) {
        this.mLabel = label;
    }

    public void setIsCenterLabel(Boolean isCenterLabel) {
        this.mIsCenterLabel = isCenterLabel;
    }

    public void setGravity(int gravity) {
        this.mGravity = gravity;
    }

    /**
     * 计算文字宽度
     */
    public int getTextWidth(Paint paint, String str) {
        int iRet = 0;
        if (str != null && str.length() > 0) {
            int len = str.length();
            float[] widths = new float[len];
            paint.getTextWidths(str, widths);
            for (int j = 0; j < len; j++) {
                iRet += (int) Math.ceil(widths[j]);
            }
        }
        return iRet;
    }

    public void setIsOptions(boolean options) {
        this.mIsOptions = options;
    }

    public void setOutTextColor(int outTextColor) {
        if (outTextColor != 0) {
            this.mOutTextColor = outTextColor;
            mPaintOuterText.setColor(this.mOutTextColor);
        }
    }

    public void setCenterTextColor(int centerTextColor) {
        if (centerTextColor != 0) {
            this.mCenterTextColor = centerTextColor;
            mPaintCenterText.setColor(this.mCenterTextColor);
        }
    }

    public void setTextXOffset(int textXOffset) {
        this.mTextXOffset = textXOffset;
        if (textXOffset != 0) {
            mPaintCenterText.setTextScaleX(1.0f);
        }
    }

    public void setDividerColor(int dividerColor) {
        if (dividerColor != 0) {
            this.mDividerColor = dividerColor;
            mPaintIndicator.setColor(this.mDividerColor);
        }
    }

    public void setDividerType(DividerType dividerType) {
        this.mDividerType = dividerType;
    }

    public void setLineSpacingMultiplier(float lineSpacingMultiplier) {
        if (lineSpacingMultiplier != 0) {
            this.mLineSpacingMultiplier = lineSpacingMultiplier;
            judgeLineSpace();
        }
    }

    public void setTotalScrollY(float totalScrollY) {
        this.mTotalScrollY = totalScrollY;
    }

    public float getTotalScrollY() {
        return mTotalScrollY;
    }

    public Handler getHandler() {
        return mHandler;
    }

    public boolean isLoop() {
        return mIsLoop;
    }

    public int getInitPosition() {
        return mInitPosition;
    }

    public float getItemHeight() {
        return mItemHeight;
    }
}