package cn.eyesw.lvenxunjian.utils;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;

public class BaiduMapUtil {

    public static void initLocation(LocationClient locationClient, BaiduMap baiduMap) {
        LocationClientOption option = new LocationClientOption();
        // 当前位置
        option.setIsNeedAddress(true);
        // 打开 gps
        option.setOpenGps(true);
        // 设置坐标类型
        option.setCoorType("bd09ll");
        // 定位的频率
        option.setScanSpan(1000 * 20);
        locationClient.setLocOption(option);
        locationClient.start();
        // 开启定位图层
        baiduMap.setMyLocationEnabled(true);
    }

}
