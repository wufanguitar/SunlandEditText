package com.wufanguitar.pickerview.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.wufanguitar.pickerview.R;
import com.wufanguitar.pickerview.listener.OnDismissListener;
import com.wufanguitar.pickerview.utils.PickerViewAnimateUtil;

/**
 * @Author: Frank Wu
 * @Time: 2017/12/25 on 23:29
 * @Email: wu.fanguitar@163.com
 * @Description:
 */

public class BasePickerView {
    private final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM
    );

    private Context mContext;
    protected ViewGroup mContentContainer;
    // 显示 pickerview 的根View，默认是 activity 的根view
    public ViewGroup mDecorView;
    // 附加 View 的根View
    protected ViewGroup mRootView;
    // 附加 Dialog 的根View
    protected ViewGroup mDialogView;

    protected int pickerview_timebtn_nor = 0xFF057dff;
    protected int pickerview_timebtn_pre = 0xFFc2daf5;
    protected int pickerview_bg_topbar = 0xFFf5f5f5;
    protected int pickerview_topbar_title = 0xFF000000;
    protected int bgColor_default = 0xFFFFFFFF;

    private OnDismissListener mOnDismissListener;
    private boolean mIsDismissing;

    private Animation mOutAnim;
    private Animation mInAnim;
    private boolean mIsShowing;
    private int mGravity = Gravity.BOTTOM;


    private Dialog mDialog;
    // Dialog 型对话框是否能取消
    private boolean mIsCancelable;
    // 是通过哪个 View 弹出的
    protected View mClickView;

    private boolean mIsAnim = true;

    public BasePickerView(Context context) {
        this.mContext = context;
    }

    protected void initViews(int backgroudId) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        if (isDialog()) {
            // 如果是对话框模式
            mDialogView = (ViewGroup) layoutInflater.inflate(R.layout.layout_basepickerview, null, false);
            // 设置界面的背景为透明
            mDialogView.setBackgroundColor(Color.TRANSPARENT);
            // 这个是真正要加载时间选取器的父布局
            mContentContainer = (ViewGroup) mDialogView.findViewById(R.id.content_container);
            // 设置对话框 左右间距屏幕30
            this.params.leftMargin = 30;
            this.params.rightMargin = 30;
            mContentContainer.setLayoutParams(this.params);
            // 创建对话框
            createDialog();
            // 给背景设置点击事件,这样当点击内容以外的地方会关闭界面
            mDialogView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
        } else {
            // 如果只是要显示在屏幕的下方
            // mDecorView 是 activity 的根View
            if (mDecorView == null) {
                mDecorView = (ViewGroup) ((Activity) mContext).getWindow().getDecorView().findViewById(android.R.id.content);
            }
            // 将控件添加到 mDecorView 中
            mRootView = (ViewGroup) layoutInflater.inflate(R.layout.layout_basepickerview, mDecorView, false);
            mRootView.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            ));
            if (backgroudId != 0) {
                mRootView.setBackgroundColor(backgroudId);
            }
            // mRootView.setBackgroundColor(ContextCompat.getColor(context,backgroudId));
            // 这个是真正要加载时间选取器的父布局
            mContentContainer = (ViewGroup) mRootView.findViewById(R.id.content_container);
            mContentContainer.setLayoutParams(params);
        }
        setKeyBackCancelable(true);
    }

    protected void init() {
        mInAnim = getInAnimation();
        mOutAnim = getOutAnimation();
    }

    protected void initEvents() {
    }

    /**
     * @param view:   是通过哪个View弹出的
     * @param isAnim: 是否显示动画效果
     */
    public void show(View view, boolean isAnim) {
        this.mClickView = view;
        this.mIsAnim = isAnim;
        show();
    }

    public void show(boolean isAnim) {
        this.mIsAnim = isAnim;
        show();
    }

    public void show(View view) {
        this.mClickView = view;
        show();
    }


    /**
     * 添加View到根视图
     */
    public void show() {
        if (isDialog()) {
            showDialog();
        } else {
            if (isShowing()) {
                return;
            }
            mIsShowing = true;
            onAttached(mRootView);
            mRootView.requestFocus();
        }
    }


    /**
     * show 的时候调用
     */
    protected void onAttached(View view) {
        mDecorView.addView(view);
        if (mIsAnim) {
            mContentContainer.startAnimation(mInAnim);
        }
    }


    /**
     * 检测该 View 是不是已经添加到根视图
     *
     * @return 如果视图已经存在该View返回true
     */
    public boolean isShowing() {
        if (isDialog()) {
            return false;
        } else {
            return mRootView.getParent() != null || mIsShowing;
        }

    }

    public void dismiss() {
        if (isDialog()) {
            dismissDialog();
        } else {
            if (mIsDismissing) {
                return;
            }
            if (mIsAnim) {
                //消失动画
                mOutAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        dismissImmediately();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                mContentContainer.startAnimation(mOutAnim);
            } else {
                dismissImmediately();
            }
            mIsDismissing = true;
        }
    }

    public void dismissImmediately() {
        mDecorView.post(new Runnable() {
            @Override
            public void run() {
                // 从根视图移除
                mDecorView.removeView(mRootView);
                mIsShowing = false;
                mIsDismissing = false;
                if (mOnDismissListener != null) {
                    mOnDismissListener.onDismiss(BasePickerView.this);
                }
            }
        });
    }

    public Animation getInAnimation() {
        int res = PickerViewAnimateUtil.getAnimationResource(this.mGravity, true);
        return AnimationUtils.loadAnimation(mContext, res);
    }

    public Animation getOutAnimation() {
        int res = PickerViewAnimateUtil.getAnimationResource(this.mGravity, false);
        return AnimationUtils.loadAnimation(mContext, res);
    }

    public BasePickerView setOnDismissListener(OnDismissListener onDismissListener) {
        this.mOnDismissListener = onDismissListener;
        return this;
    }

    public void setKeyBackCancelable(boolean isCancelable) {
        ViewGroup View;
        if (isDialog()) {
            View = mDialogView;
        } else {
            View = mRootView;
        }

        View.setFocusable(isCancelable);
        View.setFocusableInTouchMode(isCancelable);
        if (isCancelable) {
            View.setOnKeyListener(onKeyBackListener);
        } else {
            View.setOnKeyListener(null);
        }
    }

    protected View.OnKeyListener onKeyBackListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == MotionEvent.ACTION_DOWN
                    && isShowing()) {
                dismiss();
                return true;
            }
            return false;
        }
    };

    protected BasePickerView setOutSideCancelable(boolean isCancelable) {
        if (mRootView != null) {
            View view = mRootView.findViewById(R.id.outmost_container);

            if (isCancelable) {
                view.setOnTouchListener(onCancelableTouchListener);
            } else {
                view.setOnTouchListener(null);
            }
        }
        return this;
    }

    /**
     * 设置对话框模式是否可以点击外部取消
     *
     * @param cancelable
     */
    public void setDialogOutSideCancelable(boolean cancelable) {
        this.mIsCancelable = cancelable;
        if (mDialog != null) {
            mDialog.setCancelable(cancelable);
        }
    }

    /**
     * Called when the user touch on black overlay in order to dismiss the dialog
     */
    protected final View.OnTouchListener onCancelableTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                dismiss();
            }
            return false;
        }
    };

    public View findViewById(int id) {
        return mContentContainer.findViewById(id);
    }

    public void createDialog() {
        if (mDialogView != null) {
            mDialog = new Dialog(mContext, R.style.custom_dialog2);
            // 不能点外面取消，也不能点 back 取消
            mDialog.setCancelable(mIsCancelable);
            mDialog.setContentView(mDialogView);
            mDialog.getWindow().setWindowAnimations(R.style.pickerview_dialogAnim);
            mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (mOnDismissListener != null) {
                        mOnDismissListener.onDismiss(BasePickerView.this);
                    }
                }
            });
        }
    }

    public void showDialog() {
        if (mDialog != null) {
            mDialog.show();
        }
    }

    public void dismissDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    public boolean isDialog() {
        return false;
    }
}
