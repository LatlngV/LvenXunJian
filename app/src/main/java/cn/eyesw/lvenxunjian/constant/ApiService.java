package cn.eyesw.lvenxunjian.constant;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
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
    @GET("map_weixiu")
    Call<ResponseBody> dangerPoint();

    /**
     * 维修数据接口，包括已完成和未完成的
     */
    @FormUrlEncoded
    @POST("weixiu_list")
    Call<ResponseBody> repairList(@Field("staff_id") String staffId);

    /**
     * 抢维修图片
     */
    @FormUrlEncoded
    @POST("weixiu_content")
    Call<ResponseBody> dangerPhoto(@Field("weixiu_id") String repairId);

    /**
     * 管理员手机端数据上报
     */
    @FormUrlEncoded
    @POST("get_xunjian_data")
    Call<ResponseBody> dataReport(@Field("staff_id") String staffId, @Field("type_flg") String deviceType, @Field("status") String status);

    /**
     * 上传新建数据
     */
    @FormUrlEncoded
    @POST("save_data")
    Call<ResponseBody> saveData(@Field("staff_id") String staffId, @Field("task_id") String taskId, @Field("line_point_id") String linePointId,
                                @Field("latitude") String latitude, @Field("longitude") String longitude, @Field("address") String address,
                                @Field("weather") String weather, @Field("note") String note, @Field("type_flg") String typeFlag);

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

}
