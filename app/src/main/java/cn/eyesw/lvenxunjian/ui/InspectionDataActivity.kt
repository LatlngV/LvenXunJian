package cn.eyesw.lvenxunjian.ui

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import cn.eyesw.lvenxunjian.R
import cn.eyesw.lvenxunjian.base.BaseActivity
import cn.eyesw.lvenxunjian.constant.ApiService
import cn.eyesw.lvenxunjian.constant.Constant
import cn.eyesw.lvenxunjian.utils.NetWorkUtil
import cn.eyesw.lvenxunjian.utils.ToolbarUtil
import com.baidu.location.BDLocation
import com.baidu.location.BDLocationListener
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_danger_data.*
import kotlinx.android.synthetic.main.activity_inspection_data.*
import me.weyye.hipermission.PermissionItem
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.ArrayList

class InspectionDataActivity : BaseActivity() {

    private var mDataId: String? = null
    private var mStatus: String? = null
    private var mLocationClient: LocationClient? = null
    private var mMyLocationListener: MyLocationListener? = null
    // 纬度
    private var mLatitude: Double = 0.0
    // 经度
    private var mLongitude: Double = 0.0
    // 地址
    private var mAddress: String? = null
    // 类型标志位
    private var mTypeFlag: String? = null
    // 请求数据接口
    private var mApiServer: ApiService? = null
    // 存储 ImageView 的集合
    private var mImageViewList: List<ImageView>? = null
    // 存储 url 图片的集合
    private var mImageUrl: MutableList<String>? = null
    // 存储 Bitmap 的集合
    private val mBitmapList = ArrayList<Bitmap>()
    // 点击图片的标志位
    private var mPosition = 0
    // Dialog 对象
    private var mDialog: Dialog? = null
    // 文件名
    private var mFileName: String? = null
    // 文件夹
    private var mOutFile: File? = null

    override fun getContentLayoutRes(): Int = R.layout.activity_inspection_data

    override fun initToolbar() {
        mDataId = intent.getStringExtra("dataId")
        mStatus = intent.getStringExtra("status")
        mTypeFlag = intent.getStringExtra("typeFlag")

        val title = intent.getStringExtra("title")

        val toolbarUtil = ToolbarUtil(this)
        toolbarUtil.setToolbar(inspection_data_toolbar, title)
    }

    override fun initView() {
        mApiServer = NetWorkUtil.getInstance().apiService
        mImageUrl = mutableListOf()
        mImageViewList = listOf(inspection_data_before_first, inspection_data_before_second, inspection_data_before_third, inspection_data_before_forth)
        if (mStatus == "0") { // 从创建来的
            mMyLocationListener = MyLocationListener()
            // 默认选中正常
            inspection_data_radio_group.check(R.id.inspection_data_rb_normal)
        } else if (mStatus == "1") { // 从记录来的
            inspection_data_radio_group.isEnabled = false
            inspection_data_rb_normal.isEnabled = false
            inspection_data_rb_repair.isEnabled = false
            inspection_data_rb_update.isEnabled = false
            inspection_data_et_detail.isEnabled = false
            inspection_data_et_bury_data.isEnabled = false
            inspection_data_et_finish_dat.isEnabled = false
            inspection_data_btn_save.visibility = View.INVISIBLE

            /* 根据 dataId 请求数据 */
            requestDataDetail()
        }

        inspection_data_before_first.setOnClickListener(mOnClick)
        inspection_data_before_second.setOnClickListener(mOnClick)
        inspection_data_before_third.setOnClickListener(mOnClick)
        inspection_data_before_forth.setOnClickListener(mOnClick)

        // button 点击事件
        inspection_data_btn_save.setOnClickListener { reportData() }
    }

    /**
     * 请求数据
     */
    private fun reportData() {

    }

    /**
     * 根据 dataId 请求数据
     */
    private fun requestDataDetail() {
        val dataDetail = mApiServer?.dataDetail(mDataId)
        dataDetail?.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                showToast(getString(R.string.network_error))
            }

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                val json = String(response?.body()?.bytes()!!)
                Log.d("tag", json)
                val jsonObject = JSONObject(json)
                val code = jsonObject.getInt("code")
                if (code == 200) {
                    val data = jsonObject.getJSONObject("data").getJSONObject("report_detail")
                    mLatitude = data.getString("latitude").toDouble()
                    mLongitude = data.getString("longitude").toDouble()
                    mAddress = data.getString("address")
                    val note = data.getString("note")
                    val status = data.getString("status") /* 状态，0 是正常，1 是建议维修，2是建议更换 */
                    val depth = data.getString("depth")
                    val finishTime = data.getString("finish_time")
                    val images = data.getJSONArray("images")

                    inspection_data_tv_latitude.text = mLatitude.toString()
                    inspection_data_tv_longitude.text = mLongitude.toString()
                    inspection_data_tv_address.text = mAddress
                    if (depth == "null") {
                        inspection_data_et_bury_data.setText("")
                    } else {
                        inspection_data_et_bury_data.setText(depth)
                    }
                    if (finishTime == "null") {
                        inspection_data_et_finish_dat.setText("")
                    } else {
                        inspection_data_et_finish_dat.setText(finishTime)
                    }
                    inspection_data_et_detail.setText(note)
                    for (i in 0 until images.length()) {
                        val url = images.optString(i)
                        if (!TextUtils.isEmpty(url)) {
                            mImageUrl?.add(url)
                            Picasso.with(mContext).load(url).into(mImageViewList?.get(i))
                        }
                    }
                    when (status) {
                        "0" -> inspection_data_radio_group.check(R.id.inspection_data_rb_normal)
                        "1" -> inspection_data_radio_group.check(R.id.inspection_data_rb_repair)
                        "2" -> inspection_data_radio_group.check(R.id.inspection_data_rb_update)
                    }
                }
            }

        })
    }

    /**
     * 点击事件
     */
    private val mOnClick = View.OnClickListener { v ->
        val intent = Intent(mContext, LargeImageActivity::class.java)
        when (v.id) {
            R.id.inspection_data_before_first -> {
                if (mStatus == "0") {
                    mPosition = 0
                    createDialog()
                } else if (mStatus == "1") {
                    if (mImageUrl?.size!! >= 1) {
                        intent.putExtra("url", mImageUrl?.get(0))
                        startActivity(intent)
                    }
                }
            }
            R.id.inspection_data_before_second -> {
                if (mStatus == "0") {
                    mPosition = 1
                    createDialog()
                } else if (mStatus == "1") {
                    if (mImageUrl?.size!! >= 2) {
                        intent.putExtra("url", mImageUrl?.get(1))
                        startActivity(intent)
                    }
                }
            }
            R.id.inspection_data_before_third -> {
                if (mStatus == "0") {
                    mPosition = 2
                    createDialog()
                } else if (mStatus == "1") {
                    if (mImageUrl?.size!! >= 3) {
                        intent.putExtra("url", mImageUrl?.get(2))
                        startActivity(intent)
                    }
                }
            }
            R.id.inspection_data_before_forth -> {
                if (mStatus == "0") {
                    mPosition = 3
                    createDialog()
                } else if (mStatus == "1") {
                    if (mImageUrl?.size!! >= 4) {
                        intent.putExtra("url", mImageUrl?.get(3))
                        startActivity(intent)
                    }
                }
            }
        }
    }

    /**
     * 创建 Dialog
     */
    private fun createDialog() {
        mDialog = Dialog(this, R.style.UserDialog)
        val view = View.inflate(mContext, R.layout.dialog_select_photo, null)
        mDialog?.setContentView(view)
        mDialog?.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        mDialog?.show()

        val tvCamera = view.findViewById<TextView>(R.id.user_dialog_tv_camera)
        val tvAlbum = view.findViewById<TextView>(R.id.user_dialog_tv_album)
        val tvCancel = view.findViewById<TextView>(R.id.user_dialog_tv_cancel)

        // 相机
        tvCamera.setOnClickListener({
            // 检查权限
            val permissions = ArrayList<PermissionItem>()
            permissions.add(PermissionItem(Manifest.permission.CAMERA, "打开相机", R.drawable.permission_ic_camera))
            permission(permissions) {
                // 打开相机
                takePhoto()
            }
        })
        // 相册
        tvAlbum.setOnClickListener({
            val intent = Intent(Intent.ACTION_PICK, null)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            startActivityForResult(intent, Constant.ALBUM_REQUEST_CODE)
            mDialog?.dismiss()
        })
        // 取消
        tvCancel.setOnClickListener({ mDialog?.dismiss() })
    }

    /**
     * 打开相机
     */
    private fun takePhoto() {
        val state = Environment.getExternalStorageState()
        if (state == Environment.MEDIA_MOUNTED) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            val outDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            if (!outDir.exists()) {
                outDir.mkdirs()
            }
            mFileName = System.currentTimeMillis().toString() + ".jpg"
            mOutFile = File(outDir, mFileName)

            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mOutFile))
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
            startActivityForResult(intent, Constant.CAMERA_REQUEST_CODE)

        } else {
            Toast.makeText(mContext, "请安装 sdcrad", Toast.LENGTH_SHORT).show()
        }
        mDialog?.dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            Constant.CAMERA_REQUEST_CODE -> startPhotoZoom(Uri.fromFile(mOutFile))
            Constant.ALBUM_REQUEST_CODE -> {
                if (data == null || data.data == null) {
                    return
                }
                startPhotoZoom(data.data)
            }
            Constant.CROP_REQUEST_CODE -> if (data != null) {
                setPicToView(data, mPosition)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * 裁剪图片
     */
    private fun startPhotoZoom(data: Uri?) {
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(data, "image/*")
        intent.putExtra("crop", "true")
        intent.putExtra("aspectX", 1)
        intent.putExtra("aspectY", 1)
        intent.putExtra("outputX", 300)
        intent.putExtra("outputY", 300)
        intent.putExtra("scale", true)
        intent.putExtra("scaleUpIfNeeded", true)
        intent.putExtra("return-data", true)
        intent.putExtra("noFaceDetection", true)
        startActivityForResult(intent, Constant.CROP_REQUEST_CODE)
    }

    /**
     * 设置图片
     */
    private fun setPicToView(data: Intent, position: Int) {
        val extras = data.extras
        if (extras != null) {
            val bitmap = extras.getParcelable<Bitmap>("data")
            when (position) {
                0 -> {
                    danger_data_before_first.setImageBitmap(bitmap)
                    mBitmapList.add(bitmap)
                }
                1 -> {
                    danger_data_before_second.setImageBitmap(bitmap)
                    mBitmapList.add(bitmap)
                }
                2 -> {
                    danger_data_before_third.setImageBitmap(bitmap)
                    mBitmapList.add(bitmap)
                }
                3 -> {
                    danger_data_before_forth.setImageBitmap(bitmap)
                    mBitmapList.add(bitmap)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mLocationClient?.registerLocationListener(mMyLocationListener)
        if (mStatus == "0") {
            val permissions = ArrayList<PermissionItem>()
            permissions.add(PermissionItem(Manifest.permission.ACCESS_FINE_LOCATION, "精确位置", R.drawable.permission_ic_location))
            permission(permissions) {
                val option = LocationClientOption()
                option.locationMode = LocationClientOption.LocationMode.Hight_Accuracy
                option.setCoorType("bd09ll")
                option.setScanSpan(20 * 1000)
                option.isOpenGps = true
                option.isLocationNotify = true
                option.setIsNeedAddress(true)
                option.setIgnoreKillProcess(false)
                option.SetIgnoreCacheException(false)
                option.setEnableSimulateGps(false)

                mLocationClient?.locOption = option
                mLocationClient?.start()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (mStatus == "0") {
            mLocationClient?.stop()
            mLocationClient?.unRegisterLocationListener(mMyLocationListener)
        }
    }

    /**
     * 百度地图定位监听
     */
    private inner class MyLocationListener : BDLocationListener {

        override fun onReceiveLocation(location: BDLocation?) {
            if (location == null) return
            // 纬度
            mLatitude = location.latitude
            // 经度
            mLongitude = location.longitude
            // 地址
            mAddress = location.addrStr

            inspection_data_tv_latitude.text = mLatitude.toString()
            inspection_data_tv_longitude.text = mLongitude.toString()
            inspection_data_tv_address.text = mAddress
        }
    }

}
