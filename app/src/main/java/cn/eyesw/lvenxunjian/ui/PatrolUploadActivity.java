package cn.eyesw.lvenxunjian.ui;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
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

    private Bitmap mBitmap;
    private OkHttpManager mOkHttpManager;
    private SpUtil mSpUtil;
    private String mTime;
    /* 经纬度坐标 */
    private String mLongitude;
    private String mLatitude;
    private String mAddress;
    // 点击 ImageView 的标志位
    private int mIndex = 0;
    // 存储图片的集合
    private List<Bitmap> mBitmapList;
    private int mUploadIndex = 0;

    @BindView(R.id.upload_toolbar)
    protected Toolbar mToolbar;
    @BindView(R.id.upload_tv_adress)
    protected TextView mTvAddress;
    @BindView(R.id.upload_tv_date)
    protected TextView mTvDate;
    @BindView(R.id.upload_image_view_left) // 必经点第一张照片
    protected ImageView mImageViewLeft;
    @BindView(R.id.upload_image_view_right) // 必经点第二张照片
    protected ImageView mImageViewRight;
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
        mBitmapList = new ArrayList<>();
        mOkHttpManager = OkHttpManager.getInstance();
        mSpUtil = SpUtil.getInstance(mContext);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 获取经纬度坐标
        getLatlng();
    }

    @OnClick({R.id.upload_tv_refresh, R.id.upload_image_view_left, R.id.upload_image_view_right, R.id.upload_btn_commit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.upload_tv_refresh:
                // 获取经纬度坐标
                getLatlng();
                break;
            case R.id.upload_image_view_left:
                mIndex = 0;
                // 申请权限
                applyPermission();
                break;
            case R.id.upload_image_view_right:
                mIndex = 1;
                // 申请权限
                applyPermission();
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
     * 权限申请
     */
    private void applyPermission() {
        List<PermissionItem> permissions = new ArrayList<>();
        permissions.add(new PermissionItem(Manifest.permission.CAMERA, "相机授权", R.drawable.permission_ic_camera));
        permissions.add(new PermissionItem(Manifest.permission.WRITE_EXTERNAL_STORAGE, "写入授权", R.drawable.permission_ic_storage));
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
    }

    /**
     * 打开相机拍照
     */
    private void openCamera() {
        Intent intent = new Intent(this, WaterMarkerCameraActivity.class);
        intent.putExtra("address", mAddress);
        startActivityForResult(intent, 1);
    }

    /**
     * 上传照片
     */
    private void uploadPicture() {
        // 上传照片
        if (mBitmap != null) {
            new Handler().postDelayed(this::sendPortrait, 1500);
        }
    }

    /**
     * 上传照片
     */
    private void sendPortrait() {
        new Thread() {
            @Override
            public void run() {
                Map<String, String> map;
                for (int i = 0; i < mBitmapList.size(); i++) {
                    map = new HashMap<>();
                    String picture = BitmapUtil.convertBitmapToString(mBitmapList.get(i));
                    map.put("file", picture);
                    map.put("task_id", mSpUtil.getString("task_id"));
                    map.put("staff_id", mSpUtil.getString("id"));
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    mTime = format.format(new Date());
                    map.put("photo_time", mTime);
                    map.put("longitude", mLongitude);
                    map.put("latitude", mLatitude);
                    map.put("file_name", "1");
                    mOkHttpManager.postAsyncForm(NetworkApi.UPLOAD_TASK_IMG, map, new OkHttpManager.DataCallback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            PictureDao pictureDao = new PictureDao(mContext);
                            PictureBean pictureBean = new PictureBean(picture, 0 + "", 0 + "", mTime, "");
                            pictureDao.add(pictureBean);
                        }

                        @Override
                        public void onResponse(String json) {
                            mUploadIndex += 1;
                            if (mUploadIndex == 2) {
                                for (int j = 0, length = mBitmapList.size(); j < length; j++) {
                                    Bitmap bitmap = mBitmapList.get(j);
                                    if (!bitmap.isRecycled()) {
                                        bitmap.recycle();
                                    }
                                }
                                runOnUiThread(() -> Toast.makeText(mContext, "上传成功", Toast.LENGTH_SHORT).show());
                            }
                        }
                    });
                }
            }
        }.start();

        mBtnCommit.setEnabled(false);
        mImageViewLeft.setImageResource(R.drawable.timg);
        mImageViewRight.setImageResource(R.drawable.timg);
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
                        mAddress = data.getString("address");
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        mTime = format.format(new Date());

                        mTvAddress.setText(mAddress);
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
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 2 && requestCode == 1) {
            try {
                String filePath = data.getStringExtra("filePath");
                FileInputStream fis = new FileInputStream(filePath);
                mBitmap = BitmapFactory.decodeStream(fis);
                switch (mIndex) {
                    case 0:
                        mImageViewLeft.setImageBitmap(mBitmap);
                        mBitmapList.add(mBitmap);
                        break;
                    case 1:
                        mImageViewRight.setImageBitmap(mBitmap);
                        mBitmapList.add(mBitmap);
                        break;
                }
                if (mBitmapList.size() == 2) {
                    mBtnCommit.setEnabled(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
