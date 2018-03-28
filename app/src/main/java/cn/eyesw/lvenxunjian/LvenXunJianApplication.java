package cn.eyesw.lvenxunjian;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.os.StrictMode;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;

import cn.eyesw.greendao.DaoMaster;
import cn.eyesw.greendao.DaoSession;
import cn.eyesw.lvenxunjian.constant.Constant;

/**
 * 巡检 APP
 */
public class LvenXunJianApplication extends Application {

    private static DaoSession sDaoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化百度地图
        SDKInitializer.initialize(getApplicationContext());
        SDKInitializer.setCoordType(CoordType.BD09LL);

        // 适配 Android 7.0 FileProvider 问题
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        // 初始化 GreenDao
        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(this, Constant.DATABASE_NAME, null);
        SQLiteDatabase sqLiteDatabase = devOpenHelper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(sqLiteDatabase);
        sDaoSession = daoMaster.newSession();
    }

    public static DaoSession getDaoSession() {
        return sDaoSession;
    }

}
