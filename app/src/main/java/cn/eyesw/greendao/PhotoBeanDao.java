package cn.eyesw.greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import cn.eyesw.lvenxunjian.bean.PhotoBean;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "PHOTO_BEAN".
*/
public class PhotoBeanDao extends AbstractDao<PhotoBean, Void> {

    public static final String TABLENAME = "PHOTO_BEAN";

    /**
     * Properties of entity PhotoBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Bitmap = new Property(0, String.class, "bitmap", false, "BITMAP");
        public final static Property Latitude = new Property(1, String.class, "latitude", false, "LATITUDE");
        public final static Property Longitude = new Property(2, String.class, "longitude", false, "LONGITUDE");
        public final static Property Date = new Property(3, String.class, "date", false, "DATE");
        public final static Property FileName = new Property(4, String.class, "fileName", false, "FILE_NAME");
    }


    public PhotoBeanDao(DaoConfig config) {
        super(config);
    }
    
    public PhotoBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"PHOTO_BEAN\" (" + //
                "\"BITMAP\" TEXT," + // 0: bitmap
                "\"LATITUDE\" TEXT," + // 1: latitude
                "\"LONGITUDE\" TEXT," + // 2: longitude
                "\"DATE\" TEXT," + // 3: date
                "\"FILE_NAME\" TEXT);"); // 4: fileName
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"PHOTO_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, PhotoBean entity) {
        stmt.clearBindings();
 
        String bitmap = entity.getBitmap();
        if (bitmap != null) {
            stmt.bindString(1, bitmap);
        }
 
        String latitude = entity.getLatitude();
        if (latitude != null) {
            stmt.bindString(2, latitude);
        }
 
        String longitude = entity.getLongitude();
        if (longitude != null) {
            stmt.bindString(3, longitude);
        }
 
        String date = entity.getDate();
        if (date != null) {
            stmt.bindString(4, date);
        }
 
        String fileName = entity.getFileName();
        if (fileName != null) {
            stmt.bindString(5, fileName);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, PhotoBean entity) {
        stmt.clearBindings();
 
        String bitmap = entity.getBitmap();
        if (bitmap != null) {
            stmt.bindString(1, bitmap);
        }
 
        String latitude = entity.getLatitude();
        if (latitude != null) {
            stmt.bindString(2, latitude);
        }
 
        String longitude = entity.getLongitude();
        if (longitude != null) {
            stmt.bindString(3, longitude);
        }
 
        String date = entity.getDate();
        if (date != null) {
            stmt.bindString(4, date);
        }
 
        String fileName = entity.getFileName();
        if (fileName != null) {
            stmt.bindString(5, fileName);
        }
    }

    @Override
    public Void readKey(Cursor cursor, int offset) {
        return null;
    }    

    @Override
    public PhotoBean readEntity(Cursor cursor, int offset) {
        PhotoBean entity = new PhotoBean( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // bitmap
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // latitude
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // longitude
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // date
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4) // fileName
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, PhotoBean entity, int offset) {
        entity.setBitmap(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setLatitude(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setLongitude(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setDate(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setFileName(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
     }
    
    @Override
    protected final Void updateKeyAfterInsert(PhotoBean entity, long rowId) {
        // Unsupported or missing PK type
        return null;
    }
    
    @Override
    public Void getKey(PhotoBean entity) {
        return null;
    }

    @Override
    public boolean hasKey(PhotoBean entity) {
        // TODO
        return false;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
