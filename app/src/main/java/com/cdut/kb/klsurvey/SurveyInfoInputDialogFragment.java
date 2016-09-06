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

        String township=((EditText)mView.findViewById(R.id.township_value)).getText().toString().trim();//乡镇
        contentValues.put("township",township);

        String village=((EditText)mView.findViewById(R.id.village_value)).getText().toString().trim();//村
        contentValues.put("village",village);

        String principalOfVillage=((EditText)mView.findViewById(R.id.principalOfVillage_value)).getText().toString().trim();//村负责人
        contentValues.put("principalOfVillage",principalOfVillage);

        String tel=((EditText)mView.findViewById(R.id.tel_value)).getText().toString().trim();//联系电话
        contentValues.put("tel",tel);

        String number=((EditText)mView.findViewById(R.id.number_value)).getText().toString().trim();//编号
        contentValues.put("number",number);

        EditText JBNTTBH_edit=(EditText)mView.findViewById(R.id.JBNTTBH_value);
        String JBNTTBH=JBNTTBH_edit.getText().toString().trim();//基本农田图斑号
        if("".equals(JBNTTBH)){
            Toast.makeText(MainActivity.mainActivity, "\"基本农田图斑号\"为必填字段", Toast.LENGTH_SHORT).show();
            CommonUtil.editTextRegainFocus(JBNTTBH_edit);
            return;
        }
        contentValues.put("JBNTTBH",JBNTTBH);

        String ZM=((EditText)mView.findViewById(R.id.ZM_value)).getText().toString().trim();//组名
        contentValues.put("ZM",ZM);

        String XDM=((EditText)mView.findViewById(R.id.XDM_value)).getText().toString().trim();//小地名
        contentValues.put("XDM",XDM);

        EditText CBR_edit=(EditText)mView.findViewById(R.id.CBR_value);
        String CBR=CBR_edit.getText().toString().trim();//承包人
        if("".equals(CBR)){
            Toast.makeText(MainActivity.mainActivity, "\"承包人\"为必填字段", Toast.LENGTH_SHORT).show();
            CommonUtil.editTextRegainFocus(CBR_edit);
            return;
        }
        contentValues.put("CBR",CBR);

        float MJ;
        try {
            String str_MJ=((EditText)mView.findViewById(R.id.MJ_value)).getText().toString().trim();
            if("".equals(str_MJ)){
                MJ=-1; //没填面积就让面积默认为-1
            }else
            {
                MJ=Float.parseFloat(str_MJ);//面积
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.mainActivity, "请确认面积是否填写合理", Toast.LENGTH_SHORT).show();
            return;
        }
        contentValues.put("MJ",MJ);

        String GDLX=((EditText)mView.findViewById(R.id.GDLX_value)).getText().toString().trim();//耕地类型
        contentValues.put("GDLX",GDLX);

        String remarks=((EditText)mView.findViewById(R.id.remarks_value)).getText().toString().trim();//备注
        contentValues.put("remarks",remarks);

        String DCR=((EditText)mView.findViewById(R.id.DCR_value)).getText().toString().trim();//调查人
        contentValues.put("DCR",DCR);

        String ZFZR=((EditText)mView.findViewById(R.id.ZFZR_value)).getText().toString().trim();//组负责人
        contentValues.put("ZFZR",ZFZR);

        String JLR=((EditText)mView.findViewById(R.id.JLR_value)).getText().toString().trim();//记录人
        contentValues.put("JLR",JLR);

        if((MainActivity.dbManager.insert(contentValues,"surveyInfo"))!=-1){ //如果添加数据成功就销毁这个对话框
            SurveyInfoInputDialogFragment.this.dismiss();
        }
    }

    /**
     * 更新surveyInfo表中的记录
     */
    private void updateTableSurveyInfo(){
        ContentValues contentValues=new ContentValues();

        String township=((EditText)mView.findViewById(R.id.township_value)).getText().toString().trim();//乡镇
        contentValues.put("township",township);

        String village=((EditText)mView.findViewById(R.id.village_value)).getText().toString().trim();//村
        contentValues.put("village",village);

        String principalOfVillage=((EditText)mView.findViewById(R.id.principalOfVillage_value)).getText().toString().trim();//村负责人
        contentValues.put("principalOfVillage",principalOfVillage);

        String tel=((EditText)mView.findViewById(R.id.tel_value)).getText().toString().trim();//联系电话
        contentValues.put("tel",tel);

        String number=((EditText)mView.findViewById(R.id.number_value)).getText().toString().trim();//编号
        contentValues.put("number",number);

        EditText JBNTTBH_edit=(EditText)mView.findViewById(R.id.JBNTTBH_value);
        String JBNTTBH=JBNTTBH_edit.getText().toString().trim();//基本农田图斑号
        if("".equals(JBNTTBH)){
            Toast.makeText(MainActivity.mainActivity, "\"基本农田图斑号\"为必填字段", Toast.LENGTH_SHORT).show();
            CommonUtil.editTextRegainFocus(JBNTTBH_edit);
            return;
        }
        contentValues.put("JBNTTBH",JBNTTBH);

        String ZM=((EditText)mView.findViewById(R.id.ZM_value)).getText().toString().trim();//组名
        contentValues.put("ZM",ZM);

        String XDM=((EditText)mView.findViewById(R.id.XDM_value)).getText().toString().trim();//小地名
        contentValues.put("XDM",XDM);

        EditText CBR_edit=(EditText)mView.findViewById(R.id.CBR_value);
        String CBR=CBR_edit.getText().toString().trim();//承包人
        if("".equals(CBR)){
            Toast.makeText(MainActivity.mainActivity, "\"承包人\"为必填字段", Toast.LENGTH_SHORT).show();
            CommonUtil.editTextRegainFocus(CBR_edit);
            return;
        }
        contentValues.put("CBR",CBR);

        float MJ;
        try {
            String str_MJ=((EditText)mView.findViewById(R.id.MJ_value)).getText().toString().trim();
            if("".equals(str_MJ)){
                MJ=-1; //没填面积就让面积默认为-1
            }else
            {
                MJ=Float.parseFloat(str_MJ);//面积
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.mainActivity, "请确认面积是否填写合理", Toast.LENGTH_SHORT).show();
            return;
        }
        contentValues.put("MJ",MJ);

        String GDLX=((EditText)mView.findViewById(R.id.GDLX_value)).getText().toString().trim();//耕地类型
        contentValues.put("GDLX",GDLX);

        String remarks=((EditText)mView.findViewById(R.id.remarks_value)).getText().toString().trim();//备注
        contentValues.put("remarks",remarks);

        String DCR=((EditText)mView.findViewById(R.id.DCR_value)).getText().toString().trim();//调查人
        contentValues.put("DCR",DCR);

        String ZFZR=((EditText)mView.findViewById(R.id.ZFZR_value)).getText().toString().trim();//组负责人
        contentValues.put("ZFZR",ZFZR);

        String JLR=((EditText)mView.findViewById(R.id.JLR_value)).getText().toString().trim();//记录人
        contentValues.put("JLR",JLR);

        if(MainActivity.dbManager.update("surveyInfo","BSM",BSM,contentValues)>0){
            Toast.makeText(MainActivity.mainActivity, "更新数据成功", Toast.LENGTH_SHORT).show();
            SurveyInfoInputDialogFragment.this.dismiss();
        }else{
            Toast.makeText(MainActivity.mainActivity, "更新数据失败", Toast.LENGTH_SHORT).show();
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
            ((EditText)mView.findViewById(R.id.township_value)).setText(cursor.getString(cursor.getColumnIndex("township")));
            ((EditText)mView.findViewById(R.id.village_value)).setText(cursor.getString(cursor.getColumnIndex("village")));
            ((EditText)mView.findViewById(R.id.principalOfVillage_value)).setText(cursor.getString(cursor.getColumnIndex("principalOfVillage")));
            ((EditText)mView.findViewById(R.id.tel_value)).setText(cursor.getString(cursor.getColumnIndex("tel")));
            ((EditText)mView.findViewById(R.id.number_value)).setText(cursor.getString(cursor.getColumnIndex("number")));
            ((EditText)mView.findViewById(R.id.JBNTTBH_value)).setText(cursor.getString(cursor.getColumnIndex("JBNTTBH")));
            ((EditText)mView.findViewById(R.id.ZM_value)).setText(cursor.getString(cursor.getColumnIndex("ZM")));
            ((EditText)mView.findViewById(R.id.XDM_value)).setText(cursor.getString(cursor.getColumnIndex("XDM")));
            ((EditText)mView.findViewById(R.id.CBR_value)).setText(cursor.getString(cursor.getColumnIndex("CBR")));
            ((EditText)mView.findViewById(R.id.MJ_value)).setText(String.valueOf(cursor.getFloat(cursor.getColumnIndex("MJ"))));
            ((EditText)mView.findViewById(R.id.GDLX_value)).setText(cursor.getString(cursor.getColumnIndex("GDLX")));
            ((EditText)mView.findViewById(R.id.remarks_value)).setText(cursor.getString(cursor.getColumnIndex("remarks")));
            ((EditText)mView.findViewById(R.id.DCR_value)).setText(cursor.getString(cursor.getColumnIndex("DCR")));
            ((EditText)mView.findViewById(R.id.ZFZR_value)).setText(cursor.getString(cursor.getColumnIndex("ZFZR")));
            ((EditText)mView.findViewById(R.id.JLR_value)).setText(cursor.getString(cursor.getColumnIndex("JLR")));
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
