package cn.eyesw.lvenxunjian.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import cn.eyesw.lvenxunjian.R;
import cn.eyesw.lvenxunjian.base.BaseActivity;
import cn.eyesw.lvenxunjian.bean.PipelinePointBean;
import cn.eyesw.lvenxunjian.bean.StaffPointBean;
import cn.eyesw.lvenxunjian.constant.Constant;
import cn.eyesw.lvenxunjian.service.UpdateClockService;
import cn.eyesw.lvenxunjian.utils.OkHttpManager;
import cn.eyesw.lvenxunjian.utils.PipelinePointDao;
import cn.eyesw.lvenxunjian.utils.SpUtil;
import cn.eyesw.lvenxunjian.utils.StaffPointDao;
import cn.eyesw.lvenxunjian.utils.ToolbarUtil;
import okhttp3.Call;

public class BaiduActivity extends BaseActivity {

    @BindView(R.id.baidu_toolbar)
    protected Toolbar mToolbar;
    @BindView(R.id.baidu_texture_map_view)
    protected TextureMapView mTextureMapView;
    @BindView(R.id.baidu_timer)
    protected TextView mTvTimer;
    @BindView(R.id.baidu_btn_clock)
    protected Button mBtnClock;

    private MyLocationListener mMyLocationListener = new MyLocationListener();
    // 是否打卡的标志位，true 是开始打卡，false 是结束打卡
    private boolean mIsClock = false;
    // 百度地图
    private BaiduMap mBaiduMap;
    // 时间更改的广播
    private TimeReceiver mTimeReceiver;
    // 服务的意图
    private Intent mService;
    // 时间戳临时变量
    private long mTimeTemp;
    // 是不是第一次定位
    private boolean isFirstLoc = true;
    // 保存管线点的实体类
    private PipelinePointBean pipelinePointBean;
    private double latitude;
    private double longitude;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // 先清空地图，重新画数据（Android 中百度地图不能批量删除覆盖物，只能全部清除）
            mBaiduMap.clear();
            drawPipeline();
        }
    };
    private OkHttpManager mOkHttpManager;
    private Map<String, String> mMap;
    private LatLng mLatLng;

    @Override
    protected int getContentLayoutRes() {
        return R.layout.activity_baidu;
    }

    @Override
    protected void initToolbar() {
        ToolbarUtil toolbarUtil = new ToolbarUtil(this);
        toolbarUtil.setToolbar(mToolbar, "百度地图");
    }

    @Override
    protected void initView() {
        mOkHttpManager = OkHttpManager.getInstance();
        mBaiduMap = mTextureMapView.getMap();
        // 定位
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        LocationClient mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(mMyLocationListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();

        // 设置地图类型
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
    }

    private class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            Log.d("TAG", "onReceiveLocation: " + bdLocation.toString());
            if (bdLocation == null || mTextureMapView == null) {
                return;
            }
            double latitude1 = bdLocation.getLatitude();
            double longitude1 = bdLocation.getLongitude();

            MyLocationData myLocationData = new MyLocationData.Builder()
                    .accuracy(bdLocation.getRadius())
                    .direction(100)
                    .latitude(latitude1)
                    .longitude(longitude1)
                    .build();
            // 设置定位数据
            mBaiduMap.setMyLocationData(myLocationData);
            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.app_launcher);
            MyLocationConfiguration myLocationConfiguration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true, bitmapDescriptor);
            mBaiduMap.setMyLocationConfiguration(myLocationConfiguration);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mIsClock) {
            startService(mService);
            IntentFilter filter = new IntentFilter(Constant.TIME_CHANGED_ACTION);
            registerReceiver(mTimeReceiver, filter);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 画管道
        drawPipeline();
    }

    /**
     * 画管道
     */
    private void drawPipeline() {
        mMap = new HashMap<>();
        mMap.put("staff_id", SpUtil.getInstance(getApplicationContext()).getString("id"));
        mOkHttpManager.postAsyncForm("", mMap, new OkHttpManager.DataCallback() {

            @Override
            public void onFailure(Call call, IOException e) {
                showToast("网络连接失败");
            }

            @Override
            public void onResponse(String json) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    int code = jsonObject.getInt("code");
                    if (code == 200) {
                        // 如果数据库为空，请求数据
                        PipelinePointDao pipelinePointDao = new PipelinePointDao(getApplicationContext());
                        List<PipelinePointBean> select = pipelinePointDao.select();
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        List<LatLng> pipeline = new ArrayList<>();

                        if (select.size() != jsonArray.length()) {
                            // 清空数据库
                            pipelinePointDao.delete();
                            for (int i = 0, length = jsonArray.length(); i < length; i++) {
                                JSONObject data = (JSONObject) jsonArray.get(i);
                                latitude = Double.parseDouble(data.getString("latitude"));
                                longitude = Double.parseDouble(data.getString("longitude"));
                                mLatLng = new LatLng(latitude, longitude);
                                pipeline.add(mLatLng);

                                // 把数据插入到数据库中
                                pipelinePointBean = new PipelinePointBean(latitude, longitude);
                                pipelinePointDao.add(pipelinePointBean);
                            }
                        } else {
                            for (int i = 0, length = select.size(); i < length; i++) {
                                pipelinePointBean = select.get(i);
                                latitude = pipelinePointBean.getLatitude();
                                longitude = pipelinePointBean.getLongitude();
                                mLatLng = new LatLng(latitude, longitude);
                                pipeline.add(mLatLng);
                            }
                        }
                        // 画管线
                        OverlayOptions overlayOptions = new PolylineOptions().width(10).color(0x0000ff).points(pipeline);
                        mBaiduMap.addOverlay(overlayOptions);

                        // 画必经点
                        drawStaffPoint();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        // 如果数据库为空，请求数据
        PipelinePointDao pipelinePointDao = new PipelinePointDao(getApplicationContext());
        List<PipelinePointBean> select = pipelinePointDao.select();
        if (select != null && select.size() > 0) {
            List<LatLng> pipeline = new ArrayList<>();
            for (int i = 0, length = select.size(); i < length; i++) {
                PipelinePointBean pipelinePointBean = select.get(i);
                double latitude = pipelinePointBean.getLatitude();
                double longitude = pipelinePointBean.getLongitude();
                LatLng latLng = new LatLng(latitude, longitude);
                pipeline.add(latLng);
            }
            // 画管线
            OverlayOptions overlayOptions = new PolylineOptions().width(10).color(0x0000ff).points(pipeline);
            mBaiduMap.addOverlay(overlayOptions);
        }
    }

    /**
     * 画必经点
     */
    private void drawStaffPoint() {
        mMap = new HashMap<>();
        mMap.put("staff_id", SpUtil.getInstance(getApplicationContext()).getString("id"));
        mOkHttpManager.postAsyncForm("", mMap, new OkHttpManager.DataCallback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showToast("网络连接失败");
            }

            @Override
            public void onResponse(String json) {
                if (json != null && json.length() > 0) {
                    StaffPointDao staffPointDao = new StaffPointDao(getApplicationContext());
                    List<StaffPointBean> select = staffPointDao.select();
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        int code = jsonObject.getInt("code");
                        if (code == 200) {
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            // 覆盖物集合
                            List<OverlayOptions> overlayOptionsList = new ArrayList<>();
                            if (select.size() != jsonArray.length()) {
                                // 如果数据库中的必经点个数不等于 json 中传过来的数组的长度，清空表中的数据
                                staffPointDao.delete();
                                // 在地图上显示必经点
                                for (int i = 0, length = jsonArray.length(); i < length; i++) {
                                    JSONObject data = (JSONObject) jsonArray.get(i);
                                    double latitude = Double.parseDouble(data.getString("latitude"));
                                    double longitude = Double.parseDouble(data.getString("longitude"));

                                    // 实例化对象
                                    LatLng latLng = new LatLng(latitude, longitude);
                                    // TODO: 2017/10/28 需要根据状态添加不同颜色的图片
                                    OverlayOptions overlayOptions = new MarkerOptions().position(latLng).icon(null);
                                    overlayOptionsList.add(overlayOptions);

                                    // 将新的数据添加到表中
                                    StaffPointBean staffPointBean = new StaffPointBean(latitude, longitude);
                                    staffPointDao.add(staffPointBean);
                                }
                            } else {
                                BitmapDescriptor bitmap;
                                // 将坐标点从数据库中读取出来，根据状态显示不同的图片
                                for (int i = 0, length = select.size(); i < length; i++) {
                                    StaffPointBean staffPointBean = select.get(i);
                                    double latitude = staffPointBean.getLatitude();
                                    double longitude = staffPointBean.getLongitude();
                                    LatLng latLng = new LatLng(latitude, longitude);
                                    // TODO: 2017/10/28 if 里面的判断条件需要更改
                                    if (latitude == 0.00) {
                                        bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher);
                                    } else {
                                        bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.app_launcher);
                                    }
                                    OverlayOptions overlayOptions = new MarkerOptions().position(latLng).icon(bitmap);
                                    overlayOptionsList.add(overlayOptions);
                                }
                            }
                            mHandler.sendEmptyMessageDelayed(0, 3 * 60 * 1000);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @OnClick(R.id.baidu_btn_clock)
    public void onClick() {
        long timeStamp = new Date().getTime();
        if (mIsClock) { // 结束打卡
            if (timeStamp - mTimeTemp >= 10 * 1000) {
                mIsClock = false;
                mTvTimer.setText("00:00:00");
                mBtnClock.setText("开始打卡");
                if (mService != null) {
                    stopService(mService);
                }
                // 解注册
                if (mTimeReceiver != null) {
                    unregisterReceiver(mTimeReceiver);
                }
            } else {
                showToast("打卡时间间隔太短！");
            }
        } else { // 开始打卡
            mTimeTemp = timeStamp;
            mBtnClock.setText("停止打卡");
            mService = new Intent(getApplicationContext(), UpdateClockService.class);
            mService.putExtra("timeStamp", timeStamp);
            startService(mService);
            // 注册广播
            registerBroadcastReceiver();
            mIsClock = true;
        }
    }

    /**
     * 注册广播
     */
    private void registerBroadcastReceiver() {
        mTimeReceiver = new TimeReceiver();
        IntentFilter filter = new IntentFilter(Constant.TIME_CHANGED_ACTION);
        registerReceiver(mTimeReceiver, filter);

    }

    /**
     * 时间改变的广播
     */
    public class TimeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Constant.TIME_CHANGED_ACTION.equals(action)) {
                Bundle bundle = intent.getExtras();
                String starTime = bundle.getString("time");
                mTvTimer.setText(starTime);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTextureMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTextureMapView.onPause();
        mHandler.removeCallbacksAndMessages(null);
        // 解注册
        if (mTimeReceiver != null) {
            unregisterReceiver(mTimeReceiver);
        }
        // 停止服务
        if (mService != null) {
            stopService(mService);
        }
    }

    @Override
    protected void onDestroy() {
        mBaiduMap.setMyLocationEnabled(false);
        mTextureMapView.onDestroy();
        mTextureMapView = null;
        super.onDestroy();
    }

}
