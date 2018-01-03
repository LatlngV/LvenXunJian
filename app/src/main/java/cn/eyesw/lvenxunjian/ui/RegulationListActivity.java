package cn.eyesw.lvenxunjian.ui;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import cn.eyesw.lvenxunjian.R;
import cn.eyesw.lvenxunjian.base.BaseActivity;
import cn.eyesw.lvenxunjian.base.BaseListViewAdapter;
import cn.eyesw.lvenxunjian.constant.NetworkApi;
import cn.eyesw.lvenxunjian.utils.ToolbarUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 法律列表
 */
public class RegulationListActivity extends BaseActivity {

    private List<Map<String, String>> mDatas;

    @BindView(R.id.regulation_toolbar)
    protected Toolbar mToolbar;
    @BindView(R.id.regulation_list_view)
    protected ListView mListView;

    @Override
    protected int getContentLayoutRes() {
        return R.layout.activity_regulation_list;
    }

    @Override
    protected void initToolbar() {
        ToolbarUtil toolbarUtil = new ToolbarUtil(this);
        toolbarUtil.setToolbar(mToolbar, "法律列表");
    }

    @Override
    protected void initView() {
        mDatas = new ArrayList<>();

        OkHttpClient client = new OkHttpClient();
        FormBody formBody = new FormBody.Builder()
                .add("staff_id", "id")
                .add("title", getIntent().getStringExtra("title"))
                .build();
        Request request = new Request.Builder()
                .url(NetworkApi.RULES)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(mContext, "网络连接失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String json = response.body().string();
                analysisData(json);
            }
        });
    }

    private void analysisData(String json) {
        try {
            JSONObject object = new JSONObject(json);
            int code = object.getInt("code");
            if (code == 200) {
                JSONArray array = object.getJSONObject("data").getJSONArray("rules");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject data = (JSONObject) array.get(i);
                    String id = data.getString("id");
                    String chapter = data.getString("chapter");
                    Map<String, String> map = new HashMap<>();
                    map.put("id", id);
                    map.put("chapter", chapter);
                    mDatas.add(map);
                }
                runOnUiThread(() -> mListView.setAdapter(new RegulationAdapter(mDatas, mContext)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @OnItemClick(R.id.regulation_list_view)
    public void onItemClick(int position) {
        Intent intent = new Intent(mContext, RegulationDetailActivity.class);
        intent.putExtra("rule_id", mDatas.get(position).get("id"));
        startActivity(intent);

        //Bundle bundle = new Bundle();
        //bundle.putString("rule_id", mDatas.get(position).get("id"));
        //startActivity(RegulationDetailActivity.class, bundle);
    }

    private class RegulationAdapter extends BaseListViewAdapter<Map<String, String>> {

        RegulationAdapter(List<Map<String, String>> datas, Context context) {
            super(context, datas);
        }

        @Override
        protected int getAdapterLayoutRes() {
            return R.layout.regulation_list_view;
        }

        @Override
        protected void initAdapterView(View view, Map<String, String> map) {
            TextView tvTitle = ButterKnife.findById(view, R.id.regulation_item_tv_title);
            tvTitle.setText(map.get("chapter"));
        }
    }

}
