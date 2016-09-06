package com.cdut.kb.klsurvey;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;
import android.widget.EditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Ren on 2016/8/12.
 */
public class CommonUtil {

    /**
     * 将文件从源路径复制到目标路径
     * @param srcFilePath 源路径
     * @param destFilePath 目标路径
     */
    public static void copyFile(String srcFilePath,String destFilePath){

        try{
            FileInputStream fileInputStream=new FileInputStream(srcFilePath);
            FileOutputStream fileOutputStream=new FileOutputStream(destFilePath);
            byte[] buffer=new byte[1024];
            int length=-1;
            while ((length=fileInputStream.read(buffer))!=-1){
                fileOutputStream.write(buffer,0,length);
            }
            fileInputStream.close();
            fileOutputStream.close();
        }catch (Exception e){
            Log.e("copyError","复制文件出错");
            e.printStackTrace();
        }

    }


    /**
     * 得到sd卡的路径
     */
    public static String getSdCardPath(){
        /*
          这个方法有问题，对有的手机，就算有外置sd卡，返回的路径还是内置sd卡的路径
         @return 有外置sd卡时返回外置sd卡的路径，没有外置sd卡时返回内置sd卡的路径
        //得到sd卡的路径
        String result=""; //用于返回sd卡的路径

        Runtime runtime=null;
        Process process=null;
        InputStream is=null;
        InputStreamReader isReader=null;
        BufferedReader bufferedReader=null;
        try {
            runtime=Runtime.getRuntime();//得到当前应用程序的运行时
            process=runtime.exec("mount");//返回用于管理子进程的Process对象
            is=process.getInputStream();
            isReader=new InputStreamReader(is);
            bufferedReader=new BufferedReader(isReader);
            String line;
            while((line=bufferedReader.readLine())!=null){
                if(line.contains("sdcard") && line.contains(".android_secure")){
                    String [] arr=line.split(" ");
                    if(arr!=null && arr.length>=5){
                        result=arr[1].replace("/.android_secure","");
                        return result;
                    }
                }
            }
            if(process.waitFor()!=0 && process.exitValue()==1){
                // p.exitValue()==0表示正常结束，1：非正常结束
                Log.w("getSDCardPath", "尝试得到sd卡路径出现异常");
            }
        }catch (Exception e){
            Log.e("getSDCardPath", "尝试得到sd卡路径失败");
        }finally {
            //后创建的先关闭
            try{
                if(bufferedReader!=null){
                    bufferedReader.close();
                }
                if(isReader!=null){
                    isReader.close();
                }
                if(is!=null){
                    is.close();
                }
            }catch(IOException e){
                Log.w("getSDCardPath","关闭流资源失败");
            }

        }
        //没有得到外置sd卡的路径，则返回内置sd卡的路径
        return Environment.getExternalStorageDirectory().getPath();*/
        return getStoragePath(MainActivity.mainActivity,true);//得到外置sd卡的路径
    }

    /**
     * 通过反射的方式获取内置/外置sd卡的路径
     * @param mContext  上下文对象
     * @param is_removale false表示不可移除，得到内置sd卡的路径；true表示可以移除，得到外置sd卡的路径
     * @return 内置/外置sd卡的路径（由is_removale决定得到的是内置或外置sd卡的路径）
     */
    private static String getStoragePath(Context mContext, boolean is_removale) {
        /*
        * 遇到的问题：华为mate1在指定默认存储位置为sd卡时，用该函数得到的内/外置sd卡的路径是相反的；指定
        *       默认存储位置为内部存储时则正确。
        *
        *       华为荣耀6不管指定默认存储位置为内部存储还是sd卡得到的内/外置sd卡的路径都是正确的。
        * */

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removale == removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param dirPath 文件夹的全路径
     * @param suffix 返回的文件名具有的后缀
     * @return 返回指定文件夹下所有指定后缀的文件名称
     */
    public static List<String> fileFilter(String dirPath, final String suffix){

        File dir=new File(dirPath); //文件夹对应得File对象
        if(!dir.exists()||!dir.isDirectory()){
            return null;
        }
        String[] fileNamesArr=dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains(suffix);
            }
        });
        List<String> fileNames=Arrays.asList(fileNamesArr); //将字符串数组转换为列表
        return fileNames;
    }

    /**
     * 在EditText未填写内容时，让EditText重新获得焦点
     * @param editText
     */
    public static void editTextRegainFocus(EditText editText){
        editText.setText(""); //去除可能存在的空格
        editText.setFocusable(true); //默认情况下不可以获得焦点（点击除外）
        editText.setFocusableInTouchMode(true); //在触摸模式下可以获得焦点（貌似这句没什么用）
        editText.requestFocus(); //获得焦点
    }

}
