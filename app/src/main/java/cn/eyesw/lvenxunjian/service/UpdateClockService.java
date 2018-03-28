package cn.eyesw.lvenxunjian.service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.eyesw.lvenxunjian.constant.ApiService;
import cn.eyesw.lvenxunjian.constant.Constant;
import cn.eyesw.lvenxunjian.utils.NetWorkUtil;
import cn.eyesw.lvenxunjian.utils.SpUtil;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 更新打卡时间的服务
 */
public class UpdateClockService extends Service {

    private static final int STAFF_POSITION = 0;

    private SimpleDateFormat mSimpleDateFormat;
    private Timer mTimer;
    private long mTime;
    private Bundle mBundle;
    private Intent mIntent;

    private double mLatitude = 0.0;
    private double mLongitude = 0.0;
    private String mAddress;
    private double mBeforeLatitude = 0.0;
    private double mBeforeLongitude = 0.0;
    private ApiService mApiService;
    private boolean isGetPosition;
    private String mStaffId;
    private LocationClient mLocationClient;
    private MyLocationListener myLocationListener = new MyLocationListener();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STAFF_POSITION:
                    // 上传巡线员实时位置
                    sendStaffPosition();
                    break;
            }
        }
    };

    /**
     * 保存巡线员实时位置
     */
    private Map saveStaffPosition() {
        Map<String, String> map = new HashMap<>();
        if (isGetPosition) {
            int status;
            // 获取当前日期（yyyy-MM-dd HH:mm:ss）
            String date = getDate();
            if (mBeforeLatitude != 0.0) {
                // 计算距离（单位：米）
                double distance = calDistance();
                if (distance == 0.0) { // 1 代表静止
                    status = 1;
                } else { // 0 代表运动
                    status = 0;
                }
                // 上传数据
                map.put("staff_id", mStaffId);
                map.put("latitude", Double.toString(mLatitude));
                map.put("longitude", Double.toString(mLongitude));
                map.put("status", Integer.toString(status));
                map.put("address", mAddress);
                map.put("length", Double.toString(distance));
                map.put("speed", Double.toString(distance / 20 * 3.6));
                map.put("createtime", date);
            }
            mBeforeLatitude = mLatitude;
            mBeforeLongitude = mLongitude;
        }
        return map;
    }

    /**
     * 获取当前日期
     */
    private String getDate() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    /**
     * 计算距离
     */
    private double calDistance() {
        LatLng beforeLatLng = new LatLng(mBeforeLatitude, mBeforeLongitude);
        LatLng latLng = new LatLng(mLatitude, mLongitude);
        return DistanceUtil.getDistance(beforeLatLng, latLng);
    }

    /**
     * 上传巡线员实时位置
     */
    private void sendStaffPosition() {
        if (isGetPosition) {
            Map<String, String> map = saveStaffPosition();
            Call<ResponseBody> staffPosition = mApiService.sendPosition(map);
            staffPosition.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    try {
                        String json = new String(response.body().bytes());
                        JSONObject jsonObject = new JSONObject(json);
                        int code = jsonObject.getInt("code");
                        if (code == 200) {
                            mHandler.sendEmptyMessageDelayed(STAFF_POSITION, 20 * 1000);
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                    call.cancel();
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    mHandler.sendEmptyMessageDelayed(STAFF_POSITION, 20 * 1000);
                    call.cancel();
                }
            });
        } else {
            mHandler.sendEmptyMessageDelayed(0, 5 * 1000);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new RuntimeException("运行时异常!");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mStaffId = SpUtil.getInstance(this).getString("id");
        mApiService = NetWorkUtil.getInstance().getApiService();
        // 初始化
        init();

        // 初始化位置
        initPosition();

        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // 发送广播
                sendTimeChangeBroadcast();
            }
        }, 1000, 1000);
    }

    /**
     * 初始化位置
     */
    private void initPosition() {
        // 初始化
        mLocationClient = new LocationClient(this);
        mLocationClient.registerLocationListener(myLocationListener);

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(1000 * 20);
        option.setOpenGps(true);
        option.setEnableSimulateGps(false);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
        mLocationClient.start();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            mLocationClient.restart();
        }
        mHandler.sendEmptyMessageDelayed(STAFF_POSITION, 5 * 1000);
    }

    /**
     * 初始化
     */
    private void init() {
        mSimpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        // mSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("GTM+0"));
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
     *
     * @return 格式化之后的两个时间戳的差，格式为: 00:00:00(时分秒)
     */
    public String getTime() {
        Date endTime = new Date();
        return mSimpleDateFormat.format(endTime.getTime() - mTime - 8 * 60 * 60 * 1000);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        mTime = intent.getLongExtra("timeStamp", 0);
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        mTimer.cancel();
        if (mLocationClient != null && mLocationClient.isStarted()) {
            mLocationClient.stop();
            mLocationClient.unRegisterLocationListener(myLocationListener);
        }
        super.onDestroy();
    }

    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            mLatitude = bdLocation.getLatitude();
            mLongitude = bdLocation.getLongitude();
            mAddress = bdLocation.getAddrStr();
            mBeforeLatitude = mLatitude;
            mBeforeLongitude = mLongitude;
            isGetPosition = true;
        }
    }

}
