package com.huxq17.example.floatball;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.huxq17.example.floatball.permission.FloatPermissionManager;
import com.huxq17.floatball.libarary.FloatBallManager;
import com.huxq17.floatball.libarary.FloatBallUtil;
import com.huxq17.floatball.libarary.floatball.FloatBallCfg;
import com.huxq17.floatball.libarary.menu.FloatMenuCfg;
import com.huxq17.floatball.libarary.menu.MenuItem;
import com.huxq17.floatball.libarary.utils.BackGroudSeletor;
import com.huxq17.floatball.libarary.utils.DensityUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MainActivity extends Activity {
    private final String TAG = getClass().getSimpleName();

    private FloatBallManager mFloatballManager;
    private FloatPermissionManager mFloatPermissionManager;
    private ActivityLifeCycleListener mActivityLifeCycleListener = new ActivityLifeCycleListener();
    private int resumed;
    boolean showMenu = true;//换成false试试

    public void showFloatBall(View v) {
        mFloatballManager.show();
//        setFullScreen(v);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (check(this)) {
            init(showMenu);
            //5 如果没有添加菜单，可以设置悬浮球点击事件
            /*if (mFloatballManager.getMenuItemSize() == 0) {
                mFloatballManager.setOnFloatBallClickListener(new FloatBallManager.OnFloatBallClickListener() {
                    @Override
                    public void onFloatBallClick() {
                        toast("点击了悬浮球");
                    }
                });
            }*/
        } else {
            try {
                commonROMPermissionApplyInternal(this);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        findViewById(R.id.btn).postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        },2000);
        //6 如果想做成应用内悬浮球，可以添加以下代码。
//        getApplication().registerActivityLifecycleCallbacks(mActivityLifeCycleListener);
    }

    public boolean check(Context context) {
        boolean result = true;
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Class clazz = Settings.class;
                Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
                result = (Boolean) canDrawOverlays.invoke(null, context);
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!showMenu) {
            init(showMenu);
            //5 如果没有添加菜单，可以设置悬浮球点击事件
            /*if (mFloatballManager.getMenuItemSize() == 0) {
                mFloatballManager.setOnFloatBallClickListener(new FloatBallManager.OnFloatBallClickListener() {
                    @Override
                    public void onFloatBallClick() {
                        toast("点击了悬浮球");
                    }
                });
            }*/
        }
    }

    public static void commonROMPermissionApplyInternal(Context context) throws NoSuchFieldException, IllegalAccessException {
        Class clazz = Settings.class;
        Field field = clazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION");
        Intent intent = new Intent(field.get(null).toString());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
        //FloatWinPermissionCompat.getInstance().startActivity(intent);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mFloatballManager.show();
//        mFloatballManager.onFloatBallClick();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
//        mFloatballManager.hide();
    }

    private boolean isfull = false;

    //全屏设置和退出全屏
    private void setFullScreen() {
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        isfull = true;
    }

    private void quitFullScreen() {
        final WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setAttributes(attrs);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        isfull = false;
    }

    public void setFullScreen(View view) {
        if (isfull == true) {
            quitFullScreen();
        } else {
            setFullScreen();
        }
    }

    private void initSinglePageFloatball(boolean showMenu) {
        //1 初始化悬浮球配置，定义好悬浮球大小和icon的drawable
        int ballSize = DensityUtil.dip2px(this, 116);
//        Drawable ballIcon = BackGroudSeletor.getdrawble("ic_floatball", this);
        Drawable ballIcon = getResources().getDrawable(R.drawable.ic_float_ball);
        FloatBallCfg ballCfg = new FloatBallCfg(ballSize, ballIcon, FloatBallCfg.Gravity.RIGHT_CENTER);
        //设置悬浮球不半隐藏
//        ballCfg.setHideHalfLater(false);
        if (showMenu) {
            //2 需要显示悬浮菜单
            //2.1 初始化悬浮菜单配置，有菜单item的大小和菜单item的个数
            int menuSize = DensityUtil.dip2px(this, 360);
            int menuItemSize = DensityUtil.dip2px(this, 116);
            FloatMenuCfg menuCfg = new FloatMenuCfg(menuSize, menuItemSize);
            //3 生成floatballManager
            //必须传入Activity
            mFloatballManager = new FloatBallManager(this, ballCfg, menuCfg);
            addFloatMenuItem();
        } else {
            //必须传入Activity
            mFloatballManager = new FloatBallManager(this, ballCfg);
        }
    }

    private void addFloatMenuItem() {
        MenuItem navigationItem = new MenuItem(getResources().getDrawable(R.drawable.ic_navigation)) {
            @Override
            public void action() {
                toast("打开导航");
                mFloatballManager.closeMenu();
            }
        };
        MenuItem musicItem = new MenuItem(getResources().getDrawable(R.drawable.ic_music)) {
            @Override
            public void action() {
                toast("打开音乐");
                mFloatballManager.closeMenu();
            }
        };
        MenuItem phoneItem = new MenuItem(getResources().getDrawable(R.drawable.ic_phone)) {
            @Override
            public void action() {
                toast("打开电话");
                mFloatballManager.closeMenu();
            }
        };
        MenuItem radioItem = new MenuItem(getResources().getDrawable(R.drawable.ic_radio)) {
            @Override
            public void action() {
                toast("打开收音机");
                mFloatballManager.closeMenu();
            }
        };
//        mFloatballManager.addMenuItem(navigationItem)
//                .addMenuItem(musicItem)
//                .addMenuItem(phoneItem)
//                .addMenuItem(radioItem)
//                .buildMenu();


//        test();
    }

    private boolean mExpend;
    View view;
    private static final int MSG_EXPEND = 13;
    private static final int MSG_PACK_UP = MSG_EXPEND + 1;
    private Handler mAnimationHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case MSG_EXPEND:
                    view.setBackgroundResource(R.drawable.pack_up_floatball);
                    mExpend = true;
                    break;
                case MSG_PACK_UP:
                    view.setBackgroundResource(R.drawable.expend_floatball);
                    mExpend = false;
                    break;
            }
            return false;
        }
    });

    private void test() {
        WindowManager mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        view = LayoutInflater.from(this).inflate(R.layout.test_view, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Log.d(TAG, "v: test_view");
                AnimationDrawable background = (AnimationDrawable) view.getBackground();
                background.start();
                if (!mExpend) {
                    int time = 33*36;
                    mAnimationHandler.sendEmptyMessageDelayed(MSG_EXPEND,time);
                } else {
                    background.start();
                    int time = 33*36;
                    mAnimationHandler.sendEmptyMessageDelayed(MSG_PACK_UP,time);
                }
            }
        });
        WindowManager.LayoutParams layoutParams = FloatBallUtil.getLayoutParams(this);
        layoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
        mWindowManager.addView(view, layoutParams);
    }


    private void init(boolean showMenu) {
        //1 初始化悬浮球配置，定义好悬浮球大小和icon的drawable
        int ballSize = DensityUtil.dip2px(this, 116);
//        Drawable ballIcon = BackGroudSeletor.getdrawble("ic_floatball", this);
        Drawable ballIcon = getResources().getDrawable(R.drawable.ic_float_ball);
        //可以尝试使用以下几种不同的config。
//        FloatBallCfg ballCfg = new FloatBallCfg(ballSize, ballIcon);
//        FloatBallCfg ballCfg = new FloatBallCfg(ballSize, ballIcon, FloatBallCfg.Gravity.LEFT_CENTER,false);
//        FloatBallCfg ballCfg = new FloatBallCfg(ballSize, ballIcon, FloatBallCfg.Gravity.LEFT_BOTTOM, -100);
//        FloatBallCfg ballCfg = new FloatBallCfg(ballSize, ballIcon, FloatBallCfg.Gravity.RIGHT_TOP, 100);
        FloatBallCfg ballCfg = new FloatBallCfg(ballSize, ballIcon, FloatBallCfg.Gravity.RIGHT_CENTER);
        //设置悬浮球不半隐藏
        ballCfg.setHideHalfLater(false);
        if (showMenu) {
            //2 需要显示悬浮菜单
            //2.1 初始化悬浮菜单配置，有菜单item的大小和菜单item的个数
            int menuSize = DensityUtil.dip2px(this, 360);
            int menuItemSize = DensityUtil.dip2px(this, 116);
            FloatMenuCfg menuCfg = new FloatMenuCfg(menuSize, menuItemSize);
            //3 生成floatballManager
            mFloatballManager = new FloatBallManager(getApplicationContext(), ballCfg, menuCfg);
            addFloatMenuItem();

        } else {
            mFloatballManager = new FloatBallManager(getApplicationContext(), ballCfg);
        }
        setFloatPermission();
    }

    private void setFloatPermission() {
        // 设置悬浮球权限，用于申请悬浮球权限的，这里用的是别人写好的库，可以自己选择
        //如果不设置permission，则不会弹出悬浮球
        mFloatPermissionManager = new FloatPermissionManager();
        mFloatballManager.setPermission(new FloatBallManager.IFloatBallPermission() {
            @Override
            public boolean onRequestFloatBallPermission() {
                requestFloatBallPermission(MainActivity.this);
                return true;
            }

            @Override
            public boolean hasFloatBallPermission(Context context) {
                return mFloatPermissionManager.checkPermission(context);
            }

            @Override
            public void requestFloatBallPermission(Activity activity) {
                mFloatPermissionManager.applyPermission(activity);
            }

        });
    }

    public class ActivityLifeCycleListener implements Application.ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
            ++resumed;
            setFloatballVisible(true);
        }

        @Override
        public void onActivityPaused(Activity activity) {
            --resumed;
            if (!isApplicationInForeground()) {
                setFloatballVisible(false);
            }
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    private void setFloatballVisible(boolean visible) {
        if (visible) {
            mFloatballManager.show();
        } else {
            mFloatballManager.hide();
        }
    }

    public boolean isApplicationInForeground() {
        return resumed > 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注册ActivityLifeCyclelistener以后要记得注销，以防内存泄漏。
        getApplication().unregisterActivityLifecycleCallbacks(mActivityLifeCycleListener);
    }
}
