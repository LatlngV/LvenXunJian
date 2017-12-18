package cn.eyesw.lvenxunjian.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class LatlngEntity {

    private double latitude;
    private double longitude;
    private String flag;

    @Generated(hash = 1499005332)
    public LatlngEntity(double latitude, double longitude, String flag) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.flag = flag;
    }

    @Generated(hash = 587243139)
    public LatlngEntity() {
    }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getFlag() {
        return this.flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

}
