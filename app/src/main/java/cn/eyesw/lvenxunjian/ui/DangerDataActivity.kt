package cn.eyesw.lvenxunjian.ui

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.text.TextUtils
import android.view.View
import android.widget.*
import cn.eyesw.lvenxunjian.R
import cn.eyesw.lvenxunjian.base.BaseActivity
import cn.eyesw.lvenxunjian.constant.ApiService
import cn.eyesw.lvenxunjian.constant.Constant
import cn.eyesw.lvenxunjian.utils.*
import com.baidu.location.BDLocation
import com.baidu.location.BDLocationListener
import com.baidu.location.LocationClient
import kotlinx.android.synthetic.main.activity_danger_data.*
import com.baidu.location.LocationClientOption
import com.squareup.picasso.Picasso
import me.weyye.hipermission.PermissionItem
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.ArrayList

/**
 * 隐患数据，包括完成、未完成的和维修人员上报的
 */
class DangerDataActivity : BaseActivity() {

    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private var mAddress: String? = null
    private var mLocationClient: LocationClient? = null
    private val mMyLocationListener = MyLocationListener()
    /* 0 代表创建，维修前照片可点击；1 代表维修中， 维修后照片可点击；2 代表已完成，点击查看大图*/
    private var mDangerDataFlag: String? = null
    // ImageView 的标志位
    private var mPosition: Int = 0
    // 维修前图片的 url
    private val mBeforePhotoUrl = mutableListOf<String>()
    // 维修后图片的 url
    private val mAfterPhotoUrl = mutableListOf<String>()
    // 维修前 ImageView
    private var mBeforeImage: List<ImageView>? = null
    // 维修后 ImageView
    private var mAfterImage: List<ImageView>? = null
    // 拿到数据 id 在传照片
    private var mDataId: String? = null
    // 存储图片的结合
    private var mBitmapList: MutableList<Bitmap>? = null
    // 接口对象
    private var mApiService: ApiService? = null
    // Dialog 对象
    private var mDialog: Dialog? = null
    // 文件夹
    private var mOutFile: File? = null
    // 文件名
    private var mFileName: String? = null
    // 类型 flag
    private var mTypeFlag: String? = null
    // Image uri
    private var mImageUri: Uri? = null

    override fun getContentLayoutRes(): Int = R.layout.activity_danger_data

    override fun initToolbar() {
        // 获取前一个界面传递的数据
        mDangerDataFlag = intent.getStringExtra("status")
        mDataId = intent.getStringExtra("dataId")
        mTypeFlag = intent.getStringExtra("typeFlag")
        val title = intent.getStringExtra("title")

        val toolbarUtil = ToolbarUtil(this)
        toolbarUtil.setToolbar(danger_data_toolbar, title)
    }

    override fun initView() {
        // 网络请求
        mApiService = NetWorkUtil.getInstance().apiService
        mLocationClient = LocationClient(mContext)

        mBeforeImage = arrayListOf<ImageView>(danger_data_before_first, danger_data_before_second, danger_data_before_third, danger_data_before_forth)
        mAfterImage = arrayListOf<ImageView>(danger_data_after_first, danger_data_after_second, danger_data_after_third, danger_data_after_forth)
        mBitmapList = mutableListOf()

        danger_data_before_first.setOnClickListener(mImgOnClick)
        danger_data_before_second.setOnClickListener(mImgOnClick)
        danger_data_before_third.setOnClickListener(mImgOnClick)
        danger_data_before_forth.setOnClickListener(mImgOnClick)
        danger_data_after_first.setOnClickListener(mImgOnClick)
        danger_data_after_second.setOnClickListener(mImgOnClick)
        danger_data_after_third.setOnClickListener(mImgOnClick)
        danger_data_after_forth.setOnClickListener(mImgOnClick)

        if (mDangerDataFlag == "1") { // 请求维修前数据
            danger_data_tv_latitude.isEnabled = false
            danger_data_tv_longitude.isEnabled = false
            danger_data_tv_address.isEnabled = false
            danger_data_switch.isChecked = false // 自动设置为已完成
            danger_data_switch.isEnabled = false

            /* 根据 dataId 请求照片数据 */
            requestDataDetail()
        } else if (mDangerDataFlag == "2") { // 请求维修后数据
            // 只保留图片可点击并且 button 要隐藏
            danger_data_tv_latitude.isEnabled = false
            danger_data_tv_longitude.isEnabled = false
            danger_data_tv_address.isEnabled = false
            danger_data_switch.isEnabled = false
            danger_data_switch.isChecked = false // 设置为已完成
            danger_data_et_detail.isEnabled = false
            danger_data_btn_save.visibility = View.INVISIBLE

            /* 根据 dataId 请求照片数据 */
            requestDataDetail()
        }

        // 上传数据
        danger_data_btn_save.setOnClickListener {
            if (mDangerDataFlag == "0") {
                // 上传维修前数据
                updateBeforeRepairData()
            } else if (mDangerDataFlag == "1") {
                // 上传维修后数据
                updateAfterRepairData()
            }
        }
    }

    /**
     * 请求照片数据
     */
    private fun requestDataDetail() {
        val dataDetail = mApiService?.dataDetail(mDataId)
        dataDetail?.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                showToast(getString(R.string.network_error))
            }

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                val json = String(response?.body()?.bytes()!!)
                val jsonObject = JSONObject(json)
                val code = jsonObject.getInt("code")
                if (code == 200) {
                    val data = jsonObject.getJSONObject("data").getJSONObject("report_detail")
                    mLatitude = data.getString("latitude").toDouble()
                    mLongitude = data.getString("longitude").toDouble()
                    mAddress = data.getString("address")
                    val note = data.getString("note")
                    val images = data.getJSONArray("images")
                    danger_data_tv_latitude.text = mLatitude.toString()
                    danger_data_tv_longitude.text = mLongitude.toString()
                    danger_data_tv_address.text = mAddress
                    danger_data_et_detail.setText(note)
                    if (mDangerDataFlag == "1") {
                        for (i in 0 until images.length()) {
                            if (!TextUtils.isEmpty(images.optString(i))) {
                                val url = images.optString(i)
                                showImage(mBeforeImage?.get(i)!!, url)
                                // 将 url 添加到维修前图片集合
                                mBeforePhotoUrl.add(url)
                            }
                        }
                    } else if (mDangerDataFlag == "2") {
                        val jsonArray = data.getJSONArray("images_time")
                        val compareTime = jsonArray.get(0)
                        for (i in 0 until jsonArray.length()) {
                            val url = images.optString(i)
                            if (compareTime == jsonArray[i]) {
                                mBeforePhotoUrl.add(url)
                            } else {
                                mAfterPhotoUrl.add(url)
                            }
                        }
                        for (i in 0 until mBeforePhotoUrl.size) {
                            showImage(mBeforeImage?.get(i)!!, mBeforePhotoUrl[i])
                        }
                        for (i in 0 until mAfterPhotoUrl.size) {
                            showImage(mAfterImage?.get(i)!!, mAfterPhotoUrl[i])
                        }
                    }
                }
            }
        })
    }

    /**
     * Picasso 加载网络 url 显示照片
     */
    private fun showImage(imageView: ImageView, url: String) {
        Picasso.with(mContext).load(url).into(imageView)
    }

    /**
     * 上传维修前照片
     */
    private fun updateBeforeRepairData() {
        if (mBitmapList?.size == 0) {
            showToast("请选择要上传的照片")
            return
        }
        val spUtil = SpUtil.getInstance(mContext)
        val note = danger_data_et_detail.text.toString()
        val saveData = mApiService?.saveData(spUtil.getString("id"), "0", "0", mLatitude.toString(), mLongitude.toString(), mAddress, "", note, mTypeFlag)
        saveData?.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                val json = String(response?.body()?.bytes()!!)
                val obj = JSONObject(json)
                val code = obj.getInt("code")
                if (code == 200) {
                    mDataId = obj.getJSONObject("data").getString("id")
                    uploadPhoto()
                }
            }

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                Toast.makeText(mContext, "网络连接失败", Toast.LENGTH_SHORT).show()
            }

        })
    }

    /**
     * 上传维修后数据
     */
    private fun updateAfterRepairData() {
        if (mBitmapList?.size == 0) {
            showToast("请选择要上传的照片")
            return
        }
        val note = danger_data_et_detail.text.toString()
        val updateData = mApiService?.updateData(mDataId, mLatitude.toString(), mLongitude.toString(), mAddress, "", note, mDangerDataFlag, "1")
        updateData?.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                val json = String(response?.body()?.bytes()!!)
                // 解析 json 数据
                val jsonObject = JSONObject(json)
                val code = jsonObject.getInt("code")
                if (code == 200) {
                    // 上传照片
                    uploadPhoto()
                }
            }

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                showToast("网络连接失败")
            }

        })
    }

    override fun onStart() {
        super.onStart()
        mLocationClient?.registerLocationListener(mMyLocationListener)
        if (mDangerDataFlag == "0") {
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
            danger_data_tv_latitude.text = mLatitude.toString()
            danger_data_tv_longitude.text = mLongitude.toString()
            danger_data_tv_address.text = mAddress
        }
    }

    /**
     * 上传照片
     */
    private fun uploadPhoto() {
        // 上传了几张照片
        var index = 0
        // 循环上传图片
        (0 until mBitmapList?.size!!)
                .asSequence()
                .map { BitmapUtil.convertBitmapToString(mBitmapList?.get(it)) }
                .map { mApiService?.uploadImg(mDataId, "1", it, mFileName, "0") }
                .forEach {
                    it?.enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                            Toast.makeText(mContext, "网络连接失败", Toast.LENGTH_SHORT).show()
                        }

                        override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                            val json = String(response?.body()?.bytes()!!)
                            val obj = JSONObject(json)
                            val code = obj.getInt("code")
                            if (code == 200) {
                                showToast("上传成功")
                                index += 1
                                // 如果上传的照片数等于集合里存的图片的数量，就 finish 掉当前界面
                                if (index == mBitmapList?.size) {
                                    finish()
                                }
                            }
                        }
                    })
                }
    }

    /**
     * 图片的点击事件
     */
    private val mImgOnClick = View.OnClickListener { view ->
        val intent = Intent(mContext, LargeImageActivity::class.java)
        when (view.id) {
            R.id.danger_data_before_first -> {
                if (mDangerDataFlag == "0") {
                    mPosition = 0
                    createDialog()
                } else {
                    if (mBeforePhotoUrl.size >= 1) {
                        // 点击跳转查看大图
                        intent.putExtra("url", mBeforePhotoUrl[0])
                        startActivity(intent)
                    }
                }
            }
            R.id.danger_data_before_second -> {
                if (mDangerDataFlag == "0") {
                    mPosition = 1
                    createDialog()
                } else {
                    // 点击查看大图
                    if (mBeforePhotoUrl.size >= 2) {
                        // 点击跳转查看大图
                        intent.putExtra("url", mBeforePhotoUrl[1])
                        startActivity(intent)
                    }
                }
            }
            R.id.danger_data_before_third -> {
                if (mDangerDataFlag == "0") {
                    mPosition = 2
                    createDialog()
                } else {
                    // 点击查看大图
                    if (mBeforePhotoUrl.size >= 3) {
                        // 点击跳转查看大图
                        intent.putExtra("url", mBeforePhotoUrl[2])
                        startActivity(intent)
                    }
                }
            }
            R.id.danger_data_before_forth -> {
                if (mDangerDataFlag == "0") {
                    mPosition = 3
                    createDialog()
                } else {
                    // 点击查看大图
                    if (mBeforePhotoUrl.size >= 4) {
                        // 点击跳转查看大图
                        intent.putExtra("url", mBeforePhotoUrl[3])
                        startActivity(intent)
                    }
                }
            }
            R.id.danger_data_after_first -> {
                if (mDangerDataFlag == "1") {
                    mPosition = 100
                    createDialog()
                } else if (mDangerDataFlag == "2") {
                    // 点击查看大图
                    if (mAfterPhotoUrl.size >= 1) {
                        // 点击跳转查看大图
                        intent.putExtra("url", mAfterPhotoUrl[0])
                        startActivity(intent)
                    }
                }
            }
            R.id.danger_data_after_second -> {
                if (mDangerDataFlag == "1") {
                    mPosition = 101
                    createDialog()
                } else if (mDangerDataFlag == "2") {
                    // 点击查看大图
                    if (mAfterPhotoUrl.size >= 2) {
                        // 点击跳转查看大图
                        intent.putExtra("url", mAfterPhotoUrl[1])
                        startActivity(intent)
                    }
                }
            }
            R.id.danger_data_after_third -> {
                if (mDangerDataFlag == "1") {
                    mPosition = 102
                    createDialog()
                } else if (mDangerDataFlag == "2") {
                    // 点击查看大图
                    if (mAfterPhotoUrl.size >= 3) {
                        // 点击跳转查看大图
                        intent.putExtra("url", mAfterPhotoUrl[2])
                        startActivity(intent)
                    }
                }
            }
            R.id.danger_data_after_forth -> {
                if (mDangerDataFlag == "1") {
                    mPosition = 103
                    createDialog()
                } else if (mDangerDataFlag == "2") {
                    // 点击查看大图
                    if (mAfterPhotoUrl.size >= 4) {
                        // 点击跳转查看大图
                        intent.putExtra("url", mAfterPhotoUrl[3])
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

            val outDir = Environment.getExternalStorageDirectory()
            if (!outDir.exists()) {
                outDir.mkdirs()
            }
            mFileName = System.currentTimeMillis().toString() + ".jpg"
            mOutFile = File(outDir, mFileName)
            mImageUri = Uri.fromFile(mOutFile)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri)
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
            startActivityForResult(intent, Constant.CAMERA_REQUEST_CODE)

        } else {
            Toast.makeText(mContext, "请安装 sdcrad", Toast.LENGTH_SHORT).show()
        }
        mDialog?.dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            Constant.CAMERA_REQUEST_CODE -> startPhotoZoom(mImageUri)
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
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
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
                    mBitmapList?.add(bitmap)
                }
                1 -> {
                    danger_data_before_second.setImageBitmap(bitmap)
                    mBitmapList?.add(bitmap)
                }
                2 -> {
                    danger_data_before_third.setImageBitmap(bitmap)
                    mBitmapList?.add(bitmap)
                }
                3 -> {
                    danger_data_before_forth.setImageBitmap(bitmap)
                    mBitmapList?.add(bitmap)
                }
                100 -> {
                    danger_data_after_first.setImageBitmap(bitmap)
                    mBitmapList?.add(bitmap)
                }
                101 -> {
                    danger_data_after_second.setImageBitmap(bitmap)
                    mBitmapList?.add(bitmap)
                }
                102 -> {
                    danger_data_after_third.setImageBitmap(bitmap)
                    mBitmapList?.add(bitmap)
                }
                103 -> {
                    danger_data_after_forth.setImageBitmap(bitmap)
                    mBitmapList?.add(bitmap)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (mDangerDataFlag == "0") {
            mLocationClient?.stop()
            mLocationClient?.unRegisterLocationListener(mMyLocationListener)
        }
    }

    override fun onDestroy() {
        (0 until mBitmapList?.size!!)
                .mapNotNull { mBitmapList?.get(it) }
                .filterNot { it.isRecycled }
                .forEach { it.recycle() }
        super.onDestroy()
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

            danger_data_tv_latitude.text = mLatitude.toString()
            danger_data_tv_longitude.text = mLongitude.toString()
            danger_data_tv_address.text = mAddress
        }
    }

}
