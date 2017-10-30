package cn.eyesw.lvenxunjian.bean;

/**
 * 图片的实体类
 */
public class PictureBean {

    // 图片
    private String mPicture;
    // 经度
    private String mLongitude;
    // 纬度
    private String mLatitude;
    // 日期
    private String mDate;
    // 文件名
    private String mFileName;

    public PictureBean(String picture, String longitude, String latitude, String date, String fileName) {
        mPicture = picture;
        mLongitude = longitude;
        mLatitude = latitude;
        mDate = date;
        mFileName = fileName;
    }

    public String getPicture() {
        return mPicture;
    }

    public String getLongitude() {
        return mLongitude;
    }

    public String getLatitude() {
        return mLatitude;
    }

    public String getDate() {
        return mDate;
    }

    public String getFileName() {
        return mFileName;
    }

}
