package cn.eyesw.lvenxunjian.ui;

import android.content.Context;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import cn.eyesw.lvenxunjian.R;
import cn.eyesw.lvenxunjian.base.BaseActivity;
import cn.eyesw.lvenxunjian.base.BaseListViewAdapter;
import cn.eyesw.lvenxunjian.utils.ToolbarUtil;

/**
 * 设置界面
 */
public class SettingActivity extends BaseActivity {

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
        String[] content = new String[]{"用户资料", "清理图片"};
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
            default:
                throw new RuntimeException(getString(R.string.unkown_error));
        }
    }

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
