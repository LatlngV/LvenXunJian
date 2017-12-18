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

        // 创建存管道的数据库表
        String pipelineTable = "create table if not exists " + Constant.PIPELINE_POINT_TABLE + "(latitude double, longitude double)";
        db.execSQL(pipelineTable);

        // 创建存巡线员必经点的数据库表
        String staffPointTable = "create table if not exists " + Constant.STAFF_POINT_TABLE + "(latitude double, longitude double)";
        db.execSQL(staffPointTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
