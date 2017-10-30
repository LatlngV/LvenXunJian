package cn.eyesw.lvenxunjian.ui;

import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import cn.eyesw.lvenxunjian.R;
import cn.eyesw.lvenxunjian.base.BaseActivity;
import cn.eyesw.lvenxunjian.constant.NetworkApi;
import cn.eyesw.lvenxunjian.utils.OkHttpManager;
import cn.eyesw.lvenxunjian.utils.SpUtil;
import cn.eyesw.lvenxunjian.utils.ToolbarUtil;
import okhttp3.Call;

/**
 * 公告详情
 */
public class DetailActivity extends BaseActivity {

    @BindView(R.id.detail_toolbar)
    protected Toolbar mToolbar;
    @BindView(R.id.detail_title)
    protected TextView mTvTitle;
    @BindView(R.id.detail_content)
    protected TextView mTvContent;

    @Override
    protected int getContentLayoutRes() {
        return R.layout.activity_detail;
    }

    @Override
    protected void initToolbar() {
        ToolbarUtil toolbarUtil = new ToolbarUtil(this);
        toolbarUtil.setToolbar(mToolbar, "详情");
    }

    @Override
    protected void initView() {
        initData();
    }

    private void initData() {
        String staffId = SpUtil.getInstance(mContext).getString("id");

        Map<String, String> map = new HashMap<>();
        map.put("staff_id", staffId);
        map.put("notice_id", getIntent().getStringExtra("notice_id"));
        OkHttpManager.getInstance().postAsyncForm(NetworkApi.NOTICE_DETAIL, map, new OkHttpManager.DataCallback() {
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

    private void analysisData(String json) {
        try {
            JSONObject object = new JSONObject(json);
            int code = object.getInt("code");
            if (code == 200) {
                JSONObject data = object.getJSONObject("data").getJSONObject("notice");
                String title = data.getString("title");
                String content = data.getString("content");

                mTvTitle.setText(title);
                mTvContent.setText(content);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
