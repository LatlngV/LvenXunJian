package cn.eyesw.lvenxunjian.ui;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import cn.eyesw.lvenxunjian.R;
import cn.eyesw.lvenxunjian.base.BaseActivity;
import cn.eyesw.lvenxunjian.bean.PictureBean;
import cn.eyesw.lvenxunjian.constant.Constant;
import cn.eyesw.lvenxunjian.constant.NetworkApi;
import cn.eyesw.lvenxunjian.utils.BitmapUtil;
import cn.eyesw.lvenxunjian.utils.OkHttpManager;
import cn.eyesw.lvenxunjian.utils.PictureDao;
import cn.eyesw.lvenxunjian.utils.SpUtil;
import cn.eyesw.lvenxunjian.utils.ToolbarUtil;
import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;
import okhttp3.Call;

/**
 * 巡检上报
 */
public class PatrolUploadActivity extends BaseActivity {

    private File mOutFile;
    private Uri mUri;
    private Bitmap mBitmap;
    private OkHttpManager mOkHttpManager;
    private SpUtil mSpUtil;
    private String mFileName;
    private String mTime;
    /* 经纬度坐标 */
    private String mLongitude;
    private String mLatitude;

    @BindView(R.id.upload_toolbar)
    protected Toolbar mToolbar;
    @BindView(R.id.upload_tv_adress)
    protected TextView mTvAddress;
    @BindView(R.id.upload_tv_date)
    protected TextView mTvDate;
    @BindView(R.id.upload_image_view)
    protected ImageView mImageView;
    @BindView(R.id.upload_btn_commit)
    protected Button mBtnCommit;

    @Override
    protected int getContentLayoutRes() {
        return R.layout.activity_notice;
    }

    @Override
    protected void initToolbar() {
        ToolbarUtil toolbarUtil = new ToolbarUtil(this);
        toolbarUtil.setToolbar(mToolbar, "巡检上报");
    }

    @Override
    protected void initView() {
        mOkHttpManager = OkHttpManager.getInstance();
        mSpUtil = SpUtil.getInstance(mContext);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 获取经纬度坐标
        getLatlng();
    }

    @OnClick({R.id.upload_tv_refresh, R.id.upload_image_view, R.id.upload_btn_commit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.upload_tv_refresh:
                // 获取经纬度坐标
                getLatlng();
                break;
            case R.id.upload_image_view:
                List<PermissionItem> permissions = new ArrayList<>();
                permissions.add(new PermissionItem(Manifest.permission.CAMERA, "相机授权", R.drawable.permission_ic_camera));
                HiPermission.create(this)
                        .title("授权")
                        .permissions(permissions)
                        .animStyle(R.style.PermissionAnimModal)
                        .filterColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme()))
                        .msg("开启权限")
                        .checkMutiPermission(new PermissionCallback() {
                            @Override
                            public void onClose() {
                                showToast("相机授权关闭");
                            }

                            @Override
                            public void onFinish() {
                                // 打开相机拍照
                                openCamera();
                            }

                            @Override
                            public void onDeny(String permission, int position) {
                            }

                            @Override
                            public void onGuarantee(String permission, int position) {
                                // 打开相机拍照
                                openCamera();
                            }
                        });
                break;
            case R.id.upload_btn_commit:
                // 上传照片
                uploadPicture();
                break;
            default:
                throw new RuntimeException(getString(R.string.unkown_error));
        }
    }

    /**
     * 打开相机拍照
     */
    private void openCamera() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            File outDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            if (!outDir.exists()) {
                outDir.mkdirs();
            }
            mFileName = System.currentTimeMillis() + ".jpg";
            mOutFile = new File(outDir, mFileName);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // 通过 FileProvider 创建一个 content 类型的 Uri
                mUri = FileProvider.getUriForFile(mContext, "cn.eyesw.lvenxunjian.fileprovider", mOutFile);
                // 添加这一句表示对目标应用临时授权该 Uri 所代表的文件
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                // 设置 Action 为拍照
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            } else {
                mUri = Uri.fromFile(mOutFile);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            startActivityForResult(intent, Constant.CAMERA_REQUEST_CODE);

        } else {
            Toast.makeText(mContext, "请安装内存卡", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 上传照片
     */
    private void uploadPicture() {
        // 上传照片
        if (mBitmap != null) {
            new Handler().postDelayed(() -> sendPortrait(mBitmap), 1500);
        }
    }

    /**
     * 上传照片
     *
     * @param bitmap 要上传的照片
     */
    private void sendPortrait(Bitmap bitmap) {
        Map<String, String> map = new HashMap<>();
        String picture = BitmapUtil.convertBitmapToString(bitmap);
        map.put("file", picture);
        map.put("task_id", mSpUtil.getString("task_id"));
        map.put("staff_id", mSpUtil.getString("id"));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mTime = format.format(new Date());
        map.put("photo_time", mTime);
        map.put("longitude", mLongitude);
        map.put("latitude", mLatitude);
        map.put("file_name", mFileName);
        mOkHttpManager.postAsyncForm(NetworkApi.UPLOAD_TASK_IMG, map, new OkHttpManager.DataCallback() {
            @Override
            public void onFailure(Call call, IOException e) {
                PictureDao pictureDao = new PictureDao(mContext);
                PictureBean pictureBean = new PictureBean(picture, 0 + "", 0 + "", mTime, mFileName);
                pictureDao.add(pictureBean);
            }

            @Override
            public void onResponse(String json) {
                Toast.makeText(mContext, "上传成功", Toast.LENGTH_SHORT).show();
            }
        });
        mBtnCommit.setEnabled(false);
        mImageView.setImageResource(R.drawable.timg);
    }

    /**
     * 获取经纬度坐标
     */
    private void getLatlng() {
        Map<String, String> map = new HashMap<>();
        map.put("staff_id", mSpUtil.getString("id"));
        mOkHttpManager.postAsyncForm(NetworkApi.GPS, map, new OkHttpManager.DataCallback() {
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
                        JSONObject data = object.getJSONObject("data");
                        mLongitude = data.getString("longitude");
                        mLatitude = data.getString("latitude");
                        String address = data.getString("address");
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        mTime = format.format(new Date());

                        mTvAddress.setText(address);
                        mTvDate.setText(mTime);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constant.CAMERA_REQUEST_CODE:
                startPhotoZoom(mUri);
                break;
            case Constant.CROP_REQUEST_CODE:
                if (data != null) {
                    setPicToView(data);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startPhotoZoom(Uri data) {
        if (data == null) {
            Log.e("TAG", "The uri is not exist.");
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(data, "image/*");
        intent.putExtra("crop", "true");

        if (Build.MODEL.contains("HUAWEI")) {
            intent.putExtra("aspectX", 9998);
            intent.putExtra("aspectY", 9999);
        } else {
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
        }

        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("return-data", true);
        intent.putExtra("noFaceDetection", true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, Constant.CROP_REQUEST_CODE);
    }

    private void setPicToView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            mBitmap = extras.getParcelable("data");
            if (mBitmap != null) {
                try {
                    FileOutputStream out = new FileOutputStream(mOutFile);
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);

                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mImageView.setImageBitmap(mBitmap);
            mBtnCommit.setEnabled(true);
        }
    }

}
