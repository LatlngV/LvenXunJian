package cn.eyesw.lvenxunjian.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences 工具类
 */
public class SpUtil {

    private static SpUtil sSpUtil;
    private final SharedPreferences mPreferences;
    private static final String LVEN_XUN_JIAN = "LvenXunJian";

    /**
     * 单例获取 SpUtil 对象
     */
    public static SpUtil getInstance(Context context) {
        if (sSpUtil == null) {
            synchronized (SpUtil.class) {
                sSpUtil = new SpUtil(context);
            }
        }
        return sSpUtil;
    }

    private SpUtil(Context context) {
        mPreferences = context.getSharedPreferences(LVEN_XUN_JIAN, Context.MODE_PRIVATE);
    }
    /**
     * 写入 boolean 变量至 SharedPreferences 中
     */
    public void putBoolean(String key, boolean value) {
        mPreferences.edit().putBoolean(key, value).apply();
    }
    /**
     * 读取 boolean 标示从 SharedPreferences 中
     */
    public boolean getBoolean(String key) {
        return mPreferences.getBoolean(key, false);
    }

    /**
     * 写入 String 变量至 SharedPreferences 中
     */
    public void putString(String key, String value) {
        mPreferences.edit().putString(key, value).apply();
    }

    /**
     * 读取 String 标示从 SharedPreferences 中
     */
    public String getString(String key) {
        return mPreferences.getString(key, "");
    }

    /**
     * 写入 int 变量至 SharedPreferences 中
     */
    public void putInt(String key, int value) {
        mPreferences.edit().putInt(key, value).apply();
    }

    /**
     * 读取 int 标示从 SharedPreferences 中
     */
    public int getInt(String key) {
        return mPreferences.getInt(key, 0);
    }

    /**
     * 从 SharedPreferences 中移除指定节点
     *
     * @param key 需要移除节点的名称
     */
    public void remove(String key) {
        mPreferences.edit().remove(key).apply();
    }

}
