package com.cdut.kb.klsurvey;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.util.Date;

public class SurveyInfoInputDialogFragment extends DialogFragment { //推荐使用DialogFragment作弹出窗口

    private View mView;//当前对话框布局的View

    private int BSM; //被点击图斑的标识码

    //启动系统相机活动的请求码
    private final int CRAEMA_REQUEST_CODE=0;

    private String defaultPhotoPath=""; //拍摄得到的默认照片的全路径

    private String finalPhotoDir=""; //最终存放照片的文件夹

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) { //重写onCreateView方法，加载碎片的布局
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);//去除掉DialogFragment默认的标题栏
        View view=inflater.inflate(R.layout.fragment_survey_info_input_dialog,container,false);

        mView=view; //保存下当前对话框的View

        ((EditText)view.findViewById(R.id.BSM_value)).setText(String.valueOf(BSM)); //将传进来的标识码设置到对话框中，并且该对话框不可编辑

        if(MainActivity.dbManager.isRecordExists("surveyInfo","BSM",BSM)){ //surveyInfo表中存在该图斑的记录，则回显
            reDisplayExistRecordInTableSurveyInfo("BSM",BSM);
        }

        //SurveyInfoInputDialogFragment中调用系统相机进行拍照的响应函数
        Button btn_takePhoto= (Button) view.findViewById(R.id.takePhoto); //"拍照"按钮
        btn_takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePhotosIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //打开系统自带的相机
                takePhotosIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(defaultPhotoPath)));//将拍摄的照片存到默认的文件里
                startActivityForResult(takePhotosIntent,CRAEMA_REQUEST_CODE);
            }
        });

        //点击“确定”按钮将数据插入到数据库中
        Button btn_inputOK= (Button) view.findViewById(R.id.inputOK);
        btn_inputOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!(MainActivity.dbManager.isRecordExists("surveyInfo","BSM",BSM))){ //surveyInfo表中不存在满足条件的记录，则插入
                    insertRowToTableSurveyInfo(); //将输入的属性数据插入到surveyInfo表中
                }else{ //存在满足条件的记录，则进行更新
                    updateTableSurveyInfo();
                }
            }
        });

        //点击“取消”按钮则直接销毁掉对话框
        Button btn_inputCancel= (Button) view.findViewById(R.id.inputCancel);
        btn_inputCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SurveyInfoInputDialogFragment.this.dismiss();
            }
        });
        return view;
    }

    /**
     *  将输入的属性数据插入到surveyInfo表中
     */
    private void insertRowToTableSurveyInfo() {
        ContentValues contentValues=new ContentValues();

        String BSM=((EditText)mView.findViewById(R.id.BSM_value)).getText().toString().trim();//标识码
        contentValues.put("BSM",BSM);

        String field1=((EditText)mView.findViewById(R.id.field1_value)).getText().toString().trim();//字段1
        contentValues.put("field1",field1);

        RadioGroup field2_radio_group= (RadioGroup) mView.findViewById(R.id.field2_value);
        int checkedRadioButtonId=field2_radio_group.getCheckedRadioButtonId();
        RadioButton field2_checked_radio_btn= (RadioButton) mView.findViewById(checkedRadioButtonId);
        String field2=field2_checked_radio_btn.getText().toString();
        contentValues.put("field2",field2);

        Spinner field3_spinner= (Spinner) mView.findViewById(R.id.field3_value);
        String field3=field3_spinner.getSelectedItem().toString();//得到Spinner中被选项的数据
        contentValues.put("field3",field3);


        if((MainActivity.dbManager.insert(contentValues,"surveyInfo"))!=-1){ //如果添加数据成功就销毁这个对话框
            SurveyInfoInputDialogFragment.this.dismiss();
        }
    }

    /**
     * 更新surveyInfo表中的记录
     */
    private void updateTableSurveyInfo(){
        ContentValues contentValues=new ContentValues();

        String field1=((EditText)mView.findViewById(R.id.field1_value)).getText().toString().trim();//字段1
        contentValues.put("field1",field1);

        RadioGroup field2_radio_group= (RadioGroup) mView.findViewById(R.id.field2_value);
        int checkedRadioButtonId=field2_radio_group.getCheckedRadioButtonId();
        RadioButton field2_checked_radio_btn= (RadioButton) mView.findViewById(checkedRadioButtonId);
        String field2=field2_checked_radio_btn.getText().toString();
        contentValues.put("field2",field2);

        Spinner field3_spinner= (Spinner) mView.findViewById(R.id.field3_value);
        String field3=field3_spinner.getSelectedItem().toString();//得到Spinner中被选项的数据
        contentValues.put("field3",field3);

        if(MainActivity.dbManager.update("surveyInfo","BSM",BSM,contentValues)>0){
            if(MainActivity.currentLanguageEnvironment.endsWith("zh")){
                Toast.makeText(MainActivity.mainActivity, "更新数据成功", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(MainActivity.mainActivity, "Update Data Success", Toast.LENGTH_SHORT).show();
            }
            SurveyInfoInputDialogFragment.this.dismiss();
        }else{
            if(MainActivity.currentLanguageEnvironment.endsWith("zh")){
                Toast.makeText(MainActivity.mainActivity, "更新数据失败", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(MainActivity.mainActivity, "Update Data Failed", Toast.LENGTH_SHORT).show();
            }
        }

    }

    /**
     * 如果图斑记录以及存在于surveyInfo表中，则回显在对话框中
     * @param fieldName 字段名
     * @param obj_fieldValue 字段值
     */
    private void reDisplayExistRecordInTableSurveyInfo(String fieldName,Object obj_fieldValue){
        String fieldValue=String.valueOf(obj_fieldValue);
        String sql="select * from surveyInfo where "+fieldName+"=?";
        Cursor cursor=MainActivity.sd.rawQuery(sql,new String[]{fieldValue});
        //此处回显的数据肯定是唯一的
        if(cursor.moveToFirst()){

            ((EditText)mView.findViewById(R.id.field1_value)).setText(cursor.getString(cursor.getColumnIndex("field1")));

            RadioGroup field2_radio_group= (RadioGroup) mView.findViewById(R.id.field2_value);
            String field2=cursor.getString(cursor.getColumnIndex("field2"));

            if("值1".equals(field2) || "value1".equals(field2)){
                field2_radio_group.check(R.id.field2_v1);
            }else if("值2".equals(field2) || "value2".equals(field2)){
                field2_radio_group.check(R.id.field2_v2);
            }

            Spinner field3_spinner= (Spinner) mView.findViewById(R.id.field3_value);
            String field3=cursor.getString(cursor.getColumnIndex("field3"));

            if("值1".equals(field3) || "value1".equals(field3)){
                field3_spinner.setSelection(0);
            }else if("值2".equals(field3) || "value2".equals(field3)){
                field3_spinner.setSelection(1);
            }else if("值3".equals(field3) || "value3".equals(field3)){
                field3_spinner.setSelection(2);
            }
        }
        cursor.close();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode== Activity.RESULT_OK){ //这里指拍照成功
            switch (requestCode){
                case CRAEMA_REQUEST_CODE: //处理拍照活动销毁后返回的事件
                    String finalSubDirPath=finalPhotoDir+File.separator+String.valueOf(BSM);//每一个图斑一个文件夹，存放属于自己的照片。以标识码为名称新建一个子文件夹
                    File finalSubDir=new File(finalSubDirPath);
                    if(!finalSubDir.exists() || !finalSubDir.isDirectory()){ //某个图斑的文件夹不存在则创建这个文件夹
                        finalSubDir.mkdirs(); //创建图斑对应得子文件夹
                    }
                    String photoName=String.valueOf(BSM)+"_"+String.valueOf((new Date()).getTime())+".jpg";//照片的名称，保证多张照片不重名
                    String finalPhotoPath=finalSubDirPath+File.separator+photoName;//最终图片的全路径
                    CommonUtil.copyFile(defaultPhotoPath,finalPhotoPath);//拷贝到指定文件夹下
                    new File(defaultPhotoPath).delete();//删除拍照得到的临时文件
                    break;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        defaultPhotoPath=CommonUtil.getSdCardPath()+ File.separator+"default.jpg";//默认拍摄的照片的路径
        finalPhotoDir=CommonUtil.getSdCardPath()+File.separator+"KLSurveyData"+File.separator+"photos"; //最终存放照片的文件夹
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //这里我们为了解决输入法遮挡输入框的情况，不管大屏幕还是小屏幕输入框都全屏显示，在这种状态下输入法不会遮挡对话框，对话框可以自由滑动
        setStyle(DialogFragment.STYLE_NO_FRAME,android.R.style.Theme_Holo_Light);//不失为一种让DialogFragment全屏显示的好方法

        //接收传过来的Bundle参数
        if(getArguments()!=null){
            BSM=getArguments().getInt("BSM");//得到传到DialogFragment里的BSM
        }
    }

}
