package cn.eyesw.lvenxunjian.fragment;

import android.content.Context;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.eyesw.lvenxunjian.R;
import cn.eyesw.lvenxunjian.base.BaseFragment;
import cn.eyesw.lvenxunjian.base.BaseListViewAdapter;
import cn.eyesw.lvenxunjian.constant.NetworkApi;
import cn.eyesw.lvenxunjian.utils.OkHttpManager;
import cn.eyesw.lvenxunjian.utils.SpUtil;
import okhttp3.Call;

/**
 * 年度巡检记录
 */
public class YearRecordFragment extends BaseFragment {

    private List<Map<String, String>> mDatas;

    @BindView(R.id.year_record_list_view)
    protected ListView mListView;
    @BindView(R.id.year_record_tv_empty)
    protected TextView mTvEmpty;

    @Override
    public int getContentLayoutRes() {
        return R.layout.fragment_year_record;
    }

    @Override
    protected void initView() {
        mDatas = new ArrayList<>();

        Map<String, String> map = new HashMap<>();
        map.put("staff_id", SpUtil.getInstance(mContext).getString("id"));
        OkHttpManager.getInstance().postAsyncForm(NetworkApi.YEAR_RECORD, map, new OkHttpManager.DataCallback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(String json) {
                try {
                    JSONObject object = new JSONObject(json);
                    int code = object.getInt("code");
                    if (code == 200) {
                        JSONArray yearRecord = object.getJSONArray("xunjian_month_list");
                        for (int i = yearRecord.length() - 1; i >= 0; i--) {
                            JSONObject data = (JSONObject) yearRecord.get(i);
                            String month = data.getString("total_date");
                            String number = data.getString("number");
                            String complete = data.getString("complete");
                            String percent = data.getString("percent");
                            Map<String, String> hashMap = new HashMap<>();
                            hashMap.put("month", month);
                            hashMap.put("number", number);
                            hashMap.put("complete", complete);
                            hashMap.put("percent", percent);
                            mDatas.add(hashMap);
                        }
                        mListView.setAdapter(new YearRecordAdapter(mContext, mDatas));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private class YearRecordAdapter extends BaseListViewAdapter<Map<String, String>> {

        YearRecordAdapter(Context context, List<Map<String, String>> datas) {
            super(context, datas);
        }

        @Override
        protected int getAdapterLayoutRes() {
            return R.layout.month_record;
        }

        @Override
        protected void initAdapterView(View view, Map<String, String> map) {
            TextView tvMonth = ButterKnife.findById(view, R.id.item_tv_date);
            TextView tvCompleteNumber = ButterKnife.findById(view, R.id.item_tv_complete_number);
            TextView tvTotalNumber = ButterKnife.findById(view, R.id.item_tv_total_number);
            TextView tvPercent = ButterKnife.findById(view, R.id.item_tv_percent);

            tvMonth.setText(map.get("month"));
            tvCompleteNumber.setText(map.get("complete"));
            tvTotalNumber.setText(map.get("number"));
            String format = String.format(Locale.CHINA, "%s%%", map.get("percent"));
            tvPercent.setText(map.get("percent"));
        }
    }

}
