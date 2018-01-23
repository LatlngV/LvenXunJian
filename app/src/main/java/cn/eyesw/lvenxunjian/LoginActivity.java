package cn.eyesw.lvenxunjian;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import cn.eyesw.lvenxunjian.base.BaseActivity;
import cn.eyesw.lvenxunjian.constant.NetworkApi;
import cn.eyesw.lvenxunjian.utils.DialogUtil;
import cn.eyesw.lvenxunjian.utils.SpUtil;
import me.weyye.hipermission.PermissionItem;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 登陆界面
 */
public class LoginActivity extends BaseActivity {

    private String mPhoneNumber;
    private String mPassword;
    private String mPhone;
    private ProgressDialog mProgressDialog;
    private Intent mIntent;
    // 定义一个变量用来记录时间戳
    private long startTime;

    @BindView(R.id.login_toolbar)
    protected Toolbar mToolbar;
    @BindView(R.id.login_et_username)
    protected EditText mEtPhoneNumber;
    @BindView(R.id.login_et_password)
    protected EditText mEtPassword;

    @Override
    protected int getContentLayoutRes() {
        return R.layout.activity_login;
    }

    @Override
    public void initToolbar() {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.login));
        }
    }

    @Override
    protected void initView() {
    }

    @OnTextChanged({R.id.login_et_username, R.id.login_et_password})
    public void onTextChanged() {
        mPhoneNumber = mEtPhoneNumber.getText().toString().trim();
        mPassword = mEtPassword.getText().toString().trim();
    }

    @OnClick({R.id.login_btn_login, R.id.login_tv_forget_password})
    public void onClick(View view) {
        switch (view.getId()) {
            // 登陆按钮
            case R.id.login_btn_login:
                // 登陆
                login();
                break;
            // 忘记密码按钮
            case R.id.login_tv_forget_password:
                // 弹出对话框
                findPassword();
                break;
            default:
                throw new RuntimeException(getString(R.string.unkown_error));
        }
    }

    /**
     * 登陆
     */
    private void login() {
        if (TextUtils.isEmpty(mPhoneNumber)) {
            Toast.makeText(mContext, "手机号码不能为空", Toast.LENGTH_SHORT).show();

            return;
        }
        if (TextUtils.isEmpty(mPassword)) {
            Toast.makeText(mContext, "用户密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        mProgressDialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("正在登陆");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();

        OkHttpClient client = new OkHttpClient();
        FormBody formBody = new FormBody.Builder()
                .add("staff_phone", mPhoneNumber)
                .add("staff_password", mPassword)
                .build();
        Request request = new Request.Builder()
                .url(NetworkApi.LOGIN)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(mContext, "网络连接失败", Toast.LENGTH_SHORT).show();
                    mProgressDialog.dismiss();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // 上传数据成功后返回的 json 数据
                String json = response.body().string();
                progressLoginData(json);
            }
        });
    }

    /**
     * 解析 json 数据
     */
    private void progressLoginData(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            int code = obj.getInt("code");
            if (code == 200) {
                mProgressDialog.dismiss();
                JSONObject userInfo = obj.getJSONObject("data").getJSONObject("user_info");
                String id = userInfo.getString("id");
                String staffNum = userInfo.getString("staff_num");
                String staffName = userInfo.getString("staff_name");
                String staffPhone = userInfo.getString("staff_phone");
                String roleName = userInfo.getString("role_name");

                // 保存到 SharedPreferences
                SpUtil spUtil = SpUtil.getInstance(mContext);
                spUtil.putString("id", id);
                spUtil.putString("staffNum", staffNum);
                spUtil.putString("staffName", staffName);
                spUtil.putString("staffPhone", staffPhone);
                spUtil.putString("roleName", roleName);
                spUtil.putString("password", mPassword);
                spUtil.putBoolean("isLogin", true);

                Intent intent;
                if (roleName.equals("巡检人员")) {
                    intent = new Intent(LoginActivity.this, MainActivity.class);
                } else {
                    intent = new Intent(LoginActivity.this, HomeActivity.class);
                }
                startActivity(intent);
                finish();

            } else if (code == 400) {
                runOnUiThread(() -> {
                    Toast.makeText(mContext, "登录失败", Toast.LENGTH_SHORT).show();
                    mProgressDialog.dismiss();
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() -> mProgressDialog.dismiss());
        }
    }

    /**
     * 找回密码
     */
    private void findPassword() {
        Dialog dialog = DialogUtil.getTitleDialog(this, R.layout.dialog_find_password, "紧急电话");

        TextView tvPhoneNumber = ButterKnife.findById(dialog, R.id.dialog_tv_phone_number);
        mPhone = SpUtil.getInstance(mContext).getString("phone");
        tvPhoneNumber.setText(mPhone);

        tvPhoneNumber.setOnClickListener(v -> {
            List<PermissionItem> permissions = new ArrayList<>();
            permissions.add(new PermissionItem(Manifest.permission.CALL_PHONE, "打电话", R.drawable.permission_ic_phone));
            permission(permissions, this::callToContacts);
        });
    }

    /**
     * 拨打紧急联系人电话
     */
    private void callToContacts() {
        mIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mPhone));
        startActivity(mIntent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - startTime > 2000) {
                Toast.makeText(mContext, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                startTime = System.currentTimeMillis();
                return true;
            } else {
                System.exit(0);
            }
        }
        return false;
    }

}
