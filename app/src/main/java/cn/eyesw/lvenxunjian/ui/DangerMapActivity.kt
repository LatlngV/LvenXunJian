package cn.eyesw.lvenxunjian.ui

import android.Manifest
import android.os.Bundle
import android.support.v7.app.AlertDialog
import cn.eyesw.lvenxunjian.R
import cn.eyesw.lvenxunjian.base.BaseActivity
import cn.eyesw.lvenxunjian.constant.ApiService
import cn.eyesw.lvenxunjian.utils.NetWorkUtil
import cn.eyesw.lvenxunjian.utils.ToolbarUtil
import com.baidu.location.BDLocationListener
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.utils.CoordinateConverter
import com.baidu.mapapi.map.BaiduMap
import org.json.JSONArray
import org.json.JSONObject
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.map.OverlayOptions
import kotlinx.android.synthetic.main.activity_danger_map.*
import me.weyye.hipermission.PermissionItem
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class DangerMapActivity : BaseActivity() {

    private var mBaiduMap: BaiduMap? = null
    private var mConverter: CoordinateConverter? = null
    private var mLocationClient: LocationClient? = null
    private var mLatLng: LatLng? = null
    private var mOverlayOptions: OverlayOptions? = null
    private var mApiService: ApiService? = null
    private var mIsFirst = true

    override fun getContentLayoutRes(): Int = R.layout.activity_danger_map

    override fun initToolbar() {
        val toolbarUtil = ToolbarUtil(this)
        toolbarUtil.setToolbar(danger_map_toolbar, "隐患地图")
    }

    override fun initView() {
        mApiService = NetWorkUtil.getInstance().apiService
        mBaiduMap = danger_map_view.map
        mBaiduMap?.mapType = BaiduMap.MAP_TYPE_NORMAL
        // 坐标转换
        mConverter = CoordinateConverter()
        mConverter?.from(CoordinateConverter.CoordType.COMMON)
        mLocationClient = LocationClient(applicationContext)
        mLocationClient?.registerLocationListener(mBDLocationListener)

        drawDangerPoint()
    }

    override fun onStart() {
        super.onStart()
        val permissions = ArrayList<PermissionItem>()
        permissions.add(PermissionItem(Manifest.permission.ACCESS_FINE_LOCATION, "精确位置", R.drawable.permission_ic_location))
        permission(permissions) {
            // 初始化位置
            initLocation()
            // 百度地图适配 Android7.0
            mLocationClient?.restart()
        }
    }

    override fun onResume() {
        super.onResume()
        danger_map_view.onResume()
    }

    override fun onPause() {
        super.onPause()
        danger_map_view.onPause()
        mLocationClient?.stop()
    }

    override fun onDestroy() {
        mLocationClient?.unRegisterLocationListener(mBDLocationListener)
        danger_map_view.onDestroy()
        super.onDestroy()
    }

    /**
     * 初始化位置
     */
    private fun initLocation() {
        val option = LocationClientOption()

        option.locationMode = LocationClientOption.LocationMode.Hight_Accuracy
        option.setCoorType("bd09ll")
        option.setScanSpan(1000 * 20)
        option.isOpenGps = true
        mLocationClient?.locOption = option

        // 开始定位
        mLocationClient?.start()
        // 开启定位图层
        mBaiduMap?.isMyLocationEnabled = true
    }

    /**
     * 画隐患点
     */
    private fun drawDangerPoint() {
        val dangerPoint = mApiService?.dangerPoint()
        dangerPoint?.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                // 获取 json 数据
                val json = String(response?.body()?.bytes()!!)
                // 解析 json
                val jsonArray = JSONArray(json)
                val length = jsonArray.length() - 1
                for (i in 0..length) {
                    val jsonObject: JSONObject = jsonArray.get(i) as JSONObject
                    val latitude: Double = jsonObject.getString("latitude").toDouble()
                    val longitude: Double = jsonObject.getString("longitude").toDouble()

                    mLatLng = LatLng(latitude, longitude)
                    mConverter?.coord(mLatLng)
                    mLatLng = mConverter?.convert()

                    val status = jsonObject.getString("status")
                    var bitmapDescriptor: BitmapDescriptor? = null
                    if (status == "0") {
                        bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.route_green)
                    } else if (status == "1") {
                        bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.route_red)
                    }
                    if (bitmapDescriptor != null) {
                        mOverlayOptions = MarkerOptions()
                                .position(mLatLng)
                                .icon(bitmapDescriptor)
                    }
                    val typeName = jsonObject.getString("type_name")
                    val address = jsonObject.getString("addr")
                    val createTime = jsonObject.getString("ctime")
                    val bundle = Bundle()
                    bundle.putSerializable("info", typeName)
                    bundle.putSerializable("address", address)
                    bundle.putSerializable("ctime", createTime)
                    val marker = mBaiduMap?.addOverlay(mOverlayOptions) as Marker?
                    marker?.extraInfo = bundle
                }
                mBaiduMap?.setOnMarkerClickListener { marker ->
                    val extraInfo = marker?.extraInfo
                    val info = extraInfo?.getSerializable("info") as String
                    val address = extraInfo.getSerializable("address") as String
                    val createTime = extraInfo.getSerializable("ctime") as String
                    AlertDialog.Builder(this@DangerMapActivity)
                            .setTitle("隐患详情")
                            .setMessage("隐患名称: $info\n隐患地址: $address\n创建时间: $createTime")
                            .setCancelable(false)
                            .setPositiveButton("确定", null)
                            .setNegativeButton("取消", null)
                            .show()
                    true
                }
            }

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                showToast("网络连接失败")
            }

        })
    }

    /**
     * 位置监听
     */
    private val mBDLocationListener = BDLocationListener { location ->
        val latitude: Double = location?.latitude!!
        val longitude: Double = location.longitude
        val radius: Float = location.radius
        val myLocationData = MyLocationData.Builder()
                .accuracy(radius)
                .direction(0f)
                .latitude(latitude)
                .longitude(longitude)
                .build()
        // 设置定位数据
        mBaiduMap?.setMyLocationData(myLocationData)
        val bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker)
        val myLocationConfiguration = MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, false, bitmapDescriptor)
        mBaiduMap?.setMyLocationConfiguration(myLocationConfiguration)
        val latLng = LatLng(latitude, longitude)
        val mapStatus = MapStatus.Builder()
                .target(latLng)
                .zoom(18.0f)
                .build()
        if (mIsFirst) {
            mIsFirst = false
            mBaiduMap?.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus))
        }
    }

}
