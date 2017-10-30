package cn.eyesw.lvenxunjian.ui;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import cn.eyesw.lvenxunjian.R;
import cn.eyesw.lvenxunjian.base.BaseActivity;
import cn.eyesw.lvenxunjian.constant.Constant;
import cn.eyesw.lvenxunjian.constant.NetworkApi;
import cn.eyesw.lvenxunjian.utils.BitmapUtil;
import cn.eyesw.lvenxunjian.utils.DensityUtil;
import cn.eyesw.lvenxunjian.utils.DialogUtil;
import cn.eyesw.lvenxunjian.utils.OkHttpManager;
import cn.eyesw.lvenxunjian.utils.PermissionsUtil;
import cn.eyesw.lvenxunjian.utils.SpUtil;
import cn.eyesw.lvenxunjian.utils.ToolbarUtil;
import okhttp3.Call;

/**
 * 紧急报警(抢维修)
 */
public class EmergencyAlarmActivity extends BaseActivity {

    // 手机拍照的照片
    private File mOutFile;
    // 照片的名字
    private String mFileName;
    // 保存隐患等级的集合
    private List<Map<String, String>> mDatas;
    // 定义一个变量，用来判断点击的是哪个 ImageView
    private int mPosition;
    // 保存上传图片的集合
    private List<Bitmap> mList;
    // 保存隐患类型的集合
    private List<Map<String, String>> mDangerType;

    private OkHttpManager mOkHttpManager;
    private String mPositionDes;
    private String mEvetntDes;
    private String mDataId;
    private Intent mIntent;
    private Dialog mDialog;
    private String mLevelId;
    private String mTypeId;

    @BindView(R.id.alarm_toolbar)
    protected Toolbar mToolbar;
    @BindView(R.id.alarm_tv_longitude)
    protected TextView mTvLongitude;
    @BindView(R.id.alarm_tv_latitude)
    protected TextView mTvLatitude;
    @BindView(R.id.alarm_et_position_description)
    protected EditText mEtPositionDes;
    @BindView(R.id.alarm_tv_enent_type)
    protected TextView mTvEnentType;
    @BindView(R.id.alarm_tv_enent_level)
    protected TextView mTvEnentLevel;
    @BindView(R.id.alarm_et_event_description)
    protected EditText mEtEventDescription;
    @BindView(R.id.alarm_iv_first_take_photo)
    protected ImageView mIvFirstTakePhoto;
    @BindView(R.id.alarm_iv_secord_take_photo)
    protected ImageView mIvSecordTakePhoto;
    @BindView(R.id.alarm_iv_third_take_photo)
    protected ImageView mIvThirdTakePhoto;
    @BindView(R.id.alarm_iv_forth_take_photo)
    protected ImageView mIvForthTakePhoto;

    @Override
    protected int getContentLayoutRes() {
        return R.layout.activity_emergency_alarm;
    }

    @Override
    protected void initToolbar() {
        ToolbarUtil toolbarUtil = new ToolbarUtil(this);
        toolbarUtil.setToolbar(mToolbar, "紧急报警");
    }

    @Override
    protected void initView() {
        mOkHttpManager = OkHttpManager.getInstance();
        mDatas = new ArrayList<>();
        mDangerType = new ArrayList<>();
        mList = new ArrayList<>();

        Map<String, String> map = new HashMap<>();
        map.put("staff_id", SpUtil.getInstance(mContext).getString("id"));
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
                        String longitude = data.getString("longitude");
                        String latitude = data.getString("latitude");
                        String address = data.getString("address");

                        mTvLongitude.setText(longitude);
                        mTvLatitude.setText(latitude);
                        mEtPositionDes.setText(address);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @OnTextChanged({R.id.alarm_et_position_description, R.id.alarm_et_event_description})
    public void onTextChanged() {
        mPositionDes = mEtPositionDes.getText().toString();
        mEvetntDes = mEtEventDescription.getText().toString();
    }

    @OnClick({R.id.alarm_tv_enent_type, R.id.alarm_tv_enent_level, R.id.alarm_et_event_description,
            R.id.alarm_iv_first_take_photo, R.id.alarm_iv_secord_take_photo,
            R.id.alarm_iv_third_take_photo, R.id.alarm_btn_save, R.id.alarm_civ_emergency_alarm,
            R.id.alarm_iv_forth_take_photo})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.alarm_tv_enent_type:
                // 获取隐患类型
                getDangerType();
                break;
            case R.id.alarm_et_position_description:
                mEtPositionDes.setCursorVisible(true);
                break;
            case R.id.alarm_tv_enent_level:
                // 获取隐患级别
                getDangerLevel();
                break;
            case R.id.alarm_iv_first_take_photo:
                mPosition = 100;
                // 弹出一个对话框，选择拍照还是从相册上传图片
                createDialog();
                break;
            case R.id.alarm_iv_secord_take_photo:
                mPosition = 101;
                createDialog();
                break;
            case R.id.alarm_iv_third_take_photo:
                mPosition = 102;
                createDialog();
                break;
            case R.id.alarm_iv_forth_take_photo:
                mPosition = 103;
                createDialog();
                break;
            case R.id.alarm_btn_save:
                // 上传数据
                uploadData();
                break;
            case R.id.alarm_civ_emergency_alarm:
                // 显示紧急报警的对话框
                showEmergencyalarmdialog();
                break;
            case R.id.alarm_et_event_description:
                mEtEventDescription.setCursorVisible(true);
                break;
            default:
                throw new RuntimeException(getString(R.string.unkown_error));
        }
    }

    /**
     * 获取隐患类型
     */
    private void getDangerType() {
        OkHttpManager.getInstance().getAsync(NetworkApi.DANGER_TYPE, new OkHttpManager.DataCallback() {
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
                        JSONArray array = object.getJSONArray("data");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject data = (JSONObject) array.get(i);
                            String name = data.getString("type_name");
                            String id = data.getString("id");
                            Map<String, String> map = new HashMap<>();
                            map.put("name", name);
                            map.put("id", id);
                            mDangerType.add(map);
                        }
                        showDangerTypeDialog();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 显示隐患类型的 Dialog
     */
    private void showDangerTypeDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setTitle("隐患类型");
        View view = View.inflate(mContext, R.layout.dialog_danger_type, null);
        dialog.setContentView(view);
        dialog.show();

        LinearLayout llContainer = ButterKnife.findById(dialog, R.id.dialog_ll_container);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                DensityUtil.dip2px(mContext, 150),
                DensityUtil.dip2px(mContext, 50));
        for (int i = 0; i < mDangerType.size(); i++) {
            TextView textView = new TextView(this);
            textView.setText(mDangerType.get(i).get("name"));
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setTextColor(Color.RED);

            params.leftMargin = DensityUtil.dip2px(mContext, 15);
            textView.setLayoutParams(params);

            Map<String, String> map = mDangerType.get(i);
            textView.setOnClickListener(v -> {
                mTypeId = map.get("id");
                mTvEnentType.setText(map.get("name"));
                dialog.dismiss();
            });
            llContainer.addView(textView);
        }
        mDangerType.clear();
    }

    /**
     * 上传数据
     */
    private void uploadData() {
        sendInfo();
        Handler handler = new Handler();
        handler.postDelayed(this::updataBitmap, 2000);
    }

    /**
     * 上传信息
     */
    private void sendInfo() {
        Map<String, String> map = new HashMap<>();
        map.put("staff_id", SpUtil.getInstance(mContext).getString("id"));
        map.put("task_id", "");
        map.put("latitude", mTvLatitude.getText().toString());
        map.put("longitude", mTvLongitude.getText().toString());
        map.put("address", mPositionDes);
        map.put("note", mEvetntDes);
        map.put("weather", null);
        map.put("danger_type", mTypeId);
        map.put("danger_level", mLevelId);

        mOkHttpManager.postAsyncForm(NetworkApi.SAVE_WEIXIU, map, new OkHttpManager.DataCallback() {
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
                        mDataId = object.getJSONObject("data").getString("id");
                        Toast.makeText(mContext, "数据上传成功", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 上传图片
     */
    private void updataBitmap() {
        // 如果保存 Bitmap 的集合长度大于 4，就移除集合里前面几张照片，直至集合里剩下 4 张照片
        if (mList != null && mList.size() > 0) {
            if (mList.size() > 4) {
                for (int i = 0; i < mList.size() - 4; i++) {
                    mList.remove(i);
                }
            }
            for (int i = 0; i < mList.size(); i++) {
                Map<String, String> map = new HashMap<>();
                map.put("weixiu_data_id", mDataId);
                map.put("file_type", 1 + "");
                map.put("file", BitmapUtil.convertBitmapToString(mList.get(i)));
                map.put("file_name", mFileName);
                mOkHttpManager.postAsyncForm(NetworkApi.UPLOAD_WEIXIU_IMG, map, new OkHttpManager.DataCallback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Toast.makeText(mContext, "上传失败", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String json) {
                        try {
                            JSONObject object = new JSONObject(json);
                            int code = object.getInt("code");
                            if (code == 200) {
                                Toast.makeText(mContext, "图片上传成功", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            // 清空集合
            mList.clear();
        }
    }

    /**
     * 显示危险等级的 Dialog
     */
    private void showDangerLevelDialog() {
        Dialog dialog = new Dialog(this);
        View view = View.inflate(mContext, R.layout.dialog_danger_level, null);
        dialog.setContentView(view);
        dialog.setTitle("隐患级别");
        dialog.show();

        LinearLayout llContainer = ButterKnife.findById(dialog, R.id.danger_level_ll_container);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                DensityUtil.dip2px(mContext, 150),
                DensityUtil.dip2px(mContext, 50));
        for (int i = 0; i < mDatas.size(); i++) {
            TextView textView = new TextView(this);
            textView.setText(mDatas.get(i).get("levelName"));
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setTextColor(Color.RED);

            params.leftMargin = DensityUtil.dip2px(mContext, 15);
            textView.setLayoutParams(params);

            Map<String, String> map = mDatas.get(i);
            textView.setOnClickListener(v -> {
                mLevelId = map.get("id");
                mTvEnentLevel.setText(map.get("levelName"));
                dialog.dismiss();
            });

            llContainer.addView(textView);
        }
        mDatas.clear();
    }

    /**
     * 获取隐患级别
     */
    private void getDangerLevel() {
        mOkHttpManager.getAsync(NetworkApi.DANGER_LEVEL, new OkHttpManager.DataCallback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(mContext, "网络连接失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String json) {
                // 解析数据
                analysisData(json);
            }
        });
    }

    /**
     * 解析数据
     */
    private void analysisData(String json) {
        try {
            JSONObject object = new JSONObject(json);
            int code = object.getInt("code");
            if (code == 200) {
                JSONArray jsonArray = object.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject data = (JSONObject) jsonArray.opt(i);
                    String levelName = data.getString("level_name");
                    String id = data.getString("id");
                    Map<String, String> map = new HashMap<>();
                    map.put("levelName", levelName);
                    map.put("id", id);
                    mDatas.add(map);
                }
                showDangerLevelDialog();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 选择获取照片的方式
     */
    private void createDialog() {
        mDialog = new Dialog(this, R.style.UserDialog);
        View view = View.inflate(mContext, R.layout.dialog_select_photo, null);
        mDialog.setContentView(view);
        mDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        mDialog.show();

        TextView tvCamera = ButterKnife.findById(mDialog, R.id.user_dialog_tv_camera);
        TextView tvAlbum = ButterKnife.findById(mDialog, R.id.user_dialog_tv_album);
        TextView tvCancel = ButterKnife.findById(mDialog, R.id.user_dialog_tv_cancel);

        // 相机
        tvCamera.setOnClickListener(v -> {
            // 检查权限
            PermissionsUtil.requestPermission(EmergencyAlarmActivity.this, PermissionsUtil.CODE_CAMERA, mPermissionGrant);
        });
        // 相册
        tvAlbum.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, null);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(intent, Constant.ALBUM_REQUEST_CODE);
            mDialog.dismiss();
        });
        // 取消
        tvCancel.setOnClickListener(v -> mDialog.dismiss());
    }

    /**
     * 打开相机拍照
     */
    private void takePhoto() {
        try {
            String state = Environment.getExternalStorageState();
            if (state.equals(Environment.MEDIA_MOUNTED)) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                File outDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                if (!outDir.exists()) {
                    outDir.mkdirs();
                }
                mFileName = System.currentTimeMillis() + ".jpg";
                mOutFile = new File(outDir, mFileName);

                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mOutFile));
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                startActivityForResult(intent, Constant.CAMERA_REQUEST_CODE);

            } else {
                Toast.makeText(mContext, "请安装 sdcrad", Toast.LENGTH_SHORT).show();
            }
            mDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constant.CAMERA_REQUEST_CODE:
                startPhotoZoom(Uri.fromFile(mOutFile));
                break;
            case Constant.ALBUM_REQUEST_CODE:
                if (data == null || data.getData() == null) {
                    return;
                }
                startPhotoZoom(data.getData());
                break;
            case Constant.CROP_REQUEST_CODE:
                if (data != null) {
                    setPicToView(data, mPosition);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 裁剪照片
     */
    private void startPhotoZoom(Uri data) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(data, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("return-data", true);
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, Constant.CROP_REQUEST_CODE);
    }

    /**
     * 将裁剪好的照片设置到 ImageView 上
     */
    private void setPicToView(Intent data, int position) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap bitmap = extras.getParcelable("data");
            switch (position) {
                case 100:
                    mIvFirstTakePhoto.setImageBitmap(bitmap);
                    mList.add(bitmap);
                    break;
                case 101:
                    mIvSecordTakePhoto.setImageBitmap(bitmap);
                    mList.add(bitmap);
                    break;
                case 102:
                    mIvThirdTakePhoto.setImageBitmap(bitmap);
                    mList.add(bitmap);
                    break;
                case 103:
                    mIvForthTakePhoto.setImageBitmap(bitmap);
                    mList.add(bitmap);
                    break;
            }
        }
    }

    /**
     * 紧急报警的对话框
     */
    private void showEmergencyalarmdialog() {
        String phone = SpUtil.getInstance(mContext).getString("phone");
        if (!phone.equals("")) {
            DialogUtil.getAlertDialog(this, "紧急报警电话", phone, (dialog, which) -> PermissionsUtil.requestPermission(EmergencyAlarmActivity.this, PermissionsUtil.CODE_CALL_PHONE, mPermissionGrant));
        } else {
            Toast.makeText(mContext, "请设置紧急联系人电话", Toast.LENGTH_SHORT).show();
        }
    }

    private PermissionsUtil.PermissionGrant mPermissionGrant = requestCode -> {
        if (requestCode == PermissionsUtil.CODE_CALL_PHONE) {
            callToUrgentContact();
        }
        if (requestCode == PermissionsUtil.CODE_CAMERA) {
            takePhoto();
        }
    };

    /**
     * 跳转到打电话界面
     */
    private void callToUrgentContact() {
        String phone = SpUtil.getInstance(mContext).getString("phone");
        mIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
        startActivity(mIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionsUtil.requestPermissionsResult(this, requestCode, permissions, grantResults, mPermissionGrant);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
