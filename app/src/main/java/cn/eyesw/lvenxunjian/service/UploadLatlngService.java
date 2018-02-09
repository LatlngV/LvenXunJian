package cn.eyesw.lvenxunjian.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cn.eyesw.lvenxunjian.constant.ApiService;
import cn.eyesw.lvenxunjian.utils.NetWorkUtil;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * 上传位置
 */
public class UploadLatlngService extends Service {

    private double mLatitude = 0.0;
    private double mLongitude = 0.0;
    private String mAddress = null;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // 上传信息
            ApiService apiService = NetWorkUtil.getInstance().getApiService();
            Call<ResponseBody> currentPosition = apiService.currentPosition(Double.toString(mLatitude), Double.toString(mLongitude), mAddress);
            currentPosition.enqueue(new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    Log.d("tag", "latitude == " + Double.toString(mLatitude) + ", longitude == " + Double.toString(mLongitude));
                    call.cancel();
                    mHandler.sendEmptyMessageDelayed(0, 1000 * 10);
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    call.cancel();
                    mHandler.sendEmptyMessageDelayed(0, 1000 * 10);
                }
            });
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        /*
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.app_launcher)
                .setWhen(System.currentTimeMillis())
                .setTicker("有通知到来")
                .setContentTitle("点击进入主界面")
                .setContentText("正在上传当前位置")
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
        */

        // 初始化
        LocationClient locationClient = new LocationClient(this);
        MyLocationListener myLocationListener = new MyLocationListener();
        locationClient.registerLocationListener(myLocationListener);

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(1000 * 10);
        option.setOpenGps(true);
        option.setEnableSimulateGps(false);
        option.setIsNeedAddress(true);
        locationClient.setLocOption(option);
        locationClient.start();
        locationClient.restart();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler.sendEmptyMessageDelayed(0, 1000 * 10);
        return super.onStartCommand(intent, flags, startId);
    }

    private class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            mLatitude = bdLocation.getLatitude();
            mLongitude = bdLocation.getLongitude();
            mAddress = bdLocation.getAddrStr();
        }
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        stopForeground(true);
        super.onDestroy();
    }

}
