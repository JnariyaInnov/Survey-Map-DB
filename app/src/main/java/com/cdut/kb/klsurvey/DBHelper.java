package com.cdut.kb.klsurvey;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by Ren on 2016/8/20.
 */
public class DBHelper extends SQLiteOpenHelper {

    private Context mContext;

    private static String create_table_surveyInfo="create table surveyInfo(" +
            "id integer primary key autoincrement,"+
            "BSM text,"+
            "township text," +
            "village text," +
            "principalOfVillage text," +
            "tel text," +
            "number text," +
            "JBNTTBH text," +
            "ZM text," +
            "XDM text," +
            "CBR text," +
            "MJ real," +
            "GDLX text," +
            "remarks text," +
            "DCR text," +
            "ZFZR text," +
            "JLR text)";

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(create_table_surveyInfo); //在创建数据库的同时创建数据表
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(mContext, "创建数据表失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
