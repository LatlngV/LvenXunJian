package cn.eyesw.lvenxunjian.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 日志工具类，采用构建者模式
 */
public class LogUtil {

    private boolean mSwitch;
    private String mTag;

    public LogUtil() {

    }

    public LogUtil(Builder builder) {
        mSwitch = builder.mLogSwitch;
        mTag = builder.mLogTag;
    }

    /**
     * 设置 log 的开关
     *
     * @param aSwitch 开关，true 表示开关打开， false 表示开关关闭
     */
    public void setSwitch(boolean aSwitch) {
        mSwitch = aSwitch;
    }

    /**
     * 设置 TAG
     *
     * @param tag 设置 log 的标签
     */
    public void setTag(String tag) {
        mTag = tag;
    }

    public void debug(CharSequence msg) {
        if (mSwitch) {
            Log.d(mTag, "debug: " + msg);
        }
    }

    public void debug(String tag, CharSequence msg) {
        if (mSwitch) {
            Log.d(tag, "debug: " + msg);
        }
    }

    public void error(CharSequence msg) {
        if (mSwitch) {
            Log.d(mTag, "error: " + msg);
        }
    }

    public void error(String tag, CharSequence msg) {
        if (mSwitch) {
            Log.d(tag, "error: " + msg);
        }
    }

    public static class Builder {

        // 控制 Log 的开关，全局管用
        private boolean mLogSwitch;
        // 控制全局的 Tag
        private String mLogTag;

        public Builder setLogSwitch(boolean logSwitch) {
            mLogSwitch = logSwitch;
            return this;
        }

        public Builder setLogTag(String logTag) {
            mLogTag = logTag;
            return this;
        }

        public LogUtil build() {
            return new LogUtil(this);
        }

    }

}
