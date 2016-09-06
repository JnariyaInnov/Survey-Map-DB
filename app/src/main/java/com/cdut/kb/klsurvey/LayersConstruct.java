package com.cdut.kb.klsurvey;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.runtime.ArcGISRuntime;
import com.esri.core.geodatabase.ShapefileFeature;
import com.esri.core.geodatabase.ShapefileFeatureTable;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.FeatureResult;
import com.esri.core.map.Graphic;
import com.esri.core.renderer.Renderer;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.FillSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.tasks.SpatialRelationship;
import com.esri.core.tasks.query.QueryParameters;

import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Ren on 2016/8/5.
 */
public class LayersConstruct {

    /**
     * 初始化底图
     */
    public void initBaseMap() {

        //基础版授权，去除水印（基础版免费）
        ArcGISRuntime.setClientId("iemhtr6HIJoxtvxu");

        //底图采用tpk方式
        List<String> tpkFileNames=CommonUtil.fileFilter(CommonUtil.getSdCardPath() + "/KLSurveyData/tpk",".tpk");//得到文件夹下所有tpk文件的名称.eg:XXX.tpk

        Envelope mapEnvelope=new Envelope();//创建一个空的Envelope对象，用来表示MapView的范围
        for(int i=0;i<tpkFileNames.size();i++){

            ArcGISLocalTiledLayer arcGISLocalTiledLayer=new ArcGISLocalTiledLayer(CommonUtil.getSdCardPath() + "/KLSurveyData/tpk/"+tpkFileNames.get(i));
            Envelope envelopeOfPerLayer=arcGISLocalTiledLayer.getFullExtent();
            mapEnvelope.merge(envelopeOfPerLayer); //将每一个图层的范围取并集
            MainActivity.arcGISLocalTiledLayerList.add(arcGISLocalTiledLayer);
            MainActivity.map.addLayer(arcGISLocalTiledLayer);
        }
        MainActivity.map.setMaxExtent(mapEnvelope);//将最后得到的所有图层范围的并集设置为MapView的范围

        //MainActivity.map.setMaxExtent(MainActivity.arcGISLocalTiledLayer.getFullExtent());//这句话好像不是必须的

    }


    /*
    特别注意：制作tpk底图的时候，先将原始tiff文件及其所属的数据框的坐标系转换为WGS_1984_Web_Mercator_Auxiliary_Sphere,
    再转换为tpk文件；添加到tpk底图上的shape图层的坐标系也应该设为WGS_1984_Web_Mercator_Auxiliary_Sphere
     */
    public void initShpLayer(){ //初始化展示shape文件的图层

        List<String> shpFileNames=CommonUtil.fileFilter(CommonUtil.getSdCardPath()+"/KLSurveyData/shp",".shp");
        for(String shpFileName:shpFileNames){ //遍历所有shp文件的名称
            String shpPath=CommonUtil.getSdCardPath()+"/KLSurveyData/shp/"+shpFileName; //shp文件的全路径
            ShapefileFeatureTable shapefileFeatureTable=null; //ShapefileFeatureTable用于读取shp文件，并通过它将shp文件添加到FeatureLayer上
            //通过ShapefileFeatureTable可以加载shp文件并显示在FeatureLayer上
            try{
                shapefileFeatureTable=new ShapefileFeatureTable(shpPath);
            }catch (FileNotFoundException e){
                e.printStackTrace();
                Log.d("shp","读取shape文件失败");
            }catch(Exception e2){
                e2.printStackTrace();
                Log.d("shp","读取shape文件异常");
            }

            FeatureLayer shpFeatureLayer=new FeatureLayer(shapefileFeatureTable); //依次创建每一个shp文件对应得FeatureLayer
            //填充面的样式
            FillSymbol symbol=new SimpleFillSymbol(Color.RED,SimpleFillSymbol.STYLE.NULL);//面内部填充设为空心
            //面的边线样式
            SimpleLineSymbol outLineSymbol=new SimpleLineSymbol(Color.RED,2,SimpleLineSymbol.STYLE.SOLID);

            symbol.setOutline(outLineSymbol);//通过FillSymbol的setOutline方法设置面的边线

            //通过Renderer加载符号系统，从而将符号系统设置到图层上
            Renderer renderer=new SimpleRenderer(symbol);
            shpFeatureLayer.setRenderer(renderer);

            //将shp文件对应得图层添加到图层集合中保存下来，并添加到MapView上进行显示
            MainActivity.shpFeatureLayerList.add(shpFeatureLayer);
            MainActivity.map.addLayer(shpFeatureLayer);
        }

        //加载完shape文件后设置MapView上的单击事件
        MainActivity.map.setOnSingleTapListener(new OnSingleTapListener() {
            @Override
            public void onSingleTap(float x, float y) { //x,y为屏幕坐标
                Point geoPt=MainActivity.map.toMapPoint(x,y);//将屏幕坐标转换为MapView坐标系下的地理坐标
                QueryParameters queryParameters=new QueryParameters();//QueryParameters可以用于离线或在线的查询
                queryParameters.setInSpatialReference(SpatialReference.create(102100));//设置输入几何体的空间参考
                queryParameters.setReturnGeometry(true);//设置结果集是否应该包括与结果相关的几何体
                queryParameters.setSpatialRelationship(SpatialRelationship.INTERSECTS);//设置输入几何体与要查询结果的空间关系
                queryParameters.setGeometry(geoPt);//设置在查询中作为空间过滤器的几何体

                for(int i=0;i<MainActivity.shpFeatureLayerList.size();i++){ //依次遍历shp文件对应得每一FeatureLayer，看看被点击的要素到底来自哪个图层
                    //基于查询在图层中选择要素
                    MainActivity.shpFeatureLayerList.get(i).selectFeatures(queryParameters, FeatureLayer.SelectionMode.NEW, new CallbackListener<FeatureResult>() {
                        @Override
                        public void onCallback(FeatureResult objects) { //选择要素后的回调函数。这里的参数表示选中的要素集
                            //FeatureResult中封装了一系列要素以及它们的元数据
                            if(objects.featureCount()>0){ //选中了要素（结果集中要素的个数>0）
                                Iterator iterator=objects.iterator(); //得到结果集的迭代器
                                if(iterator.hasNext()){
                                    Object selectedObject= iterator.next();
                                    if(selectedObject instanceof ShapefileFeature){ //如果选中的要素是shape图层上的要素
                                        ShapefileFeature selectedFeature= (ShapefileFeature) selectedObject;
                                        int BSM= (int) selectedFeature.getAttributeValue("BSM");
                                        // ...
                                        showSurveyInfoInputDialog(BSM);
                                    /*
                                    Log.d("val",String.valueOf(selectedFeature.getAttributeValue("LocName")));
                                    try{
                                        Thread.sleep(5000);//测试：休眠5秒后清除选中
                                    }catch(InterruptedException e){
                                        e.printStackTrace();
                                    }
                                    MainActivity.shpFeatureLayer.clearSelection();//清除选中
                                    */

                                    }
                                }
                            }
                        }
                        @Override
                        public void onError(Throwable throwable) {

                        }
                    });
                }
                //注意：用ArcGIS Desktop 10.1或更早版本生成的shp文件缺少.cpg文件，用arcgis android读取属性会出现
                //中文乱码的现象。因此，这里统一用10.2或更高版本
            }
        });

    }


    /**
     * 动态更新位置信息
     */
    public void dynamicUpdateLocationInfo(){ //不够完善

        MainActivity.locationMarkerLayer=new GraphicsLayer(SpatialReference.create(102100),MainActivity.map.getMaxExtent());//初始化GraphicsLayer对象，用于存放动态更新的位置标注
        MainActivity.map.addLayer(MainActivity.locationMarkerLayer);//添加到MapView中(后添加的图层在上层)

        //初始化LocationManager对象
        LocationManager locationManager=(LocationManager)MainActivity.mainActivity.getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){ //如果GPS没有正常启动
            Toast.makeText(MainActivity.mainActivity, "启动定位模块失败，请检查GPS是否正常启动", Toast.LENGTH_LONG).show();
            /*//返回开启GPS导航设置界面
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            //startActivityForResult(intent,1);
            startActivity(intent);*/
            return;
        }

        //------ android 6.0开始授权机制发生了改变。需要权限的地方每次运行时都要检查是否已经授权。（此处不完善）
        if(ContextCompat.checkSelfPermission(MainActivity.mainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){ //没有授权的话进行授权
            ActivityCompat.requestPermissions(MainActivity.mainActivity,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}
                    ,1);
        }
        //------

        //注册位置更新
        //这里使用GPS进行定位
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {/*LocationManager.GPS_PROVIDER*//*LocationManager.NETWORK_PROVIDER*/
            @Override
            public void onLocationChanged(Location location) { //位置发生改变时触发
                double longitude=location.getLongitude(); //改变后位置的经度
                double latitude=location.getLatitude(); //改变后位置的纬度
                Point originPt=new Point(longitude,latitude);//WSG84下的位置
                Log.d("location","转换前坐标:"+originPt.getX()+","+originPt.getY());
                SpatialReference spatialReference4326=SpatialReference.create(4326);//创建一个WGS84空间参考实例
                //创建Web Mercator坐标系空间参考实例
                SpatialReference spatialReference3857=SpatialReference.create(102100);
                //将WGS84下的位置转换到Web Mercator坐标系下
                Point finalPt= (Point) GeometryEngine.project(originPt,spatialReference4326,spatialReference3857);
                Log.d("location","转换后坐标:"+finalPt.getX()+","+finalPt.getY());
                MainActivity.map.centerAt(finalPt,true);//以定位到的位置为地图中心

                //添加位置标注
                MainActivity.locationMarkerLayer.removeAll();
                //标注点的样式
                SimpleMarkerSymbol locationMarkerSymbol=new SimpleMarkerSymbol(Color.YELLOW,10,SimpleMarkerSymbol.STYLE.CIRCLE);
                Graphic locationMarker=new Graphic(finalPt,locationMarkerSymbol);//构造位置标注
                MainActivity.locationMarkerLayer.addGraphic(locationMarker);

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        });
    }

    private void showSurveyInfoInputDialog(int BSM){ //点击shape上的图斑后，显示调查信息输入对话框
        //用Activity做伪弹窗有一点不好的地方，就是宽高不好控制，有时必须写死。这里用DialogFragment做弹窗，也是官方推荐的。
        SurveyInfoInputDialogFragment surveyInfoInputDialogFragment=new SurveyInfoInputDialogFragment();//创建DialogFragment对话框实例对象
        android.app.FragmentManager fragmentManager=MainActivity.mainActivity.getFragmentManager();

        //这里Bundle对象用于存放用于传递给DialogFragment的初始参数
        Bundle bundle=new Bundle();
        bundle.putInt("BSM",BSM);
        surveyInfoInputDialogFragment.setArguments(bundle);

        surveyInfoInputDialogFragment.show(fragmentManager,"SurveyInfoInputDialogFragment");//弹出DialogFragment
    }

}
