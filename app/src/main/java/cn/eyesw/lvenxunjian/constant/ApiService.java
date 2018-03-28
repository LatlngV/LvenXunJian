package cn.eyesw.lvenxunjian.constant;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * 数据接口
 */
public interface ApiService {

    /**
     * 隐患点
     */
    @GET("map_yinhuan")
    Call<ResponseBody> dangerPoint();

    /**
     * 维修数据接口，包括已完成和未完成的
     */
    @FormUrlEncoded
    @POST("weixiu_list")
    Call<ResponseBody> repairList(@Field("staff_id") String staffId);

    /**
     * 管理员手机端数据上报
     */
    @FormUrlEncoded
    @POST("get_xunjian_data")
    Call<ResponseBody> dataReport(@Field("staff_id") String staffId, @Field("type_flg") String deviceType, @Field("status") String status);

    /**
     * 维修上传新建数据
     */
    @FormUrlEncoded
    @POST("save_data")
    Call<ResponseBody> saveData(@Field("staff_id") String staffId, @Field("task_id") String taskId, @Field("line_point_id") String linePointId,
                                @Field("latitude") String latitude, @Field("longitude") String longitude, @Field("address") String address,
                                @Field("weather") String weather, @Field("note") String note, @Field("type_flg") String typeFlag);

    /**
     * 隐患上传新建数据
     */
    @FormUrlEncoded
    @POST("save_data")
    Call<ResponseBody> saveDangerData(@Field("staff_id") String staffId, @Field("task_id") String taskId, @Field("line_point_id") String linePointId,
                                      @Field("latitude") String latitude, @Field("longitude") String longitude, @Field("address") String address,
                                      @Field("weather") String weather, @Field("note") String note, @Field("type_flg") String typeFlag,
                                      @Field("depth") String depth, @Field("finish_time") String finishTime, @Field("status") String status,
                                      @Field("test_cap") String testCap, @Field("connect_info") String connectInfo, @Field("power1") String power1,
                                      @Field("power2") String power2, @Field("power3") String power3);

    /**
     * 上传照片
     */
    @FormUrlEncoded
    @POST("upload_img")
    Call<ResponseBody> uploadImg(@Field("data_id") String dataId, @Field("file_type") String fileType, @Field("file") String file,
                                 @Field("file_name") String fileName, @Field("line_point_id") String linePointId);

    /**
     * 上传更新的数据
     */
    @FormUrlEncoded
    @POST("update_data")
    Call<ResponseBody> updateData(@Field("data_id") String dataId, @Field("latitude") String latitude, @Field("longitude") String longitude,
                                  @Field("address") String address, @Field("weather") String weather, @Field("note") String note,
                                  @Field("type_flg") String typeFlag, @Field("require_status") String requireStatus);

    /**
     * 维修前照片
     */
    @FormUrlEncoded
    @POST("report_detail")
    Call<ResponseBody> dataDetail(@Field("data_id") String dataId);

    /**
     * 解析巡线员上传的照片
     */
    @FormUrlEncoded
    @POST("weixiu_content")
    Call<ResponseBody> repairPhoto(@Field("weixiu_id") String dataId);

    /**
     * 维修后数据
     */
    @FormUrlEncoded
    @POST("save_weixiu_check")
    Call<ResponseBody> saveRepairCheck(@Field("id") String dataId, @Field("note") String note, @Field("latitude") String latitude,
                                       @Field("longitude") String longitude, @Field("address") String address);

    @FormUrlEncoded
    @POST("upload_weixiu_check_img")
    Call<ResponseBody> uploadRepairImg(@Field("weixiu_data_id") String dataId, @Field("file") String file, @Field("file_name") String fileName);

    /**
     * 所有的管道数据
     */
    @GET("line_position")
    Call<ResponseBody> allPipeline();

    /**
     * 巡线区
     */
    @FormUrlEncoded
    @POST("staff_team")
    Call<ResponseBody> patrolArea(@Field("staff_id") String staffId);

    /**
     * 上传当前位置
     */
    @FormUrlEncoded
    @POST("staff_position")
    Call<ResponseBody> staffPosition(@Field("staff_id") String staffId, @Field("latitude") String latitude, @Field("longitude") String longitude);

    /**
     * 上传当前位置
     */
    @FormUrlEncoded
    @POST("staff_position")
    Call<ResponseBody> sendPosition(@FieldMap Map<String, String> map);

}
