package com.cdut.kb.klsurvey;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.esri.android.map.FeatureLayer;
import com.esri.core.geodatabase.ShapefileFeature;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.FeatureResult;
import com.esri.core.tasks.query.QueryParameters;


import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by Ren on 2016/8/21.
 */
public class LeftListViewMethods { //侧边栏中ListView子项的响应事件

    public static void searchParcel(){ //查询图斑
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.mainActivity);
        builder.setTitle("查询图斑"); //设置标题
        builder.setIcon(android.R.drawable.ic_menu_info_details);//设置标题栏上的图标
        final EditText editText_in_alert_dialog=new EditText(MainActivity.mainActivity);
        editText_in_alert_dialog.setHint("请输入要查询图斑的标识码");
        builder.setView(editText_in_alert_dialog);//设置AlertDialog内部的自定义视图

        builder.setPositiveButton("查询", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String target_BSM=editText_in_alert_dialog.getText().toString().trim(); //要查询图斑的标识码
                QueryParameters queryParameters=new QueryParameters();//适用于离线/在线查询
                queryParameters.setInSpatialReference(SpatialReference.create(102100));//设置输入几何体的空间参考
                queryParameters.setReturnGeometry(true);//设置结果集是否应该包括与结果相关的几何体
                queryParameters.setWhere("BSM="+target_BSM); //按照标识码查询

                for(int i=0;i<MainActivity.shpFeatureLayerList.size();i++){
                    MainActivity.shpFeatureLayerList.get(i).selectFeatures(queryParameters, FeatureLayer.SelectionMode.NEW, new CallbackListener<FeatureResult>(){

                        @Override
                        public void onCallback(FeatureResult objects) {
                            if(objects.featureCount()>0){ //选中了要素（结果集中要素的个数>0）
                                Iterator iterator=objects.iterator(); //得到结果集的迭代器
                                if(iterator.hasNext()){
                                    Object selectedObject= iterator.next();
                                    if(selectedObject instanceof ShapefileFeature){ //如果选中的要素是shape图层上的要素
                                        ShapefileFeature selectedFeature= (ShapefileFeature) selectedObject;
                                        Geometry geometry=selectedFeature.getGeometry(); //得到要素对应得几何体
                                        if(geometry instanceof Polygon){
                                            MultiPath multiPath= (MultiPath) geometry;
                                            Envelope envelope=new Envelope();
                                            multiPath.queryEnvelope(envelope); //MultiPath下的queryEnvelope函数可以得到Envelope范围；Polygon下的queryEnvelope无效，不知道为什么
                                            MainActivity.map.centerAt(envelope.getCenter(),true);
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }
                    });
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setCancelable(true);//可以按back键取消
        builder.show(); //创建完AlertDialog之后立马显示出来

    }

    /**
     * 将属性表导出为Excel
     * @param sd 用于操作数据库的SQLiteDatabase
     * @param tableName 要导出数据表的表明
     * @param excelPath 导出得到的Excel的完整路径
     */
    public static void exportTableAsExcel(SQLiteDatabase sd,String tableName,String excelPath){
        
        try {
            HSSFWorkbook workbook=new HSSFWorkbook(); //创建Excel工作薄
            HSSFSheet sheet=workbook.createSheet("导出结果"); //在工作薄中创建工作表

            //样式(经测试，把样式设置在行上无效，但每个单元格依次设置则有效，不知道为什么？)
            HSSFCellStyle alignStyle=workbook.createCellStyle();
            alignStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER); //水平居中
            alignStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER); //垂直居中

            HSSFRow sheetHeader=sheet.createRow(0); //第一行为表头

            HSSFCell cellA1=sheetHeader.createCell(0, Cell.CELL_TYPE_STRING);
            cellA1.setCellValue("序号");
            cellA1.setCellStyle(alignStyle);

            HSSFCell cellA2=sheetHeader.createCell(1,Cell.CELL_TYPE_STRING);
            cellA2.setCellValue("标识码");
            cellA2.setCellStyle(alignStyle);

            HSSFCell cellA3=sheetHeader.createCell(2,Cell.CELL_TYPE_STRING);
            cellA3.setCellValue("字段1");
            cellA3.setCellStyle(alignStyle);

            HSSFCell cellA4=sheetHeader.createCell(3,Cell.CELL_TYPE_STRING);
            cellA4.setCellValue("字段2");
            cellA4.setCellStyle(alignStyle);

            HSSFCell cellA5=sheetHeader.createCell(4,Cell.CELL_TYPE_STRING);
            cellA5.setCellValue("字段3");
            cellA5.setCellStyle(alignStyle);


            String sql="select * from "+tableName; //查询SQLiteDatabase中某一数据表的所有记录
            Cursor cursor=sd.rawQuery(sql,null); //Cursor中包含了查询的结果集
            int rowNum=1; //从第2行开始为内容部分
            if(cursor.moveToFirst()){
                do{
                    HSSFRow row=sheet.createRow(rowNum++);

                    HSSFCell cell=row.createCell(0,Cell.CELL_TYPE_NUMERIC);
                    cell.setCellValue(cursor.getInt(cursor.getColumnIndex("id")));
                    cell.setCellStyle(alignStyle);

                    cell=row.createCell(1,Cell.CELL_TYPE_STRING);
                    cell.setCellValue(cursor.getString(cursor.getColumnIndex("BSM")));
                    cell.setCellStyle(alignStyle);

                    cell=row.createCell(2,Cell.CELL_TYPE_STRING);
                    cell.setCellValue(cursor.getString(cursor.getColumnIndex("field1")));
                    cell.setCellStyle(alignStyle);

                    cell=row.createCell(3,Cell.CELL_TYPE_STRING);
                    cell.setCellValue(cursor.getString(cursor.getColumnIndex("field2")));
                    cell.setCellStyle(alignStyle);

                    cell=row.createCell(4,Cell.CELL_TYPE_STRING);
                    cell.setCellValue(cursor.getString(cursor.getColumnIndex("field3")));
                    cell.setCellStyle(alignStyle);

                }while(cursor.moveToNext());
            }


            FileOutputStream outputStream=new FileOutputStream(excelPath);

            workbook.write(outputStream);
            outputStream.close();
            cursor.close();//关闭游标，释放其资源
            Toast.makeText(MainActivity.mainActivity, "导出数据表成功", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.mainActivity,"导出数据表失败",Toast.LENGTH_SHORT).show();
        }

    }


    public static void author_info(){ //显示作者信息
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.mainActivity);
        builder.setTitle("作者信息"); //作者信息
        builder.setIcon(android.R.drawable.ic_menu_info_details);//设置标题栏上的图标
        builder.setMessage("设计者：任飞翔\nEmail：ren_feixiang@126.com");
        builder.setPositiveButton("我知道了",null);
        builder.show();
    }
}
