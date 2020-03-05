package com.chinatsp.floatball;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.chinatsp.floatball.floatball.FloatBall;
import com.chinatsp.floatball.floatball.FloatBallCfg;
import com.chinatsp.floatball.floatball.StatusBarView;
import com.chinatsp.floatball.menu.FloatMenu;
import com.chinatsp.floatball.menu.FloatMenuCfg;
import com.chinatsp.floatball.menu.MenuItem;

import java.util.ArrayList;
import java.util.List;


public class FloatBallManager {
    private static final String TAG = "FloatBallManager";
    public int mScreenWidth, mScreenHeight;

    private IFloatBallPermission mPermission;
    private OnFloatBallClickListener mFloatballClickListener;
    private WindowManager mWindowManager;
    private Context mContext;
    private FloatBall mFloatBall;
    private FloatMenu mFloatMenu;
    private StatusBarView mStatusBarView;
    public int mFloatballX;
    public int mFloatballY;
    private boolean mShowing = false;
    private List<MenuItem> mMenuItems = new ArrayList<>();
    private Activity mActivity;

    public FloatBallManager(Context application, FloatBallCfg ballCfg) {
        this(application, ballCfg, null);
    }

    public FloatBallManager(Context application, FloatBallCfg ballCfg, FloatMenuCfg menuCfg) {
        mContext = application.getApplicationContext();
        FloatBallUtil.inSingleActivity = false;
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        computeScreenSize();
        mFloatBall = new FloatBall(mContext, this, ballCfg);
        mFloatMenu = new FloatMenu(mContext, this, menuCfg);
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
        mFloatBall = new FloatBall(mActivity, this, ballCfg);
        mFloatMenu = new FloatMenu(mActivity, this, menuCfg);
        mStatusBarView = new StatusBarView(mActivity, this);
    }

    public void buildMenu() {
        inflateMenuItem();
    }

    /**
     * 添加一个菜单条目
     *
     * @param item
     */
    public FloatBallManager addMenuItem(MenuItem item) {
        mMenuItems.add(item);
        return this;
    }

    public int getMenuItemSize() {
        return mMenuItems != null ? mMenuItems.size() : 0;
    }

    /**
     * 设置菜单
     *
     * @param items
     */
    public FloatBallManager setMenu(List<MenuItem> items) {
        mMenuItems = items;
        return this;
    }

    private void inflateMenuItem() {
        mFloatMenu.removeAllItemViews();
        for (MenuItem item : mMenuItems) {
            mFloatMenu.addItem(item);
        }
    }

    public int getBallSize() {
        return mFloatBall.getSize();
    }

    public void computeScreenSize() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point point = new Point();
            mWindowManager.getDefaultDisplay().getSize(point);
            mScreenWidth = point.x;
            mScreenHeight = point.y;
        } else {
            mScreenWidth = mWindowManager.getDefaultDisplay().getWidth();
            mScreenHeight = mWindowManager.getDefaultDisplay().getHeight();
        }
    }

    public int getStatusBarHeight() {
        return mStatusBarView.getStatusBarHeight();
    }

    public void onStatusBarHeightChange() {
        mFloatBall.onLayoutChange();
    }

    public void show() {
        Log.d(TAG, "show: ");
        if (mActivity == null) {
            if (mPermission == null) {
                return;
            }
            if (!mPermission.hasFloatBallPermission(mContext)) {
                mPermission.onRequestFloatBallPermission();
                return;
            }
        }
        if (mShowing) return;
        mShowing = true;
        mFloatBall.setVisibility(View.VISIBLE);
        mStatusBarView.attachToWindow(mWindowManager);
        mFloatBall.attachToWindow(mWindowManager);
        mFloatMenu.detachFromWindow(mWindowManager);
    }

    public void closeMenu() {
        Log.d(TAG, "closeMenu: ");
        mFloatMenu.closeMenu();
    }

    public void openMenu() {
        Log.d(TAG, "openMenu: ");
        onFloatBallClick();
    }
    public void chooseMenuItem(int gravity){
        Log.d(TAG, "chooseMenuItem: ");
        if (!mFloatMenu.isExpended()) {
            Log.d(TAG, "chooseMenuItem: mFloatMenu.isExpended() = false");
            return;
        }
        mFloatMenu.chooseMenuItem(gravity);
    }

    public void reset() {
        Log.d(TAG, "reset: ");
        mFloatBall.setVisibility(View.VISIBLE);
//        mFloatBall.postSleepRunnable();
        mFloatMenu.detachFromWindow(mWindowManager);
    }

    public void onFloatBallClick() {
        if (mMenuItems != null && mMenuItems.size() > 0) {
            mFloatMenu.attachToWindow(mWindowManager);
        } else {
            if (mFloatballClickListener != null) {
                mFloatballClickListener.onFloatBallClick();
            }
        }
    }
    public void hideFloatBall(){
        mFloatBall.setVisibility(View.GONE);
    }

    public void hide() {
        if (!mShowing) return;
        mShowing = false;
        mFloatBall.detachFromWindow(mWindowManager);
        mFloatMenu.detachFromWindow(mWindowManager);
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
