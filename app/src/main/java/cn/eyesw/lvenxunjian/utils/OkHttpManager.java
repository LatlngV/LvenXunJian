package cn.eyesw.lvenxunjian.utils;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Okhttp 的封装
 */
public class OkHttpManager {

    private static volatile OkHttpManager sOkHttpManager;
    private OkHttpClient mOkHttpClient;
    private static Handler mHandler;

    public static OkHttpManager getInstance() {
        if (sOkHttpManager == null) {
            synchronized (OkHttpManager.class) {
                if (sOkHttpManager == null) {
                    sOkHttpManager = new OkHttpManager();
                }
            }
        }
        return sOkHttpManager;
    }

    private OkHttpManager() {
        mOkHttpClient = new OkHttpClient.Builder()
                .readTimeout(1, TimeUnit.SECONDS)
                .writeTimeout(1, TimeUnit.SECONDS)
                .connectTimeout(1, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build();
        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 异步请求数据
     */
    public void getAsync(String url, DataCallback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                deliverDataFailure(call, e, callback);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String json = response.body().string();
                    deliverDataSuccess(json, callback);
                } catch (IOException e) {
                    deliverDataFailure(call, e, callback);
                }
            }
        });
    }

    /**
     * 异步提交表单
     */
    public void postAsyncForm(String url, Map<String, String> params, DataCallback callback) {
        if (!url.equals("")) {
            FormBody.Builder formBody = new FormBody.Builder();
            if (params == null) params = new HashMap<>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (value == null) {
                    value = "";
                }
                formBody.add(key, value);
            }
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody.build())
                    .build();
            mOkHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    deliverDataFailure(call, e, callback);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String json = response.body().string();
                    deliverDataSuccess(json, callback);
                }
            });
        }
    }

    private void deliverDataFailure(Call call, IOException e, DataCallback callback) {
        mHandler.post(() -> {
            if (callback != null) callback.onFailure(call, e);
        });
    }

    private void deliverDataSuccess(String json, DataCallback callback) {
        mHandler.post(() -> {
            if (callback != null) callback.onResponse(json);
        });
    }

    /**
     * 数据回调接口
     */
    public interface DataCallback {
        // 失败后回调
        void onFailure(Call call, IOException e);

        // 成功后回调
        void onResponse(String json);
    }

}
