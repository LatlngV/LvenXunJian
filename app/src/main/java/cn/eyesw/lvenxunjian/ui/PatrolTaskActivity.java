package cn.eyesw.lvenxunjian.ui;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.baidu.mapapi.map.UiSettings;
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
import cn.eyesw.greendao.LatlngEntityDao;
import cn.eyesw.lvenxunjian.LvenXunJianApplication;
import cn.eyesw.lvenxunjian.R;
import cn.eyesw.lvenxunjian.base.BaseActivity;
import cn.eyesw.lvenxunjian.bean.LatlngEntity;
import cn.eyesw.lvenxunjian.constant.Constant;
import cn.eyesw.lvenxunjian.constant.NetworkApi;
import cn.eyesw.lvenxunjian.service.UpdateClockService;
import cn.eyesw.lvenxunjian.utils.DensityUtil;
import cn.eyesw.lvenxunjian.utils.OkHttpManager;
import cn.eyesw.lvenxunjian.utils.SpUtil;
import cn.eyesw.lvenxunjian.utils.ToolbarUtil;
import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;
import okhttp3.Call;

/**
 * 巡检任务
 */
public class PatrolTaskActivity extends BaseActivity {

    private String mLongitude;
    private String mLatitude;
    private String mAddress;
    private OkHttpManager mOkHttpManager;
    private JSONArray mPositions;
    private SpUtil mSpUtil;

    private Intent mService;
    private TimeReceiver mTimeReceiver;

    /* 获取状态的集合 */
    private List<String> mStatus;
    /* 用来判断是不是上班 */
    private boolean mFlag = true;
    /* 百度地图 */
    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient = null;
    private LatLng mLatLng;
    private CoordinateConverter mConverter;
    private BitmapDescriptor mBitmapDescriptor;
    private LatlngEntityDao mLatlngEntityDao;
    private LatlngEntity mLatlngEntity;
    private boolean mIsFirst = true;

    @BindView(R.id.patrol_task_toolbar)
    protected Toolbar mToolbar;
    @BindView(R.id.patrol_task_btn_start)
    protected Button mBtnStart;
    @BindView(R.id.patrol_task_first_ll_container)
    protected LinearLayout mFirstLlContainer;
    @BindView(R.id.patrol_task_second_ll_container)
    protected LinearLayout mSecondLlContainer;
    @BindView(R.id.patrol_task_tv_complete)
    protected TextView mTvPositionComplete;
    @BindView(R.id.patrol_task_tv_total)
    protected TextView mTvPositionTotal;
    @BindView(R.id.patrol_task_texture_map_view)
    protected TextureMapView mTextureMapView;
    @BindView(R.id.patrol_task_tv_timer)
    protected TextView mTvTimer;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    // 获取必经点状态
                    getStaffPointState();
                    break;
            }
        }
    };

    @Override
    protected int getContentLayoutRes() {
        return R.layout.activity_patrol_task;
    }

    @Override
    protected void initToolbar() {
        ToolbarUtil toolbarUtil = new ToolbarUtil(this);
        toolbarUtil.setToolbar(mToolbar, "巡检任务");
    }

    @Override
    protected void initView() {
        mLatlngEntityDao = LvenXunJianApplication.getDaoSession().getLatlngEntityDao();
        mSpUtil = SpUtil.getInstance(mContext);
        mOkHttpManager = OkHttpManager.getInstance();

        // 初始化百度地图
        mBaiduMap = mTextureMapView.getMap();
        // 设置百度地图模式: 卫星模式
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        // 设置地图不旋转
        UiSettings uiSettings = mBaiduMap.getUiSettings();
        uiSettings.setRotateGesturesEnabled(false);
        // 坐标转换
        mConverter = new CoordinateConverter();
        mConverter.from(CoordinateConverter.CoordType.COMMON);

        List<PermissionItem> permissions = new ArrayList<>();
        permissions.add(new PermissionItem(Manifest.permission.ACCESS_FINE_LOCATION, "精确位置", R.drawable.permission_ic_location));
        HiPermission.create(this)
                .title("授权")
                .permissions(permissions)
                .animStyle(R.style.PermissionAnimModal)
                .filterColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme()))
                .msg("开启权限")
                .checkMutiPermission(new PermissionCallback() {
                    @Override
                    public void onClose() {
                        showToast("定位授权关闭");
                    }

                    @Override
                    public void onFinish() {
                        // 多个授权的时候 6.0 以下走此方法
                        baiduLocation();
                    }

                    @Override
                    public void onDeny(String permission, int position) {
                    }

                    @Override
                    public void onGuarantee(String permission, int position) {
                        // 授权定位
                        baiduLocation();
                    }
                });
    }

    private void baiduLocation() {
        MyLocationListener myLocationListener = new MyLocationListener();
        // 定位初始化
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myLocationListener);
        LocationClientOption option = new LocationClientOption();
        // 打开 gps
        option.setOpenGps(true);
        // 设置坐标类型
        option.setCoorType("bd09ll");
        // 定位的频率
        option.setScanSpan(1000 * 20);
        mLocationClient.setLocOption(option);
        mLocationClient.start();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 获取位置
        getPosition();
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
            mTimeReceiver = null;
        }
        // 停止服务
        if (mService != null) {
            stopService(mService);
        }
    }

    @Override
    protected void onDestroy() {
        if (mLocationClient != null && mLocationClient.isStarted()) {
            mLocationClient.stop();
            // 当不需要定位图层时关闭定位图层
            mBaiduMap.setMyLocationEnabled(false);
        }
        mTextureMapView.onDestroy();
        mTextureMapView = null;
        super.onDestroy();
    }

    /**
     * 获取打卡状态
     */
    private void getClockStatus() {
        Map<String, String> map = new HashMap<>();
        map.put("staff_id", mSpUtil.getString("id"));
        mOkHttpManager.postAsyncForm(NetworkApi.STAFF_STATUS, map, new OkHttpManager.DataCallback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showToast("网络连接失败");
            }

            @Override
            public void onResponse(String json) {
                try {
                    JSONObject object = new JSONObject(json);
                    int code = object.getInt("code");
                    if (code == 200) {
                        JSONObject data = object.getJSONObject("data").getJSONObject("result");
                        String status = data.getString("status");
                        if (status.equals("0")) {
                            mBtnStart.setText("开始巡检");
                        } else if (status.equals("1")) {
                            mBtnStart.setText("结束巡检");
                            mFlag = false;
                            // 开启服务
                            long time = data.getLong("time");
                            mService = new Intent(getApplicationContext(), UpdateClockService.class);
                            mService.putExtra("timeStamp", new Date().getTime() - time * 1000);
                            startService(mService);
                            // 注册广播
                            registerBroadcastReceiver();
                        }
                        // 获取必经点的集合
                        getStaffPointState();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 获取必经点的状态
     */
    private void getStaffPointState() {
        mStatus = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        map.put("staff_id", mSpUtil.getString("id"));
        mOkHttpManager.postAsyncForm(NetworkApi.LINE_POINT_LIST, map, new OkHttpManager.DataCallback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showToast("网络连接失败");
            }

            @Override
            public void onResponse(String json) {
                try {
                    JSONObject object = new JSONObject(json);
                    int code = object.getInt("code");
                    if (code == 200) {
                        mPositions = object.getJSONArray("kaoqin_positions");
                        for (int i = 0; i < mPositions.length(); i++) {
                            JSONObject data = (JSONObject) mPositions.get(i);
                            String status = data.getString("status");
                            mStatus.add(status);
                        }

                        // 获取管道数据画管线
                        drawPipeline();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 画管线
     */
    private void drawPipeline() {
        List<LatlngEntity> select = mLatlngEntityDao.queryBuilder()
                .where(LatlngEntityDao.Properties.Flag.eq("pipeline"))
                .list();

        if (select.size() > 0) { // 集合的长度大于 0，从数据库中读取数据，画出来
            // 先清空百度地图原来画的
            mBaiduMap.clear();
            List<LatLng> points = new ArrayList<>();
            for (int i = 0, length = select.size(); i < length; i++) {
                mLatlngEntity = select.get(i);
                double latitude = mLatlngEntity.getLatitude();
                double longitude = mLatlngEntity.getLongitude();
                mLatLng = new LatLng(latitude, longitude);
                points.add(mLatLng);
            }
            //绘制折线
            OverlayOptions ooPolyline = new PolylineOptions().width(10).color(0xAA0000FF).points(points);
            mBaiduMap.addOverlay(ooPolyline);

            // 根据状态画巡线员必经点
            drawStaffPoint();
        } else { // 结合的长度等于 0，请求网络数据并进行坐标转换，将大地坐标系转换为百度坐标系，将转换的坐标系保存到数据库中
            // 请求管道数据
            requestPipelineDData();
        }
    }

    /**
     * 根据状态画巡线员必经点
     */
    private void drawStaffPoint() {
        List<LatlngEntity> select = mLatlngEntityDao.queryBuilder()
                .where(LatlngEntityDao.Properties.Flag.eq("staffPoint"))
                .list();

        if (select.size() > 0) {
            //创建 OverlayOptions 的集合
            List<OverlayOptions> options = new ArrayList<>();
            double latitude;
            double longitude;
            for (int i = 0, length = select.size(); i < length; i++) {
                String status = mStatus.get(i);
                mLatlngEntity = select.get(i);
                latitude = mLatlngEntity.getLatitude();
                longitude = mLatlngEntity.getLongitude();
                mLatLng = new LatLng(latitude, longitude);
                if (status.equals("1")) {
                    mBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.route_green);
                } else if (status.equals("0")) {
                    mBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.route_red);
                }
                OverlayOptions option = new MarkerOptions()
                        .position(mLatLng)
                        .icon(mBitmapDescriptor);
                options.add(option);
            }
            mBaiduMap.addOverlays(options);

            // 显示完成情况
            showCompleteSituation();
        } else {
            // 请求必经点数据
            requestStaffPointData();
        }
    }

    /**
     * 请求管道数据
     */
    private void requestPipelineDData() {
        Map<String, String> map = new HashMap<>();
        map.put("staff_id", mSpUtil.getString("id"));
        mOkHttpManager.postAsyncForm(NetworkApi.PIPELINE_POINT, map, new OkHttpManager.DataCallback() {
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
                        JSONArray jsonArray = jsonObject.getJSONArray("positions");
                        jsonArray = ((JSONObject) jsonArray.get(0)).getJSONArray("positions");
                        List<LatLng> pointList = new ArrayList<>();
                        double latitude;
                        double longitude;
                        for (int i = 0, length = jsonArray.length(); i < length; i++) {
                            JSONObject data = (JSONObject) jsonArray.get(i);
                            latitude = Double.parseDouble(data.getString("latitude"));
                            longitude = Double.parseDouble(data.getString("longitude"));
                            mLatLng = new LatLng(latitude, longitude);
                            pointList.add(mLatLng);

                            // LatLng 待转换坐标
                            mConverter.coord(mLatLng);
                            LatLng latLng = mConverter.convert();
                            mLatlngEntity = new LatlngEntity(latLng.latitude, latLng.longitude, "pipeline");
                            mLatlngEntityDao.insert(mLatlngEntity);
                        }
                        //绘制折线
                        OverlayOptions ooPolyline = new PolylineOptions().width(10).color(0xAA0000FF).points(pointList);
                        mBaiduMap.addOverlay(ooPolyline);

                        // 请求必经点数据
                        requestStaffPointData();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 请求必经点数据
     */
    private void requestStaffPointData() {
        Map<String, String> map = new HashMap<>();
        map.put("staff_id", mSpUtil.getString("id"));
        mOkHttpManager.postAsyncForm(NetworkApi.STAFF_POINT, map, new OkHttpManager.DataCallback() {
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
                        JSONArray jsonArray = jsonObject.getJSONArray("kaoqin_positions");
                        jsonArray = ((JSONObject) jsonArray.get(0)).getJSONArray("kaoqin_positions");
                        List<OverlayOptions> options = new ArrayList<>();
                        double latitude;
                        double longitude;
                        for (int i = 0, length = jsonArray.length(); i < length; i++) {
                            String status = mStatus.get(i);
                            JSONObject data = (JSONObject) jsonArray.get(i);
                            latitude = Double.parseDouble(data.getString("latitude"));
                            longitude = Double.parseDouble(data.getString("longitude"));
                            mLatLng = new LatLng(latitude, longitude);
                            if (status.equals("1")) {
                                //构建 Marker 图标
                                mBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.route_green);
                            } else if (status.equals("0")) {
                                mBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.route_red);
                            }
                            //创建 OverlayOptions 属性
                            OverlayOptions option = new MarkerOptions()
                                    .position(mLatLng)
                                    .icon(mBitmapDescriptor);
                            options.add(option);

                            // 将必经点保存到数据库中
                            mConverter.coord(mLatLng);
                            LatLng latLng = mConverter.convert();
                            mLatlngEntity = new LatlngEntity(latLng.latitude, latLng.longitude, "staffPoint");
                            mLatlngEntityDao.insert(mLatlngEntity);
                        }
                        mBaiduMap.addOverlays(options);

                        // 显示完成情况
                        showCompleteSituation();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 显示完成情况
     */
    private void showCompleteSituation() {
        mTvPositionTotal.setText(mPositions.length() + "");

        // 先判断 LinearLayout 有没有添加子 View，如果有就移除
        if (mFirstLlContainer.getChildCount() > 0 || mSecondLlContainer.getChildCount() > 0) {
            mFirstLlContainer.removeAllViews();
            mSecondLlContainer.removeAllViews();
        }
        // 记录完成点的个数
        int position = 0;
        // 在 LinearLayout 里动态添加 TextView
        TextView textView;
        // 设置 TextView 的宽高
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                DensityUtil.dip2px(mContext, 22f),
                DensityUtil.dip2px(mContext, 22f));
        // 动态向 LinearLayout 中添加 TextView
        for (int i = 0; i < mPositions.length(); i++) {
            String status = mStatus.get(i);
            textView = new TextView(mContext);
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(Color.BLACK);
            textView.setText(i + 1 + "");

            /*
             * 从存储状态的集合里获取状态
             * 如果状态为 “0” 就设置为红色
             * 如果状态为 “1” 就设置为绿色
             */
            if (status.equals("0")) {
                textView.setBackgroundResource(R.drawable.patrol_task_text_view_red_bg);
            } else if (status.equals("1")) {
                textView.setBackgroundResource(R.drawable.patrol_task_text_view_green_bg);
                position++;
            }

            if (i == 0) {
                params.leftMargin = DensityUtil.dip2px(mContext, 5f);
            }
            if (i == mPositions.length() - 1) {
                params.rightMargin = DensityUtil.dip2px(mContext, 10f);
            }

            params.leftMargin = DensityUtil.dip2px(mContext, 3f);
            textView.setLayoutParams(params);

            if (i < mPositions.length() / 2 && mPositions.length() > 10) {
                mFirstLlContainer.addView(textView);
            } else {
                mSecondLlContainer.addView(textView);
            }

            mTvPositionComplete.setText(position + "");
        }

        // 判断完成点的个数和总点数相同，不在向服务器请求消息
        if (position == mPositions.length()) {
            mHandler.removeCallbacksAndMessages(null);
        } else {
            mHandler.sendEmptyMessageDelayed(0, 1000 * 20);
        }
    }

    /**
     * 获取位置
     */
    private void getPosition() {
        Map<String, String> map = new HashMap<>();
        map.put("staff_id", mSpUtil.getString("id"));
        mOkHttpManager.postAsyncForm(NetworkApi.GPS, map, new OkHttpManager.DataCallback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showToast("网络连接失败");
            }

            @Override
            public void onResponse(String json) {
                try {
                    JSONObject object = new JSONObject(json);
                    int code = object.getInt("code");
                    if (code == 200) {
                        JSONObject data = object.getJSONObject("data");
                        mLongitude = data.getString("longitude");
                        mLatitude = data.getString("latitude");
                        mAddress = data.getString("address");
                    }
                    // 获取打卡状态
                    getClockStatus();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 判断 Button 的内容
     * 如果内容为 “开始巡检”，向 Handler 发送消息
     * 如果内容为 “结束巡检”，移除 Handler 里所有消息
     */
    @OnClick(R.id.patrol_task_btn_start)
    public void onClick() {
        if (mFlag) {
            if (mPositions.length() == 0) {
                Dialog dialog = new Dialog(this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                View view = View.inflate(mContext, R.layout.dialog_patrol_task, null);
                dialog.setContentView(view);
                dialog.show();
                return;
            }
            mBtnStart.setText("结束巡检");

            mFlag = false;
            // 开启服务
            mService = new Intent(getApplicationContext(), UpdateClockService.class);
            mService.putExtra("timeStamp", new Date().getTime());
            startService(mService);
            // 注册广播
            registerBroadcastReceiver();
            mHandler.removeCallbacksAndMessages(null);
            // 获取必经点状态
            getStaffPointState();
        } else {
            mBtnStart.setText("开始巡检");
            mFlag = true;
            mHandler.removeCallbacksAndMessages(null);
            // 关闭服务
            stopService(mService);
            // 广播解注册
            if (mTimeReceiver != null) {
                unregisterReceiver(mTimeReceiver);
                mTimeReceiver = null;
            }
            mTvTimer.setText("00:00:00");
        }
        isSign();
    }

    /**
     * 判断 flag 的状态，如果 flag 为 true，就开始打卡，如果 flag 为 false，就结束打卡
     */
    private void isSign() {
        Map<String, String> map = new HashMap<>();
        map.put("staff_id", mSpUtil.getString("id"));
        map.put("longitude", mLongitude);
        map.put("latitude", mLatitude);
        map.put("address", mAddress);
        mOkHttpManager.postAsyncForm(url(), map, new OkHttpManager.DataCallback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(mContext, "网络连接失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String json) {
                try {
                    JSONObject object = new JSONObject(json);
                    int code = object.getInt("code");
                    if (code == 200) {
                        if (!mFlag) {
                            Toast.makeText(mContext, "打卡成功，任务开始", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, "打卡结束，任务结束", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 如果 flag 为 true：返回开始打卡的 API
     * 如果 flag 为 false：返回结束打卡的 API
     */
    private String url() {
        if (!mFlag) {
            return NetworkApi.STAFF_SIGN;
        } else {
            return NetworkApi.STAFF_UNSIGN;
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
     * 百度地图定位实现类
     */
    private class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation == null || mTextureMapView == null) {
                return;
            }
            double latitude = bdLocation.getLatitude();
            double longitude = bdLocation.getLongitude();

            MyLocationData myLocationData = new MyLocationData.Builder()
                    .accuracy(bdLocation.getRadius())
                    .direction(0)
                    .latitude(latitude)
                    .longitude(longitude)
                    .build();
            // 设置定位数据
            mBaiduMap.setMyLocationData(myLocationData);
            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker);
            MyLocationConfiguration myLocationConfiguration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, bitmapDescriptor);
            mBaiduMap.setMyLocationConfiguration(myLocationConfiguration);
            LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(ll).zoom(18.0f);
            if (mIsFirst) {
                mIsFirst = false;
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }
    }

    /**
     * 时间改变的广播
     */
    private class TimeReceiver extends BroadcastReceiver {

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

}
