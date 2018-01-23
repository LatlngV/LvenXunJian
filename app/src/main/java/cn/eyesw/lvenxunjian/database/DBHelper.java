package cn.eyesw.lvenxunjian.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cn.eyesw.lvenxunjian.constant.Constant;

/**
 * 创建数据库
 */
public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, Constant.DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建存图片的数据库表
        String sql = "create table if not exists " + Constant.PICTURE_TABLE + "(bitmap varchar, longitude varchar, latitude varchar, date varchar, file_name varchar)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
