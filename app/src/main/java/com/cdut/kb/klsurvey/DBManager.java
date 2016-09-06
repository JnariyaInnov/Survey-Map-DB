package com.cdut.kb.klsurvey;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Ren on 2016/8/20.
 */
public class DBManager { //数据库工具类

    private Context mContext;

    /*public static String sql_insert_surveyInfo="insert into surveyInfo"+
            "(BSM,township,village,principalOfVillage,tel," +
            "number,JBNTTBH,ZM,XDM,CBR,MJ,GDLX,remarks,DCR,ZFZR,JLR) " +
            "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";*/

    private SQLiteDatabase sd; //构造函数中传进来一个SQLiteDatabase对象，用于进行CRUD操作

    public DBManager(Context context,SQLiteDatabase sd) {
        mContext=context;
        this.sd = sd;
    }

    /**
     * 插入一行记录到属性表中
     * @param contentValues 存放插入数据的键值对集合
     * @param tableName 目标数据表
     * @return 返回-1表示插入失败；否则插入成功
     */
    public long insert(ContentValues contentValues,String tableName){ //插入数据

        long result=-1;
        if((result=sd.insert(tableName,null,contentValues))==-1){
            Toast.makeText(mContext, "增加数据失败", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(mContext, "增加数据成功", Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    /**
     *  更新数据表中的记录
     * @param tableName 待更新的数据表
     * @param target_fieldName 目标字段名
     * @param obj_target_fieldValue 目标字段值
     * @param contentValues 存放着要更新的部分的内容
     * @return 返回受影响的行数
     */
    public int update(String tableName,String target_fieldName,Object obj_target_fieldValue,ContentValues contentValues){
        String target_fieldValue=String.valueOf(obj_target_fieldValue);
        return sd.update(tableName,contentValues,target_fieldName+"=?",new String[]{target_fieldValue});
    }

    /**
     * 判断指定表中是否有满足条件的记录
     * @param tableName 表名
     * @param fieldName 字段名
     * @param obj_fieldValue 字段值
     * @return 存在满足条件的记录返回true;不存在则返回false。
     */
    public boolean isRecordExists(String tableName,String fieldName,Object obj_fieldValue){
        String fieldValue=String.valueOf(obj_fieldValue);

        String querySql="select * from "+tableName+" where "+fieldName+"=?";

        Cursor cursor=sd.rawQuery(querySql,new String[]{fieldValue});
        if(cursor.getCount()>0){
            return true; //记录存在，返回true
        }
        return false;
    }

}
