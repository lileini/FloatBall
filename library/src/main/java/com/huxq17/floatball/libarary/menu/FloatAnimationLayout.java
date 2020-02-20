package com.huxq17.floatball.libarary.menu;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.huxq17.floatball.libarary.FloatBallManager;
import com.huxq17.floatball.libarary.FloatBallUtil;
import com.huxq17.floatball.libarary.R;
import com.huxq17.floatball.libarary.utils.Constants;
import com.huxq17.floatball.libarary.view.FrameAnimation;


/**
 * 子菜单项布局
 *
 * @author 何凌波
 */
public class FloatAnimationLayout extends View {
    private static final String TAG = "FloatAnimationLayout";
    private boolean mExpanded = false;
    private boolean isMoving = false;
    private int position = FloatMenu.LEFT_TOP;
    private boolean isAdded;
//    private FloatBallManager mFloatBallManager;
    private OnLayoutAnimationListener mListener;

    public FloatAnimationLayout(Context context) {
        this(context,null);
    }

    public FloatAnimationLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FloatAnimationLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundResource(R.drawable.floatball_000);
    }
    /*public int computeMenuLayout(WindowManager.LayoutParams layoutParams) {
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
                wmY = floatballCenterY - mSize / 2;
            }
        }
        layoutParams.x = wmX;
        layoutParams.y = wmY;
        return position;
    }*/


    /*public FloatAnimationLayout(Context context) {
        super(context);
        mSize = Constants.FLOAT_LAYOUT_H;
        init(context);
        setBackgroundResource(R.drawable.floatball_000);
//        mFloatBallManager = floatBallManager;
    }*/



    /**
     * 开始动画
     *
     * @param
     */
    /*public void attachToWindow(final WindowManager windowManager,final boolean expanded) {
        Log.d(TAG, "attachToWindow: ");
        if (!isAdded) {
            final int position = computeMenuLayout(mLayoutParams);
            int[] frameRess = null;
            switch (position) {
                case FloatMenu.LEFT_TOP://左上

                    break;
                case FloatMenu.LEFT_CENTER://左中
                    break;
                case FloatMenu.LEFT_BOTTOM://左下
                    break;
                case FloatMenu.CENTER_TOP://上中
                    break;
                case FloatMenu.CENTER_BOTTOM://下中
                    break;
                case FloatMenu.RIGHT_TOP://右上
                    break;
                case FloatMenu.RIGHT_CENTER://右中
                    mLayoutParams.y = mFloatBallManager.floatballY - 184 + Constants.FLOAT_BALL_H / 2;
                    mLayoutParams.x = mFloatBallManager.floatballX - Constants.FLOAT_LAYOUT_W + (Constants.FLOAT_BALL_W - Constants.FLOAT_SHADOW_WIDTH);
                    if (expanded) {
                        frameRess = getRes(R.array.right_center_expend);
                    }else {
                        frameRess = getRes(R.array.right_center_right_pack_up);
                    }
                    break;
                case FloatMenu.RIGHT_BOTTOM://右下
                    break;

                case FloatMenu.CENTER:
                    break;
            }
            windowManager.addView(this, mLayoutParams);


            isAdded = true;
            FrameAnimation frameAnimation = new FrameAnimation(this, frameRess, 33, false);
            frameAnimation.setAnimationListener(new FrameAnimation.AnimationListener() {
                @Override
                public void onAnimationStart() {
                    Log.d(TAG, "onAnimationStart: ");
                }

                @Override
                public void onAnimationEnd() {
                    Log.d(TAG, "onAnimationEnd: ");
                    mFloatBallManager.onFloatAnimationEnd(expanded,position);
                }

                @Override
                public void onAnimationRepeat() {
                    Log.d(TAG, "onAnimationRepeat: ");
                }
            });
            frameAnimation.play();
        }
    }*/

    public void setOnLayoutAnimationLinstener(OnLayoutAnimationListener listener){
        mListener = listener;
    }

    public void showLayoutAnimation(final int position, final boolean expanded){
        int[] frameRess = null;
        switch (position) {
            case FloatMenu.LEFT_TOP://左上

                break;
            case FloatMenu.LEFT_CENTER://左中
                break;
            case FloatMenu.LEFT_BOTTOM://左下
                break;
            case FloatMenu.CENTER_TOP://上中
                break;
            case FloatMenu.CENTER_BOTTOM://下中
                break;
            case FloatMenu.RIGHT_TOP://右上
                break;
            case FloatMenu.RIGHT_CENTER://右中
                if (expanded) {
                    frameRess = getRes(R.array.right_center_expend);
                }else {
                    frameRess = getRes(R.array.right_center_right_pack_up);
                }
                break;
            case FloatMenu.RIGHT_BOTTOM://右下
                break;

            case FloatMenu.CENTER:
                break;
        }


        FrameAnimation frameAnimation = new FrameAnimation(this, frameRess, 33, false);
        frameAnimation.setAnimationListener(new FrameAnimation.AnimationListener() {
            @Override
            public void onAnimationStart() {
                Log.d(TAG, "onAnimationStart: ");
                setVisibility(VISIBLE);
                if (mListener != null){
                    mListener.onAnimationStart(position,expanded);
                }
            }

            @Override
            public void onAnimationEnd() {
                Log.d(TAG, "onAnimationEnd: ");
                setVisibility(GONE);
                if (mListener != null){
                    mListener.onAnimationEnd(position,expanded);
                }
//                mFloatBallManager.onFloatAnimationEnd(expanded,position);
            }

            @Override
            public void onAnimationRepeat() {
                Log.d(TAG, "onAnimationRepeat: ");
            }
        });
        frameAnimation.play();
    }

    private int[] getRes(int array) {
        TypedArray typedArray = getResources().obtainTypedArray(array);
        int len = typedArray.length();
        int[] resId = new int[len];
        for (int i = 0; i < len; i++) {
            resId[i] = typedArray.getResourceId(i, -1);
        }
        typedArray.recycle();
        return resId;
    }


    public void detachFromWindow(WindowManager windowManager) {
        if (isAdded) {

            if (getContext() instanceof Activity) {
                windowManager.removeViewImmediate(this);
            } else {
                windowManager.removeView(this);
            }
            isAdded = false;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
//        mFloatBallManager.mAnimationLayoutX = mLayoutParams.x;
//        mFloatBallManager.mAnimationLayoutY = mLayoutParams.y;
    }



    @Override
    public void requestLayout() {
        if (!isMoving) {
            super.requestLayout();
        }
    }

    public boolean isExpanded() {
        return mExpanded;
    }



    public void setExpand(boolean expand) {
        mExpanded = expand;
    }

    public interface OnLayoutAnimationListener{
        void onAnimationStart(int position,boolean expanded);
        void onAnimationEnd(int position,boolean expanded);
    }
}