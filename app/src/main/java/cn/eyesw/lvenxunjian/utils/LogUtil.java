package cn.eyesw.lvenxunjian.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 日志工具类
 */
public class LogUtil {

    /**
     * 控制全局 Log 的总开关
     * 如果为 True，输出 Log
     * 如果为 False，关闭 Log
     */
    private static boolean sLogSwitch = true;
    private static String sTag = "ApplicationLog";

    public static void setSwitch(boolean logSwitch) {
        sLogSwitch = logSwitch;
    }

    public static void setTag(String tag) {
        sTag = tag;
    }

    public static void error(String msg) {
        if (sLogSwitch) {
            Log.d(sTag, "error: == " + msg);
        }
    }

    public static void error(String tag, String msg) {
        if (sLogSwitch) {
            Log.d(tag, "error: == " + msg);
        }
    }

    public static void warn(String msg) {
        if (sLogSwitch) {
            Log.w(sTag, "warn: == " + msg);
        }
    }

    public static void warn(String tag, String msg) {
        if (sLogSwitch) {
            Log.w(tag, "warn: == " + msg);
        }
    }

    public static void debug(String msg) {
        if (sLogSwitch) {
            Log.d(sTag, "debug: == " + msg);
        }
    }

    public static void debug(String tag, String msg) {
        if (sLogSwitch) {
            Log.d(tag, "debug: == " + msg);
        }
    }

    public static void json(String json) {
        if (sLogSwitch) {
            Log.d(sTag, "json: == " + formatJSON(json));
        }
    }

    public static void json(String tag, String json) {
        if (sLogSwitch) {
            Log.d(tag, "json: == " + formatJSON(json));
        }
    }

    /**
     * 格式化 json 字符串
     *
     * @param json 需要格式化的 json 字符串
     * @return 返回格式化的 json 字符串
     */
    private static String formatJSON(String json) {
        try {
            if (json.startsWith("{")) {
                json = new JSONObject(json).toString(4);
            } else if (json.startsWith("[")) {
                json = new JSONArray(json).toString(4);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

}
