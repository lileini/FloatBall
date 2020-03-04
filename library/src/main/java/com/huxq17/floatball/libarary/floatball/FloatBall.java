package com.huxq17.floatball.libarary.floatball;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

//import com.buyi.huxq17.serviceagency.ServiceAgency;
//import com.buyi.huxq17.serviceagency.exception.AgencyException;
import com.huxq17.floatball.libarary.FloatBallManager;
import com.huxq17.floatball.libarary.FloatBallUtil;
//import com.huxq17.floatball.libarary.LocationService;
import com.huxq17.floatball.libarary.runner.ICarrier;
import com.huxq17.floatball.libarary.runner.OnceRunnable;
import com.huxq17.floatball.libarary.runner.ScrollRunner;
import com.huxq17.floatball.libarary.utils.MotionVelocityUtil;
import com.huxq17.floatball.libarary.utils.Util;


public class FloatBall extends FrameLayout implements ICarrier {

    private FloatBallManager mFloatBallManager;
    private ImageView mImageView;
    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager mWindowManager;
    private boolean mFirst = true;
    private boolean mAdded = false;
    private int mTouchSlop;
    /**
     * flag a touch is click event
     */
    private boolean mClick;
    private int mDownX, mDownY, mLastX, mLastY;
    private int mSize;
    private ScrollRunner mRunner;
    private int mVelocityX, mVelocityY;
    private MotionVelocityUtil mVelocity;
    private boolean mSleep = false;
    private FloatBallCfg mConfig;
    private boolean mHideHalfLater = true;
    private boolean mLayoutChanged = false;
    private int mSleepX = -1;
//    private boolean mLocationServiceEnable;
    private OnceRunnable mSleepRunnable = new OnceRunnable() {
        @Override
        public void onRun() {
            if (mHideHalfLater && !mSleep && mAdded) {
                mSleep = true;
                moveToEdge(false, mSleep);
                mSleepX = mLayoutParams.x;
            }
        }
    };

    public FloatBall(Context context, FloatBallManager floatBallManager, FloatBallCfg config) {
        super(context);
        this.mFloatBallManager = floatBallManager;
        mConfig = config;
        /*try {
            ServiceAgency.getService(LocationService.class);
            mLocationServiceEnable = true;
        } catch (AgencyException e) {
            mLocationServiceEnable = false;
        }*/
        init(context);
    }

    private void init(Context context) {
        mImageView = new ImageView(context);
        final Drawable icon = mConfig.mIcon;
        mSize = mConfig.mSize;
        Util.setBackground(mImageView, icon);
        addView(mImageView, new ViewGroup.LayoutParams(mSize, mSize));
        initLayoutParams(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mRunner = new ScrollRunner(this);
        mVelocity = new MotionVelocityUtil(context);
    }

    private void initLayoutParams(Context context) {
        mLayoutParams = FloatBallUtil.getLayoutParams(context);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            onConfigurationChanged(null);
        }
    }

    public void attachToWindow(WindowManager windowManager) {
        this.mWindowManager = windowManager;
        if (!mAdded) {
            windowManager.addView(this, mLayoutParams);
            mAdded = true;
        }
    }

    public void detachFromWindow(WindowManager windowManager) {
        this.mWindowManager = null;
        if (mAdded) {
            removeSleepRunnable();
            if (getContext() instanceof Activity) {
                windowManager.removeViewImmediate(this);
            } else {
                windowManager.removeView(this);
            }
            mAdded = false;
            mSleep = false;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();

        int curX = mLayoutParams.x;
        if (mSleep && curX != mSleepX && !mRunner.isRunning()) {
            mSleep = false;
            postSleepRunnable();
        }
        if (mRunner.isRunning()) {
            mLayoutChanged = false;
        }
        if (height != 0 && mFirst || mLayoutChanged) {
            if (mFirst && height != 0) {
                location(width, height);
            } else {
                moveToEdge(false, mSleep);
            }
            mFirst = false;
            mLayoutChanged = false;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mFloatBallManager.mFloatballX = mLayoutParams.x;
        mFloatBallManager.mFloatballY = mLayoutParams.y;
    }

    private void location(int width, int height) {
        FloatBallCfg.Gravity cfgGravity = mConfig.mGravity;
        mHideHalfLater = mConfig.mHideHalfLater;
        int gravity = cfgGravity.getGravity();
        int x;
        int y;
        int topLimit = 0;
        int bottomLimit = mFloatBallManager.mScreenHeight - height;
        int statusBarHeight = mFloatBallManager.getStatusBarHeight();
        if ((gravity & Gravity.LEFT) == Gravity.LEFT) {
            x = 0;
        } else {
            x = mFloatBallManager.mScreenWidth - width;
        }
        if ((gravity & Gravity.TOP) == Gravity.TOP) {
            y = topLimit;
        } else if ((gravity & Gravity.BOTTOM) == Gravity.BOTTOM) {
            y = mFloatBallManager.mScreenHeight - height - statusBarHeight;
        } else {
            y = (mFloatBallManager.mScreenHeight- statusBarHeight) / 2 - height / 2 ;
        }
        y = mConfig.mOffsetY != 0 ? y + mConfig.mOffsetY : y;
        if (y < 0) y = topLimit;
        if (y > bottomLimit)
            y = topLimit;
        /*if (mLocationServiceEnable) {
            LocationService locationService = ServiceAgency.getService(LocationService.class);
            int[] location = locationService.onRestoreLocation();
            if (location.length == 2) {
                int locationX = location[0];
                int locationY = location[1];
                if (locationX != -1 && locationY != -1) {
                    onLocation(locationX, locationY);
                    return;
                }
            }
        }*/
        onLocation(x, y);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mLayoutChanged = true;
        mFloatBallManager.onConfigurationChanged(newConfig);
        moveToEdge(false, false);
        postSleepRunnable();
    }

    public void onLayoutChange() {
        mLayoutChanged = true;
        requestLayout();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        mVelocity.acquireVelocityTracker(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                touchDown(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                touchUp();
                break;
        }
        return super.onTouchEvent(event);
    }

    private void touchDown(int x, int y) {
        mDownX = x;
        mDownY = y;
        mLastX = mDownX;
        mLastY = mDownY;
        mClick = true;
        removeSleepRunnable();
    }

    private void touchMove(int x, int y) {
        int totalDeltaX = x - mDownX;
        int totalDeltaY = y - mDownY;
        int deltaX = x - mLastX;
        int deltaY = y - mLastY;
        if (Math.abs(totalDeltaX) > mTouchSlop || Math.abs(totalDeltaY) > mTouchSlop) {
            mClick = false;
        }
        mLastX = x;
        mLastY = y;
        if (!mClick) {
            onMove(deltaX, deltaY);
        }
    }

    private void touchUp() {
        mVelocity.computeCurrentVelocity();
        mVelocityX = (int) mVelocity.getXVelocity();
        mVelocityY = (int) mVelocity.getYVelocity();
        mVelocity.releaseVelocityTracker();
        if (mSleep) {
            wakeUp();
        } else {
            if (mClick) {
                onClick();
            } else {
                moveToEdge(true, false);
            }
        }
        mVelocityX = 0;
        mVelocityY = 0;
    }

    /**
     *
     * @param smooth 是否慢慢移动
     * @param destX 移动到的x坐标
     */
    private void moveToX(boolean smooth, int destX) {
        int statusBarHeight = mFloatBallManager.getStatusBarHeight();
        final int screenHeight = mFloatBallManager.mScreenHeight - statusBarHeight;
        int height = getHeight();
        int destY = 0;
        if (mLayoutParams.y < 0) {
            destY = 0 - mLayoutParams.y;
        } else if (mLayoutParams.y > screenHeight - height) {
            destY = screenHeight - height - mLayoutParams.y;
        }
        if (smooth) {
            int dx = destX - mLayoutParams.x;
            int duration = getScrollDuration(Math.abs(dx));
            mRunner.start(dx, destY, duration);
        } else {
            onMove(destX - mLayoutParams.x, destY);
            postSleepRunnable();
        }
    }

    private void wakeUp() {
        final int screenWidth = mFloatBallManager.mScreenWidth;
        int width = getWidth();
        int halfWidth = width / 2;
        int centerX = (screenWidth / 2 - halfWidth);
        int destX;
        destX = mLayoutParams.x < centerX ? 0 : screenWidth - width;
        mSleep = false;
        moveToX(true, destX);
    }

    /**
     * 移动到屏幕边缘
     * @param smooth
     * @param forceSleep
     */
    private void moveToEdge(boolean smooth, boolean forceSleep) {
        final int screenWidth = mFloatBallManager.mScreenWidth;
        int width = getWidth();
        int halfWidth = width / 2;
        int centerX = (screenWidth / 2 - halfWidth);
        int destX;
        final int minVelocity = mVelocity.getMinVelocity();
        if (mLayoutParams.x < centerX) {
//            mSleep = forceSleep || Math.abs(mVelocityX) > minVelocity && mVelocityX < 0 || mLayoutParams.x < 0;
            destX = mSleep ? -halfWidth : 0;
        } else {
//            mSleep = forceSleep || Math.abs(mVelocityX) > minVelocity && mVelocityX > 0 || mLayoutParams.x > screenWidth - width;
            destX = mSleep ? screenWidth - halfWidth : screenWidth - width;
        }
        if (mSleep) {
            mSleepX = destX;
        }
        moveToX(smooth, destX);
    }

    private int getScrollDuration(int distance) {
        return (int) (250 * (1.0f * distance / 800));
    }

    private void onMove(int deltaX, int deltaY) {
        mLayoutParams.x += deltaX;
        mLayoutParams.y += deltaY;
        if (mWindowManager != null) {
            mWindowManager.updateViewLayout(this, mLayoutParams);
        }
    }

    public void onLocation(int x, int y) {
        mLayoutParams.x = x;
        mLayoutParams.y = y;
        if (mWindowManager != null) {
            mWindowManager.updateViewLayout(this, mLayoutParams);
        }
    }

    /**
     * scrollerRunner回调
     * @param lastX
     * @param lastY
     * @param curX
     * @param curY
     */
    @Override
    public void onMove(int lastX, int lastY, int curX, int curY) {
        onMove(curX - lastX, curY - lastY);
    }

    @Override
    public void onDone() {
        postSleepRunnable();
        /*if (mLocationServiceEnable) {
            LocationService locationService = ServiceAgency.getService(LocationService.class);
            locationService.onLocationChanged(mLayoutParams.x, mLayoutParams.y);
        }*/
    }

    private void moveTo(int x, int y) {
        mLayoutParams.x += x - mLayoutParams.x;
        mLayoutParams.y += y - mLayoutParams.y;
        if (mWindowManager != null) {
            mWindowManager.updateViewLayout(this, mLayoutParams);
        }
    }

    public int getSize() {
        return mSize;
    }

    private void onClick() {
        mFloatBallManager.mFloatballX = mLayoutParams.x;
        mFloatBallManager.mFloatballY = mLayoutParams.y;
        mFloatBallManager.onFloatBallClick();
    }

    private void removeSleepRunnable() {
        mSleepRunnable.removeSelf(this);
    }

    public void postSleepRunnable() {
        if (mHideHalfLater && !mSleep && mAdded) {
            mSleepRunnable.postDelaySelf(this, 3000);
        }
    }
}
