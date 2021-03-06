package cn.eyesw.lvenxunjian.constant;

/**
 * 常量
 */
public class Constant {

    // 打开相机的请求码
    public static final int CAMERA_REQUEST_CODE = 10;
    // 打开相册的请求码
    public static final int ALBUM_REQUEST_CODE = 20;
    // 裁剪图片的请求码
    public static final int CROP_REQUEST_CODE = 30;

    // 数据库名称
    public static final String DATABASE_NAME = "xunjian.db";
    // 保存图片的表
    public static final String PICTURE_TABLE = "picture";
    // 管道点表
    public static final String PIPELINE_POINT_TABLE = "pipelinePoint";
    // 巡线员必经点表
    public static final String STAFF_POINT_TABLE = "staffPoint";

    // 通知 UI 改变的通知
    public static final String TIME_CHANGED_ACTION  = "cn.eyesw.lvenxunjian.time_change_action";
    // 打印 log 的全局 TAG
    public static final String TAG = "LOG-TAG";

    // 是否版本更新
    public static final String VERSION_UPDATE = "versionUpdate";
    // 下载 apk 的 url
    public static final String APK_URL = "apkUrl";
    // 隐患数据标志位
    public static final String DANGER_DATA_FLAG = "dangerDataFlag";

}
