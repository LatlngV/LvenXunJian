package cn.eyesw.lvenxunjian.utils;

import java.util.concurrent.TimeUnit;

import cn.eyesw.lvenxunjian.constant.ApiService;
import cn.eyesw.lvenxunjian.constant.NetworkApi;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * Retrofit 网络请求工具类
 */
public class NetWorkUtil {

    private static volatile NetWorkUtil sNetWorkUtil;
    private Retrofit mRetrofit;
    private ApiService mApiService;

    /**
     * 单例获取
     */
    public static NetWorkUtil getInstance() {
        if (sNetWorkUtil == null) {
            synchronized (NetWorkUtil.class) {
                if (sNetWorkUtil == null) {
                    sNetWorkUtil = new NetWorkUtil();
                }
            }
        }
        return sNetWorkUtil;
    }

    private NetWorkUtil() {
        // 初始化 OkHttpClient
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        // 初始化 Retrofit
        mRetrofit = new Retrofit.Builder()
                .baseUrl(NetworkApi.BASE_URI_ZS)
                .client(okHttpClient)
                .build();
    }

    public ApiService getApiService() {
        if (mApiService == null) {
            mApiService = mRetrofit.create(ApiService.class);
        }
        return mApiService;
    }

}
