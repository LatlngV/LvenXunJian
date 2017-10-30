package cn.eyesw.lvenxunjian.bean;

public class StaffPointBean {

    private double mLatitude;
    private double mLongitude;

    public StaffPointBean(double latitude, double longitude) {
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
