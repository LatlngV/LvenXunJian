package cn.eyesw.lvenxunjian;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.eyesw.greendao.LatlngEntityDao;
import cn.eyesw.lvenxunjian.base.BaseActivity;
import cn.eyesw.lvenxunjian.bean.LatlngEntity;
import cn.eyesw.lvenxunjian.constant.Constant;
import cn.eyesw.lvenxunjian.constant.NetworkApi;
import cn.eyesw.lvenxunjian.ui.EmergencyAlarmActivity;
import cn.eyesw.lvenxunjian.ui.HiddenReportActivity;
import cn.eyesw.lvenxunjian.ui.LawRegulationActivity;
import cn.eyesw.lvenxunjian.ui.MyMessageActivity;
import cn.eyesw.lvenxunjian.ui.PatrolRecordActivity;
import cn.eyesw.lvenxunjian.ui.PatrolTaskActivity;
import cn.eyesw.lvenxunjian.ui.PatrolUploadActivity;
import cn.eyesw.lvenxunjian.ui.SettingActivity;
import cn.eyesw.lvenxunjian.ui.UserActivity;
import cn.eyesw.lvenxunjian.utils.DialogUtil;
import cn.eyesw.lvenxunjian.utils.OkHttpManager;
import cn.eyesw.lvenxunjian.utils.PermissionsUtil;
import cn.eyesw.lvenxunjian.utils.SpUtil;
import cn.eyesw.lvenxunjian.utils.UpdateManager;
import okhttp3.Call;

/**
 * 主界面
 */
public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    /* 定义一个时间戳，用来记录按下返回键的时间 */
    private long startTime;
    private OkHttpManager mOkHttpManager;
    private SpUtil mSpUtil;

    @BindView(R.id.main_drawer_layout)
    protected DrawerLayout mDrawerLayout;
    @BindView(R.id.main_toolbar)
    protected Toolbar mToolbar;
    @BindView(R.id.main_navigation_view)
    protected NavigationView mNavigationView;
    @BindView(R.id.main_tv_message)
    protected TextView mTvMessage;

    @Override
    protected int getContentLayoutRes() {
        return R.layout.activity_main;
    }

    @Override
    public void initToolbar() {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }

        // 设置 Toolbar 箭头的点击事件
        mToolbar.setNavigationOnClickListener(view -> {
            if (!mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    @Override
    protected void initView() {
        mSpUtil = SpUtil.getInstance(mContext);
        mOkHttpManager = OkHttpManager.getInstance();

        // 检测版本号
        boolean versionUpdate = mSpUtil.getBoolean(Constant.VERSION_UPDATE);
        if (versionUpdate) {
            showUpdateDialog();
        }
        // 监测是否填写了紧急联系人
        checkEmergencyContacts();

        // 初始化 DrawerLayout
        initDrawerLayout();

        // 获取底部通知栏消息
        getBottomMessage();
    }

    /**
     * 获取底部状态栏的信息
     */
    private void getBottomMessage() {
        Map<String, String> map = new HashMap<>();
        map.put("staff_id", mSpUtil.getString("id"));
        mOkHttpManager.postAsyncForm(NetworkApi.MY_MESSAGE, map, new OkHttpManager.DataCallback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(String json) {
                try {
                    JSONObject object = new JSONObject(json);
                    int code = object.getInt("code");
                    if (code == 200) {
                        JSONArray data = object.getJSONArray("data");
                        if (data.length() == 0) {
                            mTvMessage.setText("消息通知：无");
                        } else {
                            mTvMessage.setTextColor(Color.RED);
                            mTvMessage.setText("消息通知：有紧急消息");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 检测是否填写的紧急联系人
     */
    private void checkEmergencyContacts() {
        String phone = mSpUtil.getString("phone");
        if (phone.equals("")) {
            showContactsDialog();
        }
    }

    /**
     * 填写紧急电话的 Dialog
     */
    private void showContactsDialog() {
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
            mSpUtil.putString("phone", phone);
            dialog.dismiss();
        });
    }

    /**
     * 提示更新的对话框
     */
    private void showUpdateDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("版本升级");
        dialog.setMessage("有新版本，是否升级？");
        dialog.setPositiveButton("确定", (dialogInterface, i) -> {
            mSpUtil.putBoolean(Constant.VERSION_UPDATE, false);
            PermissionsUtil.requestPermission(this, PermissionsUtil.CODE_WRITE_EXTERNAL_STORAGE, mPermissionGrant);
        });
        dialog.setNegativeButton("取消", null);
        dialog.show();
    }

    private PermissionsUtil.PermissionGrant mPermissionGrant = requestCode -> {
        if (requestCode == PermissionsUtil.CODE_WRITE_EXTERNAL_STORAGE) {
            String apkUrl = mSpUtil.getString(Constant.APK_URL);
            UpdateManager updateManager = new UpdateManager(this, apkUrl);
            updateManager.checkUpdateInfo();
        }
    };

    /**
     * 初始化 DrawerLayout
     */
    private void initDrawerLayout() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 初始化 NavigationView
        initNavigationView();
    }

    /**
     * 初始化 NavigationView
     */
    private void initNavigationView() {
        // 初始化 NavigationView 的头布局
        View headerView = mNavigationView.getHeaderView(0);
        LinearLayout llUserInfo = ButterKnife.findById(headerView, R.id.navigation_view_ll_setting);
        final ImageView ivPortrait = ButterKnife.findById(headerView, R.id.navigayion_view_staff_portrait);
        final TextView tvName = ButterKnife.findById(headerView, R.id.navigation_view_staff_name);
        final TextView tvRole = ButterKnife.findById(headerView, R.id.navigation_view_staff_role);

        Map<String, String> map = new HashMap<>();
        map.put("staff_id", mSpUtil.getString("id"));
        mOkHttpManager.postAsyncForm(NetworkApi.STAFF_DETAIL, map, new OkHttpManager.DataCallback() {
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

                        mSpUtil.putString("portrait", portrait);
                        mSpUtil.putString("address", address);

                        if (!portrait.equals("")) {
                            Picasso.with(mContext)
                                    .load(portrait)
                                    .placeholder(R.drawable.center_portrait)
                                    .into(ivPortrait);
                        }
                        tvName.setText(staffName);
                        tvRole.setText(roleName);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        llUserInfo.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, UserActivity.class);
            startActivity(intent);
        });

        mNavigationView.setItemIconTintList(null);

        // 设置抽屉的监听
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * 抽屉的监听
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_navigation_view_switch_uesr:
                Intent intent = new Intent(mContext, LoginActivity.class);
                mSpUtil.putBoolean("isLogin", false);
                mSpUtil.putString("phone", "");
                mSpUtil.remove(Constant.VERSION_UPDATE);
                mSpUtil.remove(Constant.APK_URL);

                // 退出登录清空数据库
                LatlngEntityDao latlngEntityDao = LvenXunJianApplication.getDaoSession().getLatlngEntityDao();
                if (latlngEntityDao != null) {
                    List<LatlngEntity> list = latlngEntityDao.loadAll();
                    if (list.size() > 0) {
                        latlngEntityDao.deleteAll();
                    }
                }
                startActivity(intent);
                finish();
                break;
            case R.id.menu_navigation_view_law_regulations:
                intent = new Intent(mContext, LawRegulationActivity.class);
                startActivity(intent);
                break;
            default:
                throw new RuntimeException(getString(R.string.unkown_error));
        }
        // 关闭 DrawerLayout
        mDrawerLayout.closeDrawer(GravityCompat.START);

        // 返回 true，代表将该菜单项变为 checked 状态
        return true;
    }

    @OnClick({R.id.main_ll_patrol_task, R.id.main_ll_emergency_alarm, R.id.main_ll_hidden_report,
            R.id.main_ll_patrol_record, R.id.main_ll_notice, R.id.main_ll_setting, R.id.main_tv_message})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_ll_patrol_task:
                Intent intent = new Intent(mContext, PatrolTaskActivity.class);
                startActivity(intent);
                break;
            case R.id.main_ll_emergency_alarm:
                intent = new Intent(mContext, EmergencyAlarmActivity.class);
                startActivity(intent);
                break;
            case R.id.main_ll_hidden_report:
                intent = new Intent(mContext, HiddenReportActivity.class);
                startActivity(intent);
                break;
            case R.id.main_ll_patrol_record:
                intent = new Intent(mContext, PatrolRecordActivity.class);
                startActivity(intent);
                break;
            case R.id.main_ll_notice:
                intent = new Intent(mContext, PatrolUploadActivity.class);
                startActivity(intent);
                break;
            case R.id.main_ll_setting:
                intent = new Intent(mContext, SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.main_tv_message:
                intent = new Intent(mContext, MyMessageActivity.class);
                startActivity(intent);
                break;
            default:
                throw new RuntimeException(getString(R.string.unkown_error));
        }
    }

    /**
     * 按下返回键时，先判断 DrawerLayout 是不是打开的
     * 如果 DrawerLayout 是打开的，就让 DrawerLayout 关闭
     * 否则在 2s 时间内按两下返回键就退出程序
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
                if (System.currentTimeMillis() - startTime > 2000) {
                    Toast.makeText(mContext, "再按一次退出", Toast.LENGTH_SHORT).show();
                    startTime = System.currentTimeMillis();
                    return true;
                } else {
                    finish();
                }
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionsUtil.requestPermissionsResult(this, requestCode, permissions, grantResults, mPermissionGrant);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
