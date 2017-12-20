package cn.eyesw.lvenxunjian

import android.Manifest
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.view.KeyEvent
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import cn.eyesw.lvenxunjian.base.BaseActivity
import cn.eyesw.lvenxunjian.constant.Constant
import cn.eyesw.lvenxunjian.ui.*
import cn.eyesw.lvenxunjian.utils.BaiduMapUtil
import cn.eyesw.lvenxunjian.utils.SpUtil
import cn.eyesw.lvenxunjian.utils.UpdateManager
import com.baidu.location.BDLocationListener
import com.baidu.location.LocationClient
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import kotlinx.android.synthetic.main.activity_home.*
import me.weyye.hipermission.PermissionItem
import java.util.ArrayList

/**
 * 管理员 app 主界面
 */
class HomeActivity : BaseActivity(), OnNavigationItemSelectedListener {

    // 百度地图实例
    private var mBaiduMap: BaiduMap? = null
    private var mLocationClient: LocationClient? = null
    private var mSpUtil: SpUtil? = null
    // 是不是第一次定位
    private var mIsFirst = true
    // 记录按下返回键的时间
    private var startTime: Long = 0

    override fun getContentLayoutRes(): Int = R.layout.activity_home

    override fun initToolbar() {
        val toggle = ActionBarDrawerToggle(this, home_drawer_layout, home_toolbar, R.string.drawer_open, R.string.drawer_close)
        home_drawer_layout?.addDrawerListener(toggle)
        toggle.syncState()
    }

    override fun initView() {
        mSpUtil = SpUtil.getInstance(mContext)
        // 百度地图实例
        mBaiduMap = home_map_view.map

        // 检查版本
        val versionUpdate = mSpUtil?.getBoolean(Constant.VERSION_UPDATE) as Boolean
        if (versionUpdate) {
            checkVersionDialog()
        }

        initNavigationView()
    }

    override fun onStart() {
        super.onStart()
        mLocationClient = LocationClient(mContext)
        mLocationClient?.registerLocationListener(mBDLocationListener)

        // 授权
        val permissions = ArrayList<PermissionItem>()
        permissions.add(PermissionItem(Manifest.permission.ACCESS_FINE_LOCATION, "精确位置", R.drawable.permission_ic_location))
        permission(permissions) { BaiduMapUtil.initLocation(mLocationClient, mBaiduMap) }
    }

    override fun onResume() {
        super.onResume()
        home_map_view.onResume()
    }

    override fun onPause() {
        super.onPause()
        mLocationClient?.stop()
        mLocationClient?.unRegisterLocationListener(mBDLocationListener)
        mBaiduMap?.isMyLocationEnabled = false
        home_map_view.onPause()
    }

    override fun onDestroy() {
        home_map_view.onDestroy()
        super.onDestroy()
    }

    /**
     * 版本更新提示框
     */
    private fun checkVersionDialog() {
        AlertDialog.Builder(this)
                .setTitle("版本更新")
                .setMessage("有新版本，是否升级？")
                .setPositiveButton("确定") { dialog, _ ->
                    val apkUrl = mSpUtil?.getString(Constant.APK_URL)
                    val permissions = ArrayList<PermissionItem>()
                    permissions.add(PermissionItem(Manifest.permission.WRITE_EXTERNAL_STORAGE, "内存读写", R.drawable.permission_ic_storage))
                    permission(permissions) {
                        // 更新
                        val updateManager = UpdateManager(mContext, apkUrl)
                        updateManager.checkUpdateInfo()
                        dialog.dismiss()
                    }
                }
                .setNegativeButton("取消", null)
                .setCancelable(false)
                .show()
    }

    /**
     * 实现 NavigationView 的监听，重写 onNavigationItemSelected 方法
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        // 数据采集
            R.id.menu_home_data_collection -> startActivity(DataCollectionActivity::class.java)
        // 抢维修
            R.id.menu_home_repair -> startActivity(UrgentRepairActivity::class.java)
        // 隐患地图
            R.id.menu_home_danger_map -> startActivity(DangerMapActivity::class.java)
        // 退出登录
            R.id.menu_home_log_out -> {
                startActivity(LoginActivity::class.java)
                mSpUtil?.remove("roleName")
                mSpUtil?.remove("isLogin")
                mSpUtil?.remove(Constant.VERSION_UPDATE)
                mSpUtil?.remove(Constant.APK_URL)
                finish()
            }
        }
        home_drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun initNavigationView() {
        val headerView = home_navigation_view.getHeaderView(0)
        headerView.findViewById<TextView>(R.id.navigation_view_staff_name).text = mSpUtil?.getString("staffName")
        headerView.findViewById<TextView>(R.id.navigation_view_staff_role).text = mSpUtil?.getString("roleName")
        headerView.findViewById<LinearLayout>(R.id.navigation_view_ll_setting).setOnClickListener {
            startActivity(UserActivity::class.java)
        }

        home_navigation_view.itemIconTintList = null

        // 设置抽屉的监听
        home_navigation_view.setNavigationItemSelectedListener(this)
    }

    private val mBDLocationListener = BDLocationListener { location ->
        // 获取纬度信息
        val latitude: Double = location?.latitude!!
        // 获取经度信息
        val longitude: Double = location.longitude
        // 获取定位精度，默认值为 0.0f
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
        val myLocationConfiguration = MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, bitmapDescriptor)
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
        val city: String = location.city
        home_toolbar.title = city
    }

    /**
     * 按下返回键时，先判断 DrawerLayout 是不是打开的
     * 如果 DrawerLayout 是打开的，就让 DrawerLayout 关闭
     * 否则在 2s 时间内按两下返回键就退出程序
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (home_drawer_layout.isDrawerOpen(GravityCompat.START)) {
                home_drawer_layout.closeDrawer(GravityCompat.START)
            } else {
                if (System.currentTimeMillis() - startTime > 2000) {
                    Toast.makeText(mContext, "再按一次退出", Toast.LENGTH_SHORT).show()
                    startTime = System.currentTimeMillis()
                    return true
                } else {
                    System.exit(0)
                }
            }
        }
        return false
    }

}
