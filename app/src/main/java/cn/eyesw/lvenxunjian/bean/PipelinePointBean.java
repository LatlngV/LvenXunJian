package cn.eyesw.lvenxunjian.bean;

public class PipelinePointBean {

    private double mLatitude;
    private double mLongitude;

    public PipelinePointBean(double latitude, double longitude) {
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

}
