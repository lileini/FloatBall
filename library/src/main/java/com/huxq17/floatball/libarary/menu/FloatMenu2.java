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

package com.huxq17.floatball.libarary.menu;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.huxq17.floatball.libarary.FloatBallManager;
import com.huxq17.floatball.libarary.FloatBallUtil;
import com.huxq17.floatball.libarary.R;
import com.huxq17.floatball.libarary.utils.Constants;
import com.huxq17.floatball.libarary.view.FrameAnimation;

public class FloatMenu2 extends FrameLayout implements View.OnClickListener, FloatAnimationLayout.OnLayoutAnimationListener {
    private final String TAG = getClass().getSimpleName();
    //    private MenuLayout mMenuLayout;
    private int mPosition = FloatMenu.RIGHT_CENTER;
    private int mSize = Constants.FLOAT_LAYOUT_H;
    private FloatBallManager mFloatBallManager;
    private WindowManager.LayoutParams mLayoutParams;
    private boolean isAdded = false;
    private boolean mListenBackEvent = true;
    private View mMusic;
    private View mNavigation;
    private View mPhone;
    private View mRadio;
    private boolean mExpanded = false;
    private FloatAnimationLayout mFloatAnimationLayout;

    public FloatMenu2(Context context, final FloatBallManager floatBallManager, FloatMenuCfg config) {
        super(context);
        this.mFloatBallManager = floatBallManager;
        inflate(context, R.layout.layout_float_menu, this);
        init(context);
    }

    private void initLayoutParams(Context context) {
        mLayoutParams = FloatBallUtil.getLayoutParams(context, true);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.d(TAG, "onFinishInflate: ");

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
               /* if (mMenuLayout.isExpanded()) {
                    toggle(mDuration);
                }*/
                if (mExpanded) {
                    mFloatBallManager.showAnimation(false);
                }
                break;
        }
        return super.onTouchEvent(event);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (R.id.music == id) {
            mFloatBallManager.showAnimation(false);
        }
        /*switch (view.getId()){
            case R.id.music:
                break;
            case R.id.navigation:
                break;
            case R.id.phone:
                break;
            case R.id.radio:
                break;
            default:
                break;
        }*/
    }

    public void showLayoutAnimation(final WindowManager windowManager, final boolean expanded) {
        Log.d(TAG, "showLayoutAnimation: expanded=" + expanded);

        if (expanded) {
            mPosition = computeMenuLayout(mLayoutParams);
            windowManager.updateViewLayout(this, mLayoutParams);
        } else {
            //展开动画还未完成，点击不能执行收起动画
            mExpanded = expanded;
        }
        mFloatAnimationLayout.showLayoutAnimation(mPosition, expanded);
    }


    public int computeMenuLayout(WindowManager.LayoutParams layoutParams) {
        int position = FloatMenu2.RIGHT_CENTER;
        final int halfBallSize = Constants.FLOAT_BALL_W / 2;
        final int screenWidth = mFloatBallManager.mScreenWidth;
        final int screenHeight = mFloatBallManager.mScreenHeight;
        final int floatballCenterY = mFloatBallManager.floatballY + halfBallSize;
        final int statusBarHeight = mFloatBallManager.getStatusBarHeight();

        int wmX = mFloatBallManager.floatballX + Constants.CLICK_MOVE_DISTANCE_X;
        int wmY = floatballCenterY;

        if (wmX <= screenWidth / 3) //左边  竖区域
        {
            wmX = 0 + Constants.CLICK_MOVE_DISTANCE_X;
            if (wmY <= mSize / 2) {
                position = FloatMenu2.LEFT_TOP;//左上
                wmY = floatballCenterY - halfBallSize;
            } else if (wmY > screenHeight - statusBarHeight - mSize / 2) {
                position = FloatMenu2.LEFT_BOTTOM;//左下
                wmY = floatballCenterY - mSize + halfBallSize;
            } else {
                position = FloatMenu2.LEFT_CENTER;//左中
                wmY = floatballCenterY - mSize / 2;
            }
        } else if (wmX >= screenWidth * 2 / 3)//右边竖区域
        {
//            wmX = screenWidth - mSize;
            wmX = mFloatBallManager.floatballX - Constants.CLICK_MOVE_DISTANCE_X;
            if (wmY <= mSize / 2) {
                position = FloatMenu2.RIGHT_TOP;//右上
                wmY = floatballCenterY - halfBallSize;
            } else if (wmY > screenHeight - statusBarHeight - mSize / 2) {
                position = FloatMenu2.RIGHT_BOTTOM;//右下
                wmY = floatballCenterY - mSize + halfBallSize;
            } else {
                position = FloatMenu2.RIGHT_CENTER;//右中
                wmY = mFloatBallManager.floatballY - 184 + Constants.FLOAT_BALL_H / 2;
                wmX = mFloatBallManager.floatballX - Constants.FLOAT_LAYOUT_W + (Constants.FLOAT_BALL_W - Constants.FLOAT_SHADOW_WIDTH);
            }
        }
        layoutParams.x = wmX;
        layoutParams.y = wmY;
        return position;
    }

    public void attachToWindow(WindowManager windowManager) {
        if (!isAdded) {
            Log.d(TAG, "attachToWindow: ");
            setVisibility(GONE);
            mPosition = computeMenuLayout(mLayoutParams);
            Log.d(TAG, "attachToWindow: mPosition = " + mPosition);
            windowManager.addView(this, mLayoutParams);
            isAdded = true;
        }
    }

    public void detachFromWindow(WindowManager windowManager) {
        if (isAdded) {
            Log.d(TAG, "detachFromWindow: ");
            Log.d(TAG, Log.getStackTraceString(new Exception()));
//            toggle(0);
            if (getContext() instanceof Activity) {
                windowManager.removeViewImmediate(this);
            } else {
                windowManager.removeView(this);
            }
            isAdded = false;
        }
    }

    public void updateLayout(WindowManager windowManager) {
        if (isAdded) {
            Log.d(TAG, "updateLayout: ");
            mPosition = computeMenuLayout(mLayoutParams);
            windowManager.updateViewLayout(this, mLayoutParams);
        }
    }


    private void init(Context context) {
        initLayoutParams(context);
        mLayoutParams.height = (int) getResources().getDimension(R.dimen.menu_layout_h);
        mLayoutParams.width = (int) getResources().getDimension(R.dimen.menu_layout_w);
        if (mListenBackEvent) {
            setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    int action = event.getAction();
                    if (action == KeyEvent.ACTION_DOWN) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            FloatMenu2.this.mFloatBallManager.closeMenu();
                            return true;
                        }
                    }
                    return false;
                }
            });
            setFocusableInTouchMode(true);
        }
        mMusic = findViewById(R.id.music);
        mNavigation = findViewById(R.id.navigation);
        mPhone = findViewById(R.id.phone);
        mRadio = findViewById(R.id.radio);
        mFloatAnimationLayout = findViewById(R.id.float_animation_layout);

        mMusic.setOnClickListener(this);
        mNavigation.setOnClickListener(this);
        mPhone.setOnClickListener(this);
        mRadio.setOnClickListener(this);
        mFloatAnimationLayout.setOnLayoutAnimationLinstener(this);
    }


    public void remove() {
        mFloatBallManager.reset();
//        mMenuLayout.setExpand(false);
    }

    public static final int LEFT_TOP = 1;
    public static final int CENTER_TOP = 2;
    public static final int RIGHT_TOP = 3;
    public static final int LEFT_CENTER = 4;
    public static final int CENTER = 5;
    public static final int RIGHT_CENTER = 6;
    public static final int LEFT_BOTTOM = 7;
    public static final int CENTER_BOTTOM = 8;
    public static final int RIGHT_BOTTOM = 9;

    @Override
    public void onAnimationStart(int position, boolean expanded) {
        setVisibility(VISIBLE);
        setItemsVisible(false);
    }

    @Override
    public void onAnimationEnd(int position, boolean expanded) {
        if (expanded) {//展开
            setItemsVisible(true);
            mExpanded = expanded;
            //因为开始展开动画可能未完成
        } else {//收起
            setVisibility(GONE);
        }
        mFloatBallManager.onFloatAnimationEnd(expanded, position);
    }

    private void setItemsVisible(boolean visible) {
        mMusic.setVisibility(visible ? VISIBLE : GONE);
        mNavigation.setVisibility(visible ? VISIBLE : GONE);
        mPhone.setVisibility(visible ? VISIBLE : GONE);
        mRadio.setVisibility(visible ? VISIBLE : GONE);
    }
}
