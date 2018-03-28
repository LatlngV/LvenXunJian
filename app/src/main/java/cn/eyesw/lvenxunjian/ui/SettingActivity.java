package cn.eyesw.lvenxunjian.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import cn.eyesw.greendao.LatlngEntityDao;
import cn.eyesw.lvenxunjian.LoginActivity;
import cn.eyesw.lvenxunjian.LvenXunJianApplication;
import cn.eyesw.lvenxunjian.R;
import cn.eyesw.lvenxunjian.base.BaseActivity;
import cn.eyesw.lvenxunjian.base.BaseListViewAdapter;
import cn.eyesw.lvenxunjian.bean.LatlngEntity;
import cn.eyesw.lvenxunjian.constant.Constant;
import cn.eyesw.lvenxunjian.constant.NetworkApi;
import cn.eyesw.lvenxunjian.utils.DialogUtil;
import cn.eyesw.lvenxunjian.utils.OkHttpManager;
import cn.eyesw.lvenxunjian.utils.SpUtil;
import cn.eyesw.lvenxunjian.utils.ToolbarUtil;
import okhttp3.Call;

/**
 * 设置界面
 */
public class SettingActivity extends BaseActivity {

    private EditText mEtOldPassword;
    private EditText mEtNewPassword;
    private String mOldPassword;
    private String mNewPassword;

    @BindView(R.id.setting_toolbar)
    protected Toolbar mToolbar;
    @BindView(R.id.setting_list_view)
    protected ListView mListView;

    @Override
    protected int getContentLayoutRes() {
        return R.layout.activity_setting;
    }

    @Override
    protected void initToolbar() {
        ToolbarUtil toolbarUtil = new ToolbarUtil(this);
        toolbarUtil.setToolbar(mToolbar, "设置");
    }

    @Override
    protected void initView() {
        String[] content = new String[]{"用户资料", "清理图片", "更改密码"};
        List<String> list = new ArrayList<>();
        Collections.addAll(list, content);

        mListView.setAdapter(new SettingAdapter(mContext, list));
    }

    @OnItemClick(R.id.setting_list_view)
    public void onItemClick(int position) {
        switch (position) {
            case 0:
                startActivity(UserActivity.class);
                break;
            case 1:
                // 递归删除文件
                File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File[] files = directory.listFiles();
                new Thread() {
                    @Override
                    public void run() {
                        deletePhotoFile(files);
                    }
                }.start();
                break;
            case 2:
                changePasswordDialog();
                break;
            default:
                throw new RuntimeException(getString(R.string.unkown_error));
        }
    }

    /**
     * 更改密码的对话框
     */
    private void changePasswordDialog() {
        Dialog dialog = DialogUtil.getDialog(this, R.layout.dialog_change_password);
        mEtOldPassword = ButterKnife.findById(dialog, R.id.et_old_password);
        mEtNewPassword = ButterKnife.findById(dialog, R.id.et_new_password);
        Button btnSave = ButterKnife.findById(dialog, R.id.btn_save_password);

        mEtOldPassword.addTextChangedListener(mTextWatcher);
        mEtNewPassword.addTextChangedListener(mTextWatcher);
        btnSave.setOnClickListener(v -> {
            // 提价新密码
            if (TextUtils.isEmpty(mOldPassword) || TextUtils.isEmpty(mNewPassword)) {
                showToast("旧密码和新密码不能为空");
                return;
            }
            Map<String, String> map = new HashMap<>();
            SpUtil spUtil = SpUtil.getInstance(getApplicationContext());
            map.put("staff_id", spUtil.getString("id"));
            map.put("old_password", mOldPassword);
            map.put("password", mNewPassword);
            OkHttpManager.getInstance().postAsyncForm(NetworkApi.STAFF_PASSWORD, map, new OkHttpManager.DataCallback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    dialog.dismiss();
                    showToast(getString(R.string.network_error));
                    call.cancel();
                }

                @Override
                public void onResponse(String json) {
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        int code = jsonObject.getInt("code");
                        if (code == 200) {
                            showToast("保存成功");
                            Intent intent = new Intent(mContext, LoginActivity.class);
                            spUtil.putBoolean("isLogin", false);
                            spUtil.putString("phone", "");
                            spUtil.remove(Constant.VERSION_UPDATE);
                            spUtil.remove(Constant.APK_URL);

                            // 退出登录清空数据库
                            LatlngEntityDao latlngEntityDao = LvenXunJianApplication.getDaoSession().getLatlngEntityDao();
                            if (latlngEntityDao != null) {
                                List<LatlngEntity> list = latlngEntityDao.loadAll();
                                if (list.size() > 0) {
                                    latlngEntityDao.deleteAll();
                                }
                            }
                            dialog.dismiss();
                            startActivity(intent);
                            finish();
                        } else {
                            showToast("保存失败，请重新填写密码");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        dialog.dismiss();
                    }
                }
            });
        });
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mOldPassword = mEtOldPassword.getText().toString().trim();
            mNewPassword = mEtNewPassword.getText().toString().trim();
        }
    };

    /**
     * 删除图片文件
     *
     * @param files 文件数组
     */
    private void deletePhotoFile(File[] files) {
        for (File file : files) {
            if (file.isFile()) {
                file.delete();
            } else {
                deletePhotoFile(file.listFiles());
            }
        }
        runOnUiThread(() -> showToast("清理成功"));
    }

    private class SettingAdapter extends BaseListViewAdapter<String> {

        SettingAdapter(Context context, List<String> datas) {
            super(context, datas);
        }

        @Override
        protected int getAdapterLayoutRes() {
            return R.layout.setting_list_view;
        }

        @Override
        protected void initAdapterView(View convertView, String s) {
            TextView tvTitle = ButterKnife.findById(convertView, R.id.setting_item_tv_title);
            tvTitle.setText(s);
        }
    }

}
