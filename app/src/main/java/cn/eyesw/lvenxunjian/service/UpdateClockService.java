package cn.eyesw.lvenxunjian.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import cn.eyesw.lvenxunjian.constant.Constant;

/**
 * 更新打卡时间的服务
 */
public class UpdateClockService extends Service {

    private SimpleDateFormat mSimpleDateFormat;
    private Timer mTimer;
    private long mTime;
    private Bundle mBundle;
    private Intent mIntent;

    @Override
    public IBinder onBind(Intent intent) {
        throw new RuntimeException("运行时异常!");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化
        init();

        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // 发送广播
                sendTimeChangeBroadcast();
            }
        }, 1000, 1000);
    }

    /**
     * 初始化
     */
    private void init() {
        mSimpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        mTimer = new Timer();
        mIntent = new Intent();
        mBundle = new Bundle();
    }

    /**
     * 发送时间改变的通知
     */
    private void sendTimeChangeBroadcast() {
        mBundle.putString("time", getTime());
        mIntent.putExtras(mBundle);
        mIntent.setAction(Constant.TIME_CHANGED_ACTION);
        // 发送广播，通知 UI 层时间改变了
        sendBroadcast(mIntent);
    }

    /**
     * 获取两个时间戳之间的差
     * @return 格式化之后的两个时间戳的差，格式为: 00:00:00(时分秒)
     */
    public String getTime() {
        Date endTime = new Date();
        return mSimpleDateFormat.format(endTime.getTime() - mTime - 8 * 60 * 60 * 1000);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        mTime = intent.getLongExtra("timeStamp", 0);
    }

    @Override
    public void onDestroy() {
        mTimer.cancel();
        super.onDestroy();
    }

}
