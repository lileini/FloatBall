package com.huxq17.floatball.libarary;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

import com.huxq17.floatball.libarary.floatball.FloatBall;
import com.huxq17.floatball.libarary.floatball.FloatBallCfg;
import com.huxq17.floatball.libarary.floatball.StatusBarView;
import com.huxq17.floatball.libarary.menu.FloatAnimationLayout;
import com.huxq17.floatball.libarary.menu.FloatMenu2;
import com.huxq17.floatball.libarary.menu.FloatMenuCfg;
import com.huxq17.floatball.libarary.menu.MenuItem;

import java.util.ArrayList;
import java.util.List;


public class FloatBallManager {
    public int mScreenWidth, mScreenHeight;

    private IFloatBallPermission mPermission;
    private OnFloatBallClickListener mFloatballClickListener;
    private WindowManager mWindowManager;
    private Context mContext;
    private FloatBall mFloatball;
    private FloatMenu2 mFloatMenu;
//    private FloatAnimationLayout mFloatAnimationLayout;
    private StatusBarView mStatusBarView;
    public int floatballX, floatballY;
    private boolean isShowing = false;
    private List<MenuItem> menuItems = new ArrayList<>();
    private Activity mActivity;

    public FloatBallManager(Context application, FloatBallCfg ballCfg) {
        this(application, ballCfg, null);
    }

    public FloatBallManager(Context application, FloatBallCfg ballCfg, FloatMenuCfg menuCfg) {
        mContext = application.getApplicationContext();
        FloatBallUtil.inSingleActivity = false;
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        computeScreenSize();
        mFloatball = new FloatBall(mContext, this, ballCfg);
        mFloatMenu = new FloatMenu2(mContext, this, menuCfg);
//        mFloatAnimationLayout = new FloatAnimationLayout(mContext, this);
        mStatusBarView = new StatusBarView(mContext, this);
    }

    public FloatBallManager(Activity activity, FloatBallCfg ballCfg) {
        this(activity, ballCfg, null);
    }

    public FloatBallManager(Activity activity, FloatBallCfg ballCfg, FloatMenuCfg menuCfg) {
        mActivity = activity;
        FloatBallUtil.inSingleActivity = true;
        mWindowManager = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
        computeScreenSize();
        mFloatball = new FloatBall(mActivity, this, ballCfg);
        mFloatMenu = new FloatMenu2(mActivity, this, menuCfg);
//        mFloatAnimationLayout = new FloatAnimationLayout(mContext, this);
        mStatusBarView = new StatusBarView(mActivity, this);
    }




    public int getBallSize() {
        return mFloatball.getSize();
    }

    public void computeScreenSize() {
        Point point = new Point();
        mWindowManager.getDefaultDisplay().getSize(point);
        mScreenWidth = point.x;
        mScreenHeight = point.y;
    }

    public int getStatusBarHeight() {
        return mStatusBarView.getStatusBarHeight();
    }

    public void onStatusBarHeightChange() {
        mFloatball.onLayoutChange();
    }

    public void show() {
        if (mActivity == null) {
            if (mPermission == null) {
                return;
            }
            if (!mPermission.hasFloatBallPermission(mContext)) {
                mPermission.onRequestFloatBallPermission();
                return;
            }
        }
        if (isShowing) return;
        isShowing = true;
        mFloatball.setVisibility(View.VISIBLE);
        mStatusBarView.attachToWindow(mWindowManager);
        mFloatball.attachToWindow(mWindowManager);
        mFloatMenu.attachToWindow(mWindowManager);
    }

    public void closeMenu() {
//        mFloatMenu.closeMenu();
    }

    public void showAnimation(boolean expand) {
        if (expand){
            mFloatball.setVisibility(View.GONE);
        }/*else {
            mFloatMenu.detachFromWindow(mWindowManager);
        }*/
        mFloatMenu.showLayoutAnimation(mWindowManager,expand);
//        mFloatAnimationLayout.attachToWindow(mWindowManager,expand);
    }

    /*public void attachFloatMenu(int position){
        mFloatMenu.attachToWindow(mWindowManager,position);
    }*/

    public void reset() {
        mFloatball.setVisibility(View.VISIBLE);
        mFloatball.postSleepRunnable();
        mFloatball.moveToLastPostion();
        mFloatMenu.updateLayout(mWindowManager);
//        mFloatMenu.detachFromWindow(mWindowManager);
    }

    public void onFloatBallClick() {

        showAnimation(true);
        if (mFloatballClickListener != null) {
            mFloatballClickListener.onFloatBallClick();
        }
    }

    public void onFloatAnimationEnd(boolean expand, int position){
        if (expand){
//            attachFloatMenu(position);
        }else {
            mFloatball.setVisibility(View.VISIBLE);
        }
//        mFloatAnimationLayout.detachFromWindow(mWindowManager);
//        mFloatAnimationLayout.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        },500);

    }

    public void hide() {
        if (!isShowing) return;
        isShowing = false;
        mFloatball.detachFromWindow(mWindowManager);
        mFloatMenu.detachFromWindow(mWindowManager);
//        mFloatAnimationLayout.detachFromWindow(mWindowManager);
        mStatusBarView.detachFromWindow(mWindowManager);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        computeScreenSize();
        reset();
    }

    public void setPermission(IFloatBallPermission iPermission) {
        this.mPermission = iPermission;
    }

    public void setOnFloatBallClickListener(OnFloatBallClickListener listener) {
        mFloatballClickListener = listener;
    }

    public interface OnFloatBallClickListener {
        void onFloatBallClick();
    }

    public interface IFloatBallPermission {
        /**
         * request the permission of floatball,just use {@link #requestFloatBallPermission(Activity)},
         * or use your custom method.
         *
         * @return return true if requested the permission
         * @see #requestFloatBallPermission(Activity)
         */
        boolean onRequestFloatBallPermission();

        /**
         * detect whether allow  using floatball here or not.
         *
         * @return
         */
        boolean hasFloatBallPermission(Context context);

        /**
         * request floatball permission
         */
        void requestFloatBallPermission(Activity activity);
    }
}
