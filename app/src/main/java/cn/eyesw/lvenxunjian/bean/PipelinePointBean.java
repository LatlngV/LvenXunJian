package cn.eyesw.lvenxunjian.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class PipelinePointBean {

    private double latitude;
    private double longitude;
    private long pipelineId;

    @Generated(hash = 1720292086)
    public PipelinePointBean(double latitude, double longitude, long pipelineId) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.pipelineId = pipelineId;
    }

    @Generated(hash = 152620428)
    public PipelinePointBean() {
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

    public long getPipelineId() {
        return this.pipelineId;
    }

    public void setPipelineId(long pipelineId) {
        this.pipelineId = pipelineId;
    }

}
