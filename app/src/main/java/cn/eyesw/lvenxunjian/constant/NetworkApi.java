package cn.eyesw.lvenxunjian.constant;

/**
 * 网络请求数据的 API
 */
public class NetworkApi {

    // 中世 base_url
    public static final String BASE_URI = "http://121.42.136.94/gh_admin/api";
    public static final String BASE_URI_ZS = "http://121.42.136.94/gh_admin/api/";
    // 登陆
    public static final String LOGIN = BASE_URI + "/login";

    // 员工详情
    public static final String STAFF_DETAIL = BASE_URI + "/staff_detail";
    // 上传头像
    public static final String STAFF_PORTRAIT = BASE_URI + "/staff_portrait";
    // 隐患级别
    public static final String DANGER_LEVEL = BASE_URI + "/danger_level";

    // 通知公告详情
    public static final String NOTICE_DETAIL = BASE_URI + "/notice_detail";

    // 规章列表
    public static final String RULES = BASE_URI + "/rules";
    // 规章详细
    public static final String RULE_DETAIL = BASE_URI + "/rule_detail";

    // GPS 定位
    public static final String GPS = BASE_URI + "/staff_gps";
    // 地图
    public static final String MAP = BASE_URI + "/google_map";

    // 提交抢维修上报的图片
    public static final String UPLOAD_WEIXIU_IMG = BASE_URI + "/upload_weixiu_img";
    // 提交抢维修数据
    public static final String SAVE_WEIXIU = BASE_URI + "/save_weixiu";
    // 隐患类型
    public static final String DANGER_TYPE = BASE_URI + "/danger_type";
    // 提交隐患上报的图片
    public static final String UPLOAD_DANGER_IMG = BASE_URI + "/upload_danger_img";
    // 提交抢维修数据
    public static final String SAVE_DANGER = BASE_URI + "/save_danger";
    // 显示点的个数
    public static final String LINE_POINT_LIST = BASE_URI + "/app_line_point_list";

    // 开始巡检
    public static final String STAFF_SIGN = BASE_URI + "/staff_sign";
    // 停止巡检
    public static final String STAFF_UNSIGN = BASE_URI + "/staff_unsign";
    // 打卡状态
    public static final String STAFF_STATUS = BASE_URI + "/staff_status";

    // 检测版本号
    public static final String VERSION = BASE_URI + "/version";

    // 查询年度记录
    public static final String YEAR_RECORD = BASE_URI + "/xunjian_month_list";
    // 查询月度记录
    public static final String MONTH_RECORD = BASE_URI + "/xunjian_day_list";

    // 我的消息
    public static final String MY_MESSAGE = BASE_URI + "/staff_message";

    // 上传巡检任务的照片
    public static final String UPLOAD_TASK_IMG = BASE_URI + "/upload_task_img";

    // 巡线员管线
    public static final String PIPELINE_POINT = BASE_URI + "/get_part_position_by_staff";
    // 巡线员必经点
    public static final String STAFF_POINT = BASE_URI + "/get_part_point_by_staff";

    // 巡检任务
    public static final String TASK = BASE_URI + "/tasks";

}
