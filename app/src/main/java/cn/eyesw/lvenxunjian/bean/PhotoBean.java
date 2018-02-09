package cn.eyesw.lvenxunjian.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class PhotoBean {

    private String bitmap;
    private String latitude;
    private String longitude;
    private String date;
    private String fileName;

    @Generated(hash = 1656878559)
    public PhotoBean(String bitmap, String latitude, String longitude, String date, String fileName) {
        this.bitmap = bitmap;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
        this.fileName = fileName;
    }

    @Generated(hash = 487180461)
    public PhotoBean() {
    }

    public String getBitmap() {
        return this.bitmap;
    }

    public void setBitmap(String bitmap) {
        this.bitmap = bitmap;
    }

    public String getLatitude() {
        return this.latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return this.longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


}
