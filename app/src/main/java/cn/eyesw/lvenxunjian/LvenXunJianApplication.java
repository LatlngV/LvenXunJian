package cn.eyesw.lvenxunjian;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import com.baidu.mapapi.SDKInitializer;

/**
 * 巡检 APP
 */
public class LvenXunJianApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化百度地图
        SDKInitializer.initialize(getApplicationContext());

        // 适配 Android 7.0 FileProvider 问题
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
    }

}
