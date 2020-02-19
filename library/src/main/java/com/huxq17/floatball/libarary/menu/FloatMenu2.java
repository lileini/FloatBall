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
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.huxq17.floatball.libarary.FloatBallManager;
import com.huxq17.floatball.libarary.FloatBallUtil;
import com.huxq17.floatball.libarary.R;
import com.huxq17.floatball.libarary.utils.Constants;

public class FloatMenu2 extends FrameLayout implements View.OnClickListener {
    private final String TAG  = getClass().getSimpleName();
//    private MenuLayout mMenuLayout;


    private FloatBallManager mFloatBallManager;
    private WindowManager.LayoutParams mLayoutParams;
    private boolean isAdded = false;
    private boolean mListenBackEvent = true;
    private View mMusic;
    private View mNavigation;
    private View mPhone;
    private View mRadio;

    public FloatMenu2(Context context, final FloatBallManager floatBallManager, FloatMenuCfg config) {
        super(context);
        this.mFloatBallManager = floatBallManager;
        inflate(context, R.layout.layout_float_menu,this);
        init(context);
    }

    private void initLayoutParams(Context context) {
        mLayoutParams = FloatBallUtil.getLayoutParams(context, false);
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
                mFloatBallManager.showAnimation(false);
                break;
        }
        return super.onTouchEvent(event);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (R.id.music == id){

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

    public void attachToWindow(WindowManager windowManager,int position) {
        if (!isAdded) {
            mLayoutParams.x = mFloatBallManager.mAnimationLayoutX;
            mLayoutParams.y = mFloatBallManager.mAnimationLayoutY;
            windowManager.addView(this, mLayoutParams);
            isAdded = true;
        }
    }

    public void detachFromWindow(WindowManager windowManager) {
        if (isAdded) {
//            toggle(0);
            if (getContext() instanceof Activity) {
                windowManager.removeViewImmediate(this);
            } else {
                windowManager.removeView(this);
            }
            isAdded = false;
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

        mMusic.setOnClickListener(this);
        mNavigation.setOnClickListener(this);
        mPhone.setOnClickListener(this);
        mRadio.setOnClickListener(this);
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

}
