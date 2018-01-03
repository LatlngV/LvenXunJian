package cn.eyesw.lvenxunjian.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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

    public final static String SAVED_IMAGE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
    String photoPath;
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
    // 上传图片的标志位
    private int mBitmapIndex;
    private ProgressDialog mProgressDialog;

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
                if (mBitmapList.size() != 2) {
                    Toast.makeText(mContext, "当前照片数量不足 2 张", Toast.LENGTH_SHORT).show();
                    return;
                }
                mProgressDialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setMessage("照片正在上传");
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
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
        photoPath = SAVED_IMAGE_PATH + "/" + System.currentTimeMillis() + ".png";

        File imageDir = new File(photoPath);
        if (!imageDir.exists()) {
            try {
                // 根据一个 文件地址生成一个新的文件用来存照片
                imageDir.createNewFile();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = new File(photoPath);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        startActivityForResult(intent, 1);
    }

    /**
     * 上传照片
     */
    private void uploadPicture() {
        // 上传照片
        if (mBitmap != null) {
            mBtnCommit.setEnabled(false);
            new Handler().postDelayed(this::sendPortrait, 1500);
        }
    }

    /**
     * 上传照片
     */
    private void sendPortrait() {
        Map<String, String> map;
        for (int i = 0; i < mBitmapList.size(); i++) {
            map = new HashMap<>();
            Bitmap bitmap = mBitmapList.get(i);
            String picture = BitmapUtil.convertBitmapToString(bitmap);
            map.put("file", picture);
            map.put("task_id", "1");
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
                    PictureDao pictureDao = new PictureDao();
                    PictureBean pictureBean = new PictureBean(picture, 0 + "", 0 + "", mTime, "");
                    pictureDao.add(pictureBean);
                }

                @Override
                public void onResponse(String json) {
                    // 释放内存
                    bitmap.recycle();
                    mBitmapIndex += 1;

                    if (mBitmapIndex == mBitmapList.size()) {
                        mProgressDialog.dismiss();
                        Toast.makeText(mContext, "上传成功", Toast.LENGTH_SHORT).show();
                        mImageViewLeft.setImageResource(R.drawable.timg);
                        mImageViewRight.setImageResource(R.drawable.timg);
                        mBitmap.recycle();
                        mBitmap = null;
                        // 清空集合
                        mBitmapList.clear();
                    }
                }
            });
        }
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
        if (requestCode == 1) {
            File photoFile = new File(photoPath);
            if (photoFile.exists()) {
                //通过图片地址将图片加载到bitmap里面
                mBitmap = compressBitmap(photoFile.getAbsolutePath(), 720, 960);
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
            }
        }
    }

    /**
     * 压缩图片
     */
    private Bitmap compressBitmap(String path, double width, double height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false; // 设置了此属性一定要记得将值设置为 false
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        // 防止 OOM 发生
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth;
        float scaleHeight;
        if (bitmapWidth <= bitmapHeight) {
            scaleWidth = (float) (width / bitmapWidth);
            scaleHeight = (float) (height / bitmapHeight);
        } else {
            scaleWidth = (float) (height / bitmapWidth);
            scaleHeight = (float) (width / bitmapHeight);
        }
        // 按照固定大小对图片进行缩放
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
        // 用完了记得回收
        bitmap.recycle();
        return newBitmap;
    }

    /**
     * 设置 Bitmap 加载方式
     */
    private BitmapFactory.Options bitmapOptions() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        // 为位图设置缓存
        options.inTempStorage = new byte[1024 * 1024];
        // 设置位图颜色显示优化方式
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        // 设置图片可以回收
        options.inPurgeable = true;
        // 设置图片缩放比例
        options.inSampleSize = 4;
        // 设置解码位图的尺寸信息
        options.inInputShareable = true;
        return options;
    }

}
