package com.xiaoyao.mymusicapp.utils;

import android.app.ActivityManager;
import android.content.*;
import android.util.Log;

import java.util.*;

public class ServiceUtils {
    /**
     * 判断服务是否正在运行
     * @param mContext   上下文对象
     * @param className  Service类的全路径类名 "包名+类名" 如com.demo.test.MyService
     * @return
     */
    public boolean isServiceRunning(Context mContext, String className) {
        // ActivityManager用于管理Activity
        ActivityManager mActivityManager = (ActivityManager) mContext
                .getApplicationContext().getSystemService(
                        Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runSerInfoList = new ArrayList<>();
        // getRunningServices()得到正在运行的服务
        runSerInfoList = mActivityManager.getRunningServices(30);
        // 遍历数组判断是否存在服务
        for (int i = 0; i < runSerInfoList.size(); i++) {
            String serName = runSerInfoList.get(i).service.getClassName().toString();
            Log.d("服务工具类日志", "找到服务："+serName);
            if (serName.equals(className)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断音乐服务是否正在运行
     * @param mContext 上下文对象
     * @return
     */
    public static boolean isMusicServiceRunning(Context mContext) {
        // ActivityManager用于管理Activity
        ActivityManager mActivityManager = (ActivityManager) mContext
                .getApplicationContext().getSystemService(
                        Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runSerInfoList;
        // getRunningServices()得到正在运行的服务
        runSerInfoList = mActivityManager.getRunningServices(30);
        // 遍历数组判断是否存在服务
        for (int i = 0; i < runSerInfoList.size(); i++) {
            String serName = runSerInfoList.get(i).service.getClassName().toString();
            Log.d("服务工具类日志", "找到服务："+serName);
            if (serName.equals("com.xiaoyao.mymusicapp.service.MyMusicService")) {
                return true;
            }
        }
        return false;
    }
}
