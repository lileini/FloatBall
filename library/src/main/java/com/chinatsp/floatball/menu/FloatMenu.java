/*
 * Copyright (C) 2012 Capricorn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chinatsp.floatball.menu;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.chinatsp.floatball.FloatBallManager;
import com.chinatsp.floatball.FloatBallUtil;
import com.chinatsp.floatball.libarary.R;
import com.chinatsp.floatball.runner.ICarrier;
import com.chinatsp.floatball.runner.ScrollRunner;
import com.chinatsp.floatball.utils.Constants;
import com.chinatsp.floatball.utils.LogUtils;

public class FloatMenu extends FrameLayout implements ICarrier {
    private final String TAG = getClass().getSimpleName();
    private MenuLayout mMenuLayout;

    private ImageView mIconView;
    private int mPosition;
    private int mItemSize;
    private int mSize;
    private int mDuration = 250;

    private FloatBallManager mFloatBallManager;
    private WindowManager.LayoutParams mLayoutParams;
    private boolean isAdded = false;
    private int mBallSize;
    private FloatMenuCfg mConfig;
    private boolean mListenBackEvent = true;
    private WindowManager mWindowManager;
    private ScrollRunner mRunner;
    private Point mLastPoint;
    private boolean mExpended = false;

    public FloatMenu(Context context, final FloatBallManager floatBallManager, FloatMenuCfg config) {
        super(context);
        this.mFloatBallManager = floatBallManager;
        if (config == null) return;
        mConfig = config;
        mItemSize = mConfig.mItemSize;
        mSize = mConfig.mSize;
        init(context);
        mMenuLayout.setChildSize(mItemSize);

    }

    private void initLayoutParams(Context context) {
        mLayoutParams = FloatBallUtil.getLayoutParams(context, mListenBackEvent);
    }


    @SuppressWarnings("deprecation")
    public void removeViewTreeObserver(ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        } else {
            getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
    }

    public int getSize() {
        return mSize;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                if (mMenuLayout.isExpanded()) {
                    toggle(mDuration);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public void attachToWindow(WindowManager windowManager) {
        if (!isAdded) {
            mWindowManager = windowManager;
            Log.d(TAG, "attachToWindow: ");
            mBallSize = mFloatBallManager.getBallSize();
            mLayoutParams.x = mFloatBallManager.mFloatballX;
            mLayoutParams.y = mFloatBallManager.mFloatballY - mSize / 2;
            mPosition = computeMenuLayout(mLayoutParams);
            refreshPathMenu(mPosition);
            toggle(mDuration);
            windowManager.addView(this, mLayoutParams);
            isAdded = true;
        }
    }


    public void detachFromWindow(WindowManager windowManager) {
        if (isAdded) {
            Log.d(TAG, "detachFromWindow: ");
            toggle(0);
            mMenuLayout.setVisibility(GONE);
            if (getContext() instanceof Activity) {
                windowManager.removeViewImmediate(this);
            } else {
                windowManager.removeView(this);
            }
            isAdded = false;
        }
    }

    /**
     * 添加菜单布局
     *
     * @param context
     */
    private void addMenuLayout(Context context) {
        mMenuLayout = new MenuLayout(context);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(mSize, mSize);
        addView(mMenuLayout, layoutParams);
        mMenuLayout.setVisibility(INVISIBLE);
    }

    /**
     * 添加控制按钮
     *
     * @param context
     */
    private void addControllLayout(Context context) {
        mIconView = new ImageView(context);

        Log.d(TAG, "addControllLayout: mBallSize=" + mBallSize);
        mBallSize = mFloatBallManager.getBallSize();
        mIconView.setImageResource(R.drawable.ic_float_ball);
        LayoutParams sublayoutParams = new LayoutParams(mBallSize, mBallSize);
        sublayoutParams.gravity = Gravity.CENTER;
        addView(mIconView, sublayoutParams);
    }

    private void init(Context context) {
        initLayoutParams(context);
        mLayoutParams.height = mSize;
        mLayoutParams.width = mSize;
        Log.d(TAG, "init: mSize=" + mSize);
        addMenuLayout(context);
        addControllLayout(context);
        mIconView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMenu();
            }
        });
        if (mListenBackEvent) {
            setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    int action = event.getAction();
                    if (action == KeyEvent.ACTION_DOWN) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            FloatMenu.this.mFloatBallManager.closeMenu();
                            return true;
                        }
                    }
                    return false;
                }
            });
            setFocusableInTouchMode(true);
        }
        mRunner = new ScrollRunner(this);
        mLastPoint = new Point();
    }

    public void closeMenu() {
        if (mMenuLayout.isExpanded()) {
            toggle(mDuration);
        }
    }

    public boolean isExpended() {
        return mExpended;
    }

    public void remove() {
//        mFloatBallManager.reset();
//        mMenuLayout.setExpand(false);
        mIconView.setVisibility(VISIBLE);
        startTransition(false, mDuration);
    }

    /**
     * 开始展开动画
     *
     * @param duration
     */
    private void toggle(final int duration) {
        Log.d(TAG, "toggle: duration= " + duration);
        //duration==0 indicate that close the menu, so if it has closed, do nothing.
        if (!mMenuLayout.isExpanded() && duration <= 0) return;
        mMenuLayout.setVisibility(VISIBLE);
        Log.d(TAG, "toggle: getWidth() =" + getWidth());
        if (getWidth() == 0) {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mFloatBallManager.hideFloatBall();
                    Log.d(TAG, "onAnimationUpdate: getLeft() =" + mLayoutParams.x);
                    if (!mMenuLayout.isExpanded()) {
                        //先移动后展开
                        startTransition(true, duration);
                    } else {
                        //先收起后移动
                        mMenuLayout.switchState(mPosition, duration);
                    }

//                    mMenuLayout.switchState(mPosition, duration);
                    removeViewTreeObserver(this);
                }
            });
        } else {
//            mMenuLayout.switchState(mPosition, duration);

            mFloatBallManager.hideFloatBall();
            Log.d(TAG, "onAnimationUpdate: getLeft() =" + mLayoutParams.x);
            if (!mMenuLayout.isExpanded()) {
                startTransition(true, duration);
            } else {
//                startTransition(false,duration);
                mMenuLayout.switchState(mPosition, duration);
            }
        }
    }

    private void startTransition(boolean expend, final int duration) {
        final int screenWidth = mFloatBallManager.mScreenWidth;
        final int screenHeight = mFloatBallManager.mScreenHeight;
        Log.d(TAG, "startTransition: expend= " + expend);
        if (expend) {
            mLastPoint.x = mLayoutParams.x;
            mLastPoint.y = mLayoutParams.y;
            int dx = 0;
            int dy = 0;
            switch (mPosition) {
                case LEFT_TOP://左上
                    dx = -mLayoutParams.x + Constants.FLOAT_LAYOUT_MARGIN_W;
                    dy = 0 - mLayoutParams.y + Constants.FLOAT_LAYOUT_MARGIN_H;
                    break;
                case LEFT_CENTER://左中
                    dx = -mLayoutParams.x + Constants.FLOAT_LAYOUT_MARGIN_W;
                    break;
                case LEFT_BOTTOM://左下
                    dx = -mLayoutParams.x + Constants.FLOAT_LAYOUT_MARGIN_W;
                    dy = screenHeight - mFloatBallManager.getStatusBarHeight() - mSize - mLayoutParams.y - Constants.FLOAT_LAYOUT_MARGIN_H;
                    break;
                case RIGHT_TOP://右上
                    dx = screenWidth - mSize - mLayoutParams.x - Constants.FLOAT_LAYOUT_MARGIN_W;
                    dy = 0 - mLayoutParams.y + Constants.FLOAT_LAYOUT_MARGIN_H;
                    break;
                case RIGHT_CENTER://右中
                    dx = screenWidth - mSize - mLayoutParams.x - Constants.FLOAT_LAYOUT_MARGIN_W;
                    break;
                case RIGHT_BOTTOM://右下
                    dx = screenWidth - mSize - mLayoutParams.x - Constants.FLOAT_LAYOUT_MARGIN_W;
                    dy = screenHeight - mFloatBallManager.getStatusBarHeight() - mSize - mLayoutParams.y - Constants.FLOAT_LAYOUT_MARGIN_H;
                    break;

            }
            Log.d(TAG, "startTransition: mLayoutParams.x= " + mLayoutParams.x + ",mLayoutParams.y= " + mLayoutParams.y + ",dx= " + dx + ",dy=" + dy);
            mRunner.start(mLayoutParams.x, mLayoutParams.y, dx,
                    dy, 250);

        } else {
            int dx = mLastPoint.x - mLayoutParams.x;
            int dy = mLastPoint.y - mLayoutParams.y;
            Log.d(TAG, "startTransition: mLayoutParams.x= " + mLayoutParams.x + ",mLayoutParams.y= " + mLayoutParams.y + ",dx= " + dx + ",dy=" + dy);
            mRunner.start(mLayoutParams.x, mLayoutParams.y, dx,
                    dy, 250);

        }
    }

    public boolean isMoving() {
        return mMenuLayout.isMoving();
    }

    public void addItem(final MenuItem menuItem) {
        if (mConfig == null) return;
        ImageView imageview = new ImageView(getContext());
        imageview.setBackgroundDrawable(menuItem.mDrawable);
        mMenuLayout.addView(imageview);
        imageview.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mMenuLayout.isMoving())
                    menuItem.action();
            }
        });
    }

    public void removeAllItemViews() {
        mMenuLayout.removeAllViews();
    }

    /**
     * 根据按钮位置改变子菜单方向
     */
    public void refreshPathMenu(int position) {
//        LayoutParams menuLp = (LayoutParams) mMenuLayout.getLayoutParams();
//        LayoutParams iconLp = (LayoutParams) mIconView.getLayoutParams();
        LogUtils.d("refreshPathMenu position= " + position);

//        menuLp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        mMenuLayout.setArc(0, 360, position);


//        mIconView.setLayoutParams(iconLp);
//        mMenuLayout.setLayoutParams(menuLp);
    }

    /**
     * 计算菜单中各个view的位置
     *
     * @return
     */
    public int computeMenuLayout(WindowManager.LayoutParams layoutParams) {
        int position = FloatMenu.RIGHT_CENTER;
        final int halfBallSize = mBallSize / 2;
        final int screenWidth = mFloatBallManager.mScreenWidth;
        final int screenHeight = mFloatBallManager.mScreenHeight;
        final int floatballCenterY = mFloatBallManager.mFloatballY + halfBallSize;
        final int statusBarHeight = mFloatBallManager.getStatusBarHeight();

        int wmX = mFloatBallManager.mFloatballX;
        int wmY = floatballCenterY;

        if (wmX <= screenWidth / 3) { //左边  竖区域

            wmX = Constants.FLOAT_BALL_W_H / 2 - mSize / 2 - Constants.FLOAT_SHADOW;
            if (wmY <= mSize / 2 + Constants.FLOAT_LAYOUT_MARGIN_H) {
                position = FloatMenu.LEFT_TOP;//左上
                wmY = floatballCenterY - mSize / 2;
            } else if (wmY > screenHeight - statusBarHeight - mSize / 2 - Constants.FLOAT_LAYOUT_MARGIN_H) {
                position = FloatMenu.LEFT_BOTTOM;//左下
                wmY = floatballCenterY - mSize / 2;
            } else {
                position = FloatMenu.LEFT_CENTER;//左中
                wmY = floatballCenterY - mSize / 2;
            }
        } else if (wmX >= screenWidth * 2 / 3) {//右边竖区域

            wmX = screenWidth - mSize / 2 - Constants.FLOAT_BALL_W_H / 2 + Constants.FLOAT_SHADOW;
            if (wmY <= mSize / 2 + Constants.FLOAT_LAYOUT_MARGIN_H) {
                position = FloatMenu.RIGHT_TOP;//右上
//                wmY = floatballCenterY - halfBallSize;
                wmY = floatballCenterY - mSize / 2;
            } else if (wmY > screenHeight - statusBarHeight - mSize / 2 - Constants.FLOAT_LAYOUT_MARGIN_H) {
                position = FloatMenu.RIGHT_BOTTOM;//右下
//                wmY = floatballCenterY - mSize + halfBallSize;
                wmY = floatballCenterY - mSize / 2;
            } else {
                position = FloatMenu.RIGHT_CENTER;//右中
                wmY = floatballCenterY - mSize / 2;
            }
        }
        layoutParams.x = wmX;
        layoutParams.y = wmY;
        return position;
    }

    public static final int LEFT_TOP = 1;
    public static final int RIGHT_TOP = 2;
    public static final int LEFT_CENTER = 3;
    public static final int RIGHT_CENTER = 4;
    public static final int LEFT_BOTTOM = 5;
    public static final int RIGHT_BOTTOM = 6;

    @Override
    public void onMove(int lastX, int lastY, int curX, int curY) {
        Log.d(TAG, "onMove: lastX=" + lastX + ",lastY=" + lastY + ",curX=" + curX + ",curY=" + curY);
        mLayoutParams.x = curX;
        mLayoutParams.y = curY;
        mWindowManager.updateViewLayout(FloatMenu.this, mLayoutParams);
    }

    @Override
    public void onDone() {
        Log.d(TAG, "onDone: mExpended = " + mExpended);
        //没有展开，执行展开动画
        if (mExpended) {
            mFloatBallManager.reset();
            mMenuLayout.setExpand(false);

        } else {
            mIconView.setVisibility(GONE);
            mMenuLayout.switchState(mPosition, mDuration);
        }
        mExpended = !mExpended;
    }

    /**
     * 选中某个菜单
     *
     * @param gravity
     */
    public void chooseMenuItem(int gravity) {
        Log.d(TAG, "chooseMenuItem: gravity = " + gravity);
        if (mMenuLayout.isExpanded()) {
            switch (gravity) {
                case Gravity.LEFT:
                    mMenuLayout.getChildAt(2).performClick();
                    break;
                case Gravity.TOP:
                    mMenuLayout.getChildAt(3).performClick();
                    break;
                case Gravity.RIGHT:
                    mMenuLayout.getChildAt(0).performClick();
                    break;
                case Gravity.BOTTOM:
                    mMenuLayout.getChildAt(1).performClick();
                    break;
                default:
                    break;
            }
        }
    }
}
