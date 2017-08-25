package com.dev.testselectpics.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.Stack;

@SuppressWarnings("ALL")
public class AppApplication extends Application {

    private static AppApplication mAppApplication;

    private Stack<Activity> activityStack;// activity栈

    @Override
    public void onCreate() {
        super.onCreate();
        mAppApplication = this;
        initImageLoader(getApplicationContext());
    }

    private void initImageLoader(Context context) {
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        config.writeDebugLogs(); // Remove for release app

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
    }

    /**
     * 获取Application
     */
    public static AppApplication get() {
        return mAppApplication;
    }

    public static AppApplication get(Context context) {
        return (AppApplication) context.getApplicationContext();
    }

    /**
     * 把一个activity压入栈列中
     */
    public void pushActivityToStack(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<>();
        }
        activityStack.add(activity);
    }

    /**
     * 获取栈顶的activity，先进后出原则
     */
    public Activity getLastActivityFromStack() {
        return activityStack.lastElement();
    }

    /**
     * 从栈列中移除一个activity
     */
    public void popActivityFromStack(Activity activity) {
        if (activityStack != null && activityStack.size() > 0) {
            if (activity != null) {
                activity.finish();
                activityStack.remove(activity);
            }
        }
    }

    /**
     * 退出所有activity
     */
    public void finishAllActivity() {
        if (activityStack != null) {
            while (activityStack.size() > 0) {
                Activity activity = getLastActivityFromStack();
                if (activity == null) {
                    break;
                }
                popActivityFromStack(activity);
            }
        }
    }
}
