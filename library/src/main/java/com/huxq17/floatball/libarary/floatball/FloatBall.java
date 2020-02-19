package com.huxq17.floatball.libarary.floatball;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.huxq17.floatball.libarary.FloatBallManager;
import com.huxq17.floatball.libarary.FloatBallUtil;
import com.huxq17.floatball.libarary.runner.ICarrier;
import com.huxq17.floatball.libarary.runner.OnceRunnable;
import com.huxq17.floatball.libarary.runner.ScrollRunner;
import com.huxq17.floatball.libarary.utils.Constants;
import com.huxq17.floatball.libarary.utils.LogUtils;
import com.huxq17.floatball.libarary.utils.MotionVelocityUtil;


public class FloatBall extends FrameLayout implements ICarrier {

    private static final String TAG = FloatBall.class.getSimpleName();
    private FloatBallManager floatBallManager;
    private ImageView imageView;
    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager windowManager;
    private boolean isFirst = true;
    private boolean isAdded = false;
    private int mTouchSlop;
    /**
     * flag a touch is click event
     */
    private boolean isClick;
    private int mDownX, mDownY, mLastX, mLastY;
    private int mSize;
    private ScrollRunner mRunner;
    private int mVelocityX, mVelocityY;
    private MotionVelocityUtil mVelocity;
    private boolean sleep = false;
    private FloatBallCfg mConfig;
    private boolean mHideHalfLater = true;
    private boolean mLayoutChanged = false;
    private int mSleepX = -1;
    //    private boolean isLocationServiceEnable;
    private OnceRunnable mSleepRunnable = new OnceRunnable() {
        @Override
        public void onRun() {
            if (mHideHalfLater && !sleep && isAdded) {
                sleep = true;
                moveToEdge(false, sleep);
                mSleepX = mLayoutParams.x;
            }
        }
    };

    public FloatBall(Context context, FloatBallManager floatBallManager, FloatBallCfg config) {
        super(context);
        this.floatBallManager = floatBallManager;
        mConfig = config;

        init(context);
        resetPoint();
    }

    private void init(Context context) {
        imageView = new ImageView(context);
        final Drawable icon = mConfig.mIcon;
        mSize = mConfig.mSize;
        imageView.setBackgroundDrawable(icon);
        addView(imageView, new ViewGroup.LayoutParams(mSize, mSize));
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
        this.windowManager = windowManager;
        if (!isAdded) {
            windowManager.addView(this, mLayoutParams);
            isAdded = true;
        }
    }

    public void detachFromWindow(WindowManager windowManager) {
        this.windowManager = null;
        if (isAdded) {
            removeSleepRunnable();
            if (getContext() instanceof Activity) {
                windowManager.removeViewImmediate(this);
            } else {
                windowManager.removeView(this);
            }
            isAdded = false;
            sleep = false;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();

        int curX = mLayoutParams.x;
        if (sleep && curX != mSleepX && !mRunner.isRunning()) {
            sleep = false;
            postSleepRunnable();
        }
        if (mRunner.isRunning()) {
            mLayoutChanged = false;
        }
        if (height != 0 && isFirst || mLayoutChanged) {
            if (isFirst && height != 0) {
                location(width, height);
            } else {
                moveToEdge(false, sleep);
            }
            isFirst = false;
            mLayoutChanged = false;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        floatBallManager.floatballX = mLayoutParams.x;
        floatBallManager.floatballY = mLayoutParams.y;
    }

    private void location(int width, int height) {
        FloatBallCfg.Gravity cfgGravity = mConfig.mGravity;
        mHideHalfLater = mConfig.mHideHalfLater;
        int gravity = cfgGravity.getGravity();
        int x;
        int y;
        int topLimit = 0;
        int bottomLimit = floatBallManager.mScreenHeight - height;
        int statusBarHeight = floatBallManager.getStatusBarHeight();
        if ((gravity & Gravity.LEFT) == Gravity.LEFT) {
            x = 0 - Constants.FLOAT_SHADOW_WIDTH;
        } else {
            x = floatBallManager.mScreenWidth - width + Constants.FLOAT_SHADOW_WIDTH;
        }
        if ((gravity & Gravity.TOP) == Gravity.TOP) {
            y = topLimit - Constants.FLOAT_SHADOW_HEIGHT;
        } else if ((gravity & Gravity.BOTTOM) == Gravity.BOTTOM) {
            y = floatBallManager.mScreenHeight - height - statusBarHeight + Constants.FLOAT_SHADOW_HEIGHT;
        } else {
            y = floatBallManager.mScreenHeight / 2 - height / 2 - statusBarHeight;
        }
        y = mConfig.mOffsetY != 0 ? y + mConfig.mOffsetY : y;
        if (y < 0) y = topLimit;
        if (y > bottomLimit)
            y = topLimit;
        onLocation(x, y);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mLayoutChanged = true;
        floatBallManager.onConfigurationChanged(newConfig);
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
        isClick = true;
        removeSleepRunnable();
    }

    private void touchMove(int x, int y) {
        int totalDeltaX = x - mDownX;
        int totalDeltaY = y - mDownY;
        int deltaX = x - mLastX;
        int deltaY = y - mLastY;
        if (Math.abs(totalDeltaX) > mTouchSlop || Math.abs(totalDeltaY) > mTouchSlop) {
            isClick = false;
        }
        mLastX = x;
        mLastY = y;
        if (!isClick) {
            onMove(deltaX, deltaY);
        }
    }

    private void touchUp() {
        mVelocity.computeCurrentVelocity();
        mVelocityX = (int) mVelocity.getXVelocity();
        mVelocityY = (int) mVelocity.getYVelocity();
        mVelocity.releaseVelocityTracker();
        if (sleep) {
            wakeUp();
        } else {
            if (isClick) {
                onClick();
            } else {
                moveToEdge(true, false);
            }
        }
        mVelocityX = 0;
        mVelocityY = 0;
    }

    /**
     * @param smooth 是否慢慢移动
     * @param destX  移动到的x坐标
     */
    private void moveToX(boolean smooth, int destX) {
        int statusBarHeight = floatBallManager.getStatusBarHeight();
        final int screenHeight = floatBallManager.mScreenHeight - statusBarHeight;
        int height = getHeight();
        int destY = 0;
        if (mLayoutParams.y < 0) {
            destY = 0 - mLayoutParams.y - Constants.FLOAT_SHADOW_HEIGHT;
        } else if (mLayoutParams.y > screenHeight - height) {
            destY = screenHeight - height - mLayoutParams.y + Constants.FLOAT_SHADOW_HEIGHT;
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

    private void moveTo(boolean smooth, int destX, int destY) {
//        int statusBarHeight = floatBallManager.getStatusBarHeight();
//        final int screenHeight = floatBallManager.mScreenHeight - statusBarHeight;
//        int height = getHeight();
        /*int destY = 0;
        if (mLayoutParams.y < 0) {
            destY = 0 - mLayoutParams.y;
        } else if (mLayoutParams.y > screenHeight - height) {
            destY = screenHeight - height - mLayoutParams.y;
        }*/
        if (smooth) {
            int dx = destX - mLayoutParams.x;
            int dy = destY - mLayoutParams.y;
            int duration = getScrollDuration(Math.abs(dx));
            mRunner.start(dx, dy, duration);
        } else {
            onMove(destX - mLayoutParams.x, destY);
            postSleepRunnable();
        }
    }

    private void wakeUp() {
        final int screenWidth = floatBallManager.mScreenWidth;
        int width = getWidth();
        int halfWidth = width / 2;
        int centerX = (screenWidth / 2 - halfWidth);
        int destX;
        destX = mLayoutParams.x < centerX ? 0 : screenWidth - width;
        sleep = false;
        moveToX(true, destX);
    }

    /**
     * 移动到屏幕边缘
     *
     * @param smooth
     * @param forceSleep
     */
    private void moveToEdge(boolean smooth, boolean forceSleep) {
        final int screenWidth = floatBallManager.mScreenWidth;
        int width = getWidth();
        int halfWidth = width / 2;
        int centerX = (screenWidth / 2 - halfWidth);
        int destX;
        final int minVelocity = mVelocity.getMinVelocity();
        if (mLayoutParams.x < centerX) {
//            sleep = forceSleep || Math.abs(mVelocityX) > minVelocity && mVelocityX < 0 || mLayoutParams.x < 0;
            destX = sleep ? -halfWidth : 0 - Constants.FLOAT_SHADOW_WIDTH;
        } else {
//            sleep = forceSleep || Math.abs(mVelocityX) > minVelocity && mVelocityX > 0 || mLayoutParams.x > screenWidth - width;
            destX = sleep ? screenWidth - halfWidth : screenWidth - width + Constants.FLOAT_SHADOW_WIDTH;
        }
        if (sleep) {
            mSleepX = destX;
        }
        moveToX(smooth, destX);
    }

    /**
     * 获取移动时间
     *
     * @param distance 距离
     * @return
     */
    private int getScrollDuration(int distance) {
        return (int) (250 * (1.0f * distance / 800));
    }

    private void onMove(int deltaX, int deltaY) {
        mLayoutParams.x += deltaX;
        mLayoutParams.y += deltaY;
        if (windowManager != null) {
            windowManager.updateViewLayout(this, mLayoutParams);
        }
    }

    public void onLocation(int x, int y) {
        mLayoutParams.x = x;
        mLayoutParams.y = y;
        if (windowManager != null) {
            windowManager.updateViewLayout(this, mLayoutParams);
        }
    }

    /**
     * scrollerRunner回调
     *
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
        /*if (isLocationServiceEnable) {
            LocationService locationService = ServiceAgency.getService(LocationService.class);
            locationService.onLocationChanged(mLayoutParams.x, mLayoutParams.y);
        }*/
        //判断是否展开菜单时的移动
        if (isShowMenu) {
            floatBallManager.floatballX = mLayoutParams.x;
            floatBallManager.floatballY = mLayoutParams.y;
            floatBallManager.onFloatBallClick();
            isShowMenu = false;
        }
    }

    private void moveTo(int x, int y) {
        mLayoutParams.x += x - mLayoutParams.x;
        mLayoutParams.y += y - mLayoutParams.y;
        if (windowManager != null) {
            windowManager.updateViewLayout(this, mLayoutParams);
        }
    }

    public int getSize() {
        return mSize;
    }

    /**
     * 展开之前的点的位置
     */
    private Point mPoint = new Point();
    private boolean isShowMenu = false;

    private void onClick() {
//        floatBallManager.floatballX = mLayoutParams.x;
//        floatBallManager.floatballY = mLayoutParams.y;
        /*LogUtils.d("onClick x = " + mLayoutParams.x + ",y= " + mLayoutParams.y);
        //保存数据
        mPoint.x = mLayoutParams.x;
        mPoint.y = mLayoutParams.y;
        //移动到展开的位置
        int destX = mLayoutParams.x == 0 ? mLayoutParams.x + Constants.CLICK_MOVE_DISTANCE_X : mLayoutParams.x - Constants.CLICK_MOVE_DISTANCE_X;
        moveToX(true, destX);
        isShowMenu = true;*/

        floatBallManager.onFloatBallClick();

    }

    private void removeSleepRunnable() {
        mSleepRunnable.removeSelf(this);
    }

    /**
     * 回到点击时的初始位置
     */
    public void moveToLastPostion() {
        if (mPoint.y != -1 && mPoint.x != -1) {
            moveTo(true, mPoint.x, mPoint.y);
            resetPoint();
        }
    }

    private void resetPoint() {
        mPoint.y = -1;
        mPoint.x = -1;
    }

    public void postSleepRunnable() {
        if (mHideHalfLater && !sleep && isAdded) {
            mSleepRunnable.postDelaySelf(this, 3000);
        }
    }
}
