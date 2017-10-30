package cn.eyesw.lvenxunjian.ui;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.eyesw.lvenxunjian.R;
import cn.eyesw.lvenxunjian.base.BaseActivity;
import cn.eyesw.lvenxunjian.constant.Constant;
import cn.eyesw.lvenxunjian.constant.NetworkApi;
import cn.eyesw.lvenxunjian.utils.BitmapUtil;
import cn.eyesw.lvenxunjian.utils.DialogUtil;
import cn.eyesw.lvenxunjian.utils.OkHttpManager;
import cn.eyesw.lvenxunjian.utils.PermissionsUtil;
import cn.eyesw.lvenxunjian.utils.SpUtil;
import cn.eyesw.lvenxunjian.utils.ToolbarUtil;
import okhttp3.Call;

/**
 * 用户信息
 */
public class UserActivity extends BaseActivity {

    private File mOutFile;
    // 存储图片文件的名字
    private String mFileName;
    private File mOutDir;
    private Dialog mDialog;
    private Uri mUri;

    @BindView(R.id.uesr_toolbar)
    protected Toolbar mToolbar;
    @BindView(R.id.user_circl_image_view)
    protected ImageView mImageView;
    @BindView(R.id.uesr_tv_staff_name)
    protected TextView mTvStaffName;
    @BindView(R.id.uesr_tv_staff_role)
    protected TextView mTvStaffRole;
    @BindView(R.id.uesr_tv_staff_address)
    protected TextView mTvStaffAddress;
    @BindView(R.id.uesr_tv_staff_phone)
    protected TextView mTvStaffPhone;
    @BindView(R.id.uesr_tv_urgent_phone)
    protected TextView mTvUrgentPhone;

    @Override
    protected int getContentLayoutRes() {
        return R.layout.activity_user;
    }

    @Override
    protected void initToolbar() {
        ToolbarUtil toolbarUtil = new ToolbarUtil(this);
        toolbarUtil.setToolbar(mToolbar, "用户信息");
    }

    @Override
    protected void initView() {
        mTvUrgentPhone.setText(SpUtil.getInstance(mContext).getString("phone"));
        Map<String, String> map = new HashMap<>();
        map.put("staff_id", SpUtil.getInstance(mContext).getString("id"));
        OkHttpManager.getInstance().postAsyncForm(NetworkApi.STAFF_DETAIL, map, new OkHttpManager.DataCallback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(mContext, "网络连接失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String json) {
                try {
                    JSONObject obj = new JSONObject(json);
                    int code = obj.getInt("code");
                    if (code == 200) {
                        JSONObject data = obj.getJSONObject("data").getJSONObject("result");
                        String staffName = data.getString("staff_name");
                        String portrait = data.getString("portrait");
                        String roleName = data.getString("role_name");
                        String address = data.getString("address");
                        String phone = data.getString("staff_phone");

                        if (!portrait.equals("")) {
                            Picasso.with(mContext).load(portrait).into(mImageView);
                        }
                        mTvStaffName.setText(staffName);
                        mTvStaffRole.setText(roleName);
                        mTvStaffAddress.setText(address);
                        mTvStaffPhone.setText(phone);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @OnClick({R.id.user_circl_image_view, R.id.user_rl_container})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.user_circl_image_view:
                // 弹出一个对话框，选择拍照还是从相册上传图片
                createDialog();
                break;
            case R.id.user_rl_container:
                // 设置紧急联系人电话
                showSetUrgentPhone();
                break;
            default:
                throw new RuntimeException(getString(R.string.unkown_error));
        }
    }

    /**
     * 设置紧急联系人电话
     */
    private void showSetUrgentPhone() {
        Dialog dialog = DialogUtil.getDialog(this, R.layout.dialog_urgent_phone);

        EditText etInputPhone = ButterKnife.findById(dialog, R.id.dialog_et_input_phone);
        Button btnConfirm = ButterKnife.findById(dialog, R.id.dialog_btn_confirm);
        Button btnCancel = ButterKnife.findById(dialog, R.id.dialog_btn_cancel);

        // 取消按钮
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        // 确定按钮
        btnConfirm.setOnClickListener(v -> {
            String phone = etInputPhone.getText().toString().trim();
            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(mContext, "请输入电话号码", Toast.LENGTH_SHORT).show();
                return;
            }
            if (phone.length() != 11) {
                Toast.makeText(mContext, "请输入正确的电话号码", Toast.LENGTH_SHORT).show();
                return;
            }
            mTvUrgentPhone.setText(phone);
            SpUtil.getInstance(mContext).putString("phone", phone);
            dialog.dismiss();
        });
    }

    /**
     * 弹出一个对话框，选择拍照还是从相册上传图片
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
            // 获取相应的权限
            PermissionsUtil.requestPermission(UserActivity.this, PermissionsUtil.CODE_CAMERA, mPermissionGrant);
        });
        // 相册
        tvAlbum.setOnClickListener(v -> {
            // 获取相应的权限
            selectPicture();
        });
        // 取消
        tvCancel.setOnClickListener(v -> mDialog.dismiss());
    }

    private PermissionsUtil.PermissionGrant mPermissionGrant = requestCode -> {
        if (requestCode == PermissionsUtil.CODE_CAMERA) {
            takePhoto();
        }
    };

    /**
     * 打开相机
     */
    private void takePhoto() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            mOutDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            if (!mOutDir.exists()) {
                mOutDir.mkdirs();
            }
            mFileName = System.currentTimeMillis() + ".jpg";
            mOutFile = new File(mOutDir, mFileName);

            mUri = Uri.fromFile(mOutFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            startActivityForResult(intent, Constant.CAMERA_REQUEST_CODE);

        } else {
            Toast.makeText(mContext, "请安装 sdcrad", Toast.LENGTH_SHORT).show();
        }
        mDialog.dismiss();
    }

    /**
     * 打开相册
     */
    private void selectPicture() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, Constant.ALBUM_REQUEST_CODE);
        mDialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constant.CAMERA_REQUEST_CODE:
                startPhotoZoom(mUri);
                break;
            case Constant.ALBUM_REQUEST_CODE:
                if (data == null || data.getData() == null) {
                    return;
                }
                startPhotoZoom(data.getData());
                break;
            case Constant.CROP_REQUEST_CODE:
                if (data != null) {
                    setPicToView(data);
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
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, Constant.CROP_REQUEST_CODE);
    }

    /**
     * 将裁剪好的照片设置到 ImageView 上
     */
    private void setPicToView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap bitmap = extras.getParcelable("data");
            if (bitmap != null) {
                try {
                    if (mOutFile == null) {
                        mOutDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                        if (!mOutDir.exists()) {
                            mOutDir.mkdirs();
                        }
                        mFileName = System.currentTimeMillis() + ".jpg";
                        mOutFile = new File(mOutDir, mFileName);
                    }
                    FileOutputStream out = new FileOutputStream(mOutFile);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);

                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mImageView.setImageBitmap(bitmap);

            // 上传头像
            sendPortrait(bitmap);
        }
    }

    /**
     * 上传头像
     */
    private void sendPortrait(Bitmap bitmap) {
        Map<String, String> map = new HashMap<>();
        map.put("file_name", mFileName);
        map.put("file", BitmapUtil.convertBitmapToString(bitmap));
        map.put("staff_id", SpUtil.getInstance(mContext).getString("id"));
        OkHttpManager.getInstance().postAsyncForm(NetworkApi.STAFF_PORTRAIT, map, new OkHttpManager.DataCallback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(mContext, "上传失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String json) {
                try {
                    JSONObject obj = new JSONObject(json);
                    int code = obj.getInt("code");
                    if (code == 200) {
                        Toast.makeText(mContext, "头像上传成功", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionsUtil.requestPermissionsResult(this, requestCode, permissions, grantResults, mPermissionGrant);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
