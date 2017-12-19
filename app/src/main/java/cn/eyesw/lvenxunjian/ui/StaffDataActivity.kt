package cn.eyesw.lvenxunjian.ui

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import cn.eyesw.lvenxunjian.R
import cn.eyesw.lvenxunjian.base.BaseActivity
import cn.eyesw.lvenxunjian.constant.ApiService
import cn.eyesw.lvenxunjian.constant.Constant
import cn.eyesw.lvenxunjian.utils.BitmapUtil
import cn.eyesw.lvenxunjian.utils.NetWorkUtil
import cn.eyesw.lvenxunjian.utils.ToolbarUtil
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_staff_data.*
import me.weyye.hipermission.PermissionItem
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class StaffDataActivity : BaseActivity() {

    private var mPosition = 0
    private var mDid: String? = null
    private var mDialog: Dialog? = null
    private val mFileName = ArrayList<String>()
    private var mImageViews: List<ImageView>? = null
    private var mPhotoName: String? = null
    private var mOutFile: File? = null
    private var mBitmapList = ArrayList<Bitmap>()
    private var mApiService: ApiService? = null

    override fun getContentLayoutRes(): Int = R.layout.activity_staff_data

    override fun initToolbar() {
        val toolbarUtil = ToolbarUtil(this)
        toolbarUtil.setToolbar(staff_data_toolbar, "隐患数据")
    }

    override fun initView() {
        mDid = intent.getStringExtra("did")
        Log.d("tag", "did == $mDid")
        val ctime = intent.getStringExtra("ctime")
        val staffName = intent.getStringExtra("staff_name")
        val dnote = intent.getStringExtra("dnote")
        val address = intent.getStringExtra("addr")

        staff_data_tv_danger_address.text = "隐患位置：$address"
        staff_data_tv_danger_des.text = "隐患描述：$dnote"
        staff_data_tv_name.text = staffName
        staff_data_tv_time.text = ctime

        mImageViews = listOf<ImageView>(staff_data_iv_danger_first, staff_data_iv_danger_second, staff_data_iv_danger_third, staff_data_iv_danger_forth)
        mApiService = NetWorkUtil.getInstance().apiService
        // 解析需要维修的图片
        setPhoto(mDid)

        staff_data_iv_danger_first.setOnClickListener(mOnClick)
        staff_data_iv_danger_second.setOnClickListener(mOnClick)
        staff_data_iv_danger_third.setOnClickListener(mOnClick)
        staff_data_iv_danger_forth.setOnClickListener(mOnClick)
        staff_data_iv_first.setOnClickListener(mOnClick)
        staff_data_iv_second.setOnClickListener(mOnClick)
        staff_data_iv_third.setOnClickListener(mOnClick)
        staff_data_iv_forth.setOnClickListener(mOnClick)
        staff_data_btn_upload.setOnClickListener(mOnClick)
    }

    private val mOnClick = View.OnClickListener { view ->
        when (view.id) {
            R.id.staff_data_iv_danger_first -> {
                if (mFileName.size >= 1) {
                    val url = mFileName[0]
                    intentLargeImage("http://121.42.136.94/admin" + url)
                }
            }
            R.id.staff_data_iv_danger_second -> {
                if (mFileName.size >= 2) {
                    val url = mFileName[1]
                    intentLargeImage("http://121.42.136.94/admin" + url)
                }
            }
            R.id.staff_data_iv_danger_third -> {
                if (mFileName.size >= 3) {
                    val url = mFileName[2]
                    intentLargeImage("http://121.42.136.94/admin" + url)
                }
            }
            R.id.staff_data_iv_danger_forth -> {
                if (mFileName.size >= 4) {
                    val url = mFileName[3]
                    intentLargeImage("http://121.42.136.94/admin" + url)
                }
            }
            R.id.staff_data_iv_first -> {
                mPosition = 0
                createDialog()
            }
            R.id.staff_data_iv_second -> {
                mPosition = 1
                createDialog()
            }
            R.id.staff_data_iv_third -> {
                mPosition = 2
                createDialog()
            }
            R.id.staff_data_iv_forth -> {
                mPosition = 3
                createDialog()
            }
            R.id.staff_data_btn_upload -> {
                // 上传数据
                uploadData()
            }
        }
    }

    private fun uploadData() {
        val note = staff_data_tv_danger_des.text.toString()
        mApiService?.saveRepairCheck(mDid, note)
        mApiService?.equals(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                val json = String(response?.body()?.bytes()!!)
                val obj = JSONObject(json)
                val code = obj.getInt("code")
                if (code == 200) {
                    // 上传图片
                    uploadBitmap()
                }
            }

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                showToast(getString(R.string.network_error))
            }

        })
    }

    private fun uploadBitmap() {
        for (i in 0 until mBitmapList.size) {
            val uploadImg = mApiService?.uploadRepairImg(mDid, BitmapUtil.convertBitmapToString(mBitmapList[i]), mPhotoName)
            uploadImg?.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                    showToast(getString(R.string.network_error))
                }

                override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                    val json = String(response?.body()?.bytes()!!)
                    val jsonObject = JSONObject(json)
                    val code = jsonObject.getInt("code")
                    if (code == 200) {
                        Toast.makeText(this@StaffDataActivity, "图片上传成功", Toast.LENGTH_SHORT).show()
                    }
                }

            })
        }
    }

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
            val permissions = java.util.ArrayList<PermissionItem>()
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

    private fun takePhoto() {
        val state = Environment.getExternalStorageState()
        if (state == Environment.MEDIA_MOUNTED) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            val outDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            if (!outDir.exists()) {
                outDir.mkdirs()
            }
            mPhotoName = System.currentTimeMillis().toString() + ".jpg"
            mOutFile = File(outDir, mPhotoName)

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
                    staff_data_iv_first.setImageBitmap(bitmap)
                    mBitmapList.add(bitmap)
                }
                1 -> {
                    staff_data_iv_second.setImageBitmap(bitmap)
                    mBitmapList.add(bitmap)
                }
                2 -> {
                    staff_data_iv_third.setImageBitmap(bitmap)
                    mBitmapList.add(bitmap)
                }
                3 -> {
                    staff_data_iv_forth.setImageBitmap(bitmap)
                    mBitmapList.add(bitmap)
                }
            }
        }
    }

    private fun intentLargeImage(url: String) {
        val intent = Intent(mContext, LargeImageActivity::class.java)
        intent.putExtra("url", url)
        startActivity(intent)
    }

    /**
     * 根据 did 解析图片
     */
    private fun setPhoto(did: String?) {
        val repairPhoto = mApiService?.repairPhoto(did)
        repairPhoto?.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                val json = String(response?.body()?.bytes()!!)
                Log.d("tag", json)
                val jsonObject = JSONObject(json)
                val code = jsonObject.getInt("code")
                if (code == 200) {
                    val array = jsonObject.getJSONArray("weixiu_content")
                    for (i in 0 until array.length()) {
                        val data = array.get(i) as JSONObject
                        var fileName = data.getString("file_name")
                        fileName = fileName.substring(1)
                        mFileName.add(fileName)
                    }
                    if (mFileName.size > 4) {
                        for (i in 0 until mFileName.size - 4) {
                            mFileName.removeAt(i)
                        }
                    }
                    for (i in mFileName.indices) {
                        val imageView = mImageViews?.get(i)
                        val url = mFileName[i]
                        if (url != "") {
                            Picasso.with(this@StaffDataActivity).load("http://121.42.136.94/admin" + url).into(imageView)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                showToast(getString(R.string.network_error))
            }
        })
    }

}
