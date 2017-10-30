package cn.eyesw.lvenxunjian.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import cn.eyesw.lvenxunjian.bean.StaffPointBean;
import cn.eyesw.lvenxunjian.constant.Constant;
import cn.eyesw.lvenxunjian.database.DBHelper;

public class StaffPointDao {

    private DBHelper mDBHelper;

    public StaffPointDao(Context context) {
        mDBHelper = new DBHelper(context);
    }

    /**
     * 增
     */
    public void add(StaffPointBean staffPointBean) {
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("latitude", staffPointBean.getLatitude());
        values.put("longitude", staffPointBean.getLongitude());
        database.insert(Constant.STAFF_POINT_TABLE, null, values);
        database.close();
    }

    /**
     * 删
     */
    public void delete() {
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        database.delete(Constant.STAFF_POINT_TABLE, null, null);
        database.close();
    }

    /**
     * 改
     */
    public void update() {
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        /* 需要更新相应数据 */
        database.close();
    }

    /**
     * 查
     */
    public List<StaffPointBean> select() {
        List<StaffPointBean> list = new ArrayList<>();
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        Cursor cursor = database.query(Constant.STAFF_POINT_TABLE, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            double latitude = cursor.getDouble(0);
            double longitude = cursor.getDouble(1);
            StaffPointBean staffPointBean = new StaffPointBean(latitude, longitude);
            list.add(staffPointBean);
        }
        cursor.close();
        database.close();
        return list;
    }

}
