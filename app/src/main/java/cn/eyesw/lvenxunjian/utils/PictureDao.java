package cn.eyesw.lvenxunjian.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import cn.eyesw.lvenxunjian.LvenXunJianApplication;
import cn.eyesw.lvenxunjian.bean.PictureBean;
import cn.eyesw.lvenxunjian.database.DBHelper;

/**
 * 操作数据库的工具类
 */
public class PictureDao {

    private DBHelper mDBHelper;

    public PictureDao(Context context) {
        mDBHelper = new DBHelper(context);
    }

    /**
     * 增
     */
    public void add(PictureBean pictureBean) {
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("bitmap", pictureBean.getPicture());
        values.put("latitude", pictureBean.getLatitude());
        values.put("longitude", pictureBean.getLongitude());
        values.put("date", pictureBean.getDate());
        values.put("file_name", pictureBean.getFileName());
        database.insert("picture", null, values);
        database.close();
    }

    /**
     * 删
     */
    public void delete() {
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        database.delete("picture", null, null);
        database.close();
    }

    /**
     * 改
     */
    public void update(PictureBean pictureBean) {
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("bitmap", pictureBean.getPicture());
        database.update("picture", values, null, null);
        database.close();
    }

    /**
     * 查
     */
    public List<PictureBean> query() {
        List<PictureBean> list = new ArrayList<>();
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        Cursor cursor = database.query("picture", null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String picture = cursor.getString(0);
            String longitude = cursor.getString(1);
            String latitude = cursor.getString(2);
            String date = cursor.getString(3);
            String fileName = cursor.getString(4);

            PictureBean pictureBean = new PictureBean(picture, longitude, latitude, date, fileName);
            list.add(pictureBean);
        }
        cursor.close();
        database.close();
        return list;
    }

}
