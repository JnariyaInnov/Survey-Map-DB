package com.cdut.kb.klsurvey;

import android.app.Activity;

import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.cdut.kb.library.DrawerArrowDrawable;
import com.esri.android.map.FeatureLayer;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity { //主活动
    //地图容器控件
    public static MapView map=null;
    //tpk图层集合，本系统用tpk文件作为底图
    public static ArrayList<ArcGISLocalTiledLayer> arcGISLocalTiledLayerList=null;
    //shape图层集合
    public static ArrayList<FeatureLayer> shpFeatureLayerList=null;
    //存放动态更新的位置标注的图层
    public static GraphicsLayer locationMarkerLayer=null;
    //本类的Activity对象实例
    public static MainActivity mainActivity=null;
    //数据库相关
    public static DBHelper dbHelper=null; //SQLiteOpenHelper对象
    public static SQLiteDatabase sd=null; //数据库对象
    public static DBManager dbManager=null; //数据库工具类

    // --- DrawerLayout
    public static DrawerLayout drawerLayout=null;
    private DrawerArrowDrawable drawerArrowDrawable=null; //实现抽屉图标/返回箭头之间的旋转切换
    private float offset;
    private  boolean flipped;
    // ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);//这句话在AppCompatActivity下无效，
        //无法去掉标题栏，需要继承自Activity才有效
        setContentView(R.layout.activity_main);

        initDrawerLayout();//初始化DrawerLayout

        arcGISLocalTiledLayerList=new ArrayList<ArcGISLocalTiledLayer>(); //初始化tpk图层集合
        shpFeatureLayerList=new ArrayList<FeatureLayer>();//初始化shp图层集合

        //初始化本类的Activity对象实例
        mainActivity=this;

        //得到MapView对象
        map= (MapView) findViewById(R.id.mapView);

        //创建图层构造对象
        LayersConstruct layersConstruct=new LayersConstruct();

        //初始化底图
        layersConstruct.initBaseMap();

        //初始化shape文件图层
        layersConstruct.initShpLayer();

        //动态更新位置信息
        layersConstruct.dynamicUpdateLocationInfo();

        //初始化数据库
        initDB();
    }

    private void initDB(){ //初始化数据库
        dbHelper=new DBHelper(this,"surveyInfo.db",null,1);//创建SQLiteOpenHelper对象
        sd=dbHelper.getWritableDatabase(); //得到数据库对象
        dbManager=new DBManager(this,sd);
    }

    private void initDrawerLayout(){ //初始化DrawerLayout
        drawerLayout= (DrawerLayout) findViewById(R.id.drawer_layout); //得到DrawerLayout
        final ImageView imageView= (ImageView) findViewById(R.id.drawer_indicator); //得到存放“抽屉/箭头”图标的ImageView
        final Resources resources=getResources();//得到应用程序相关的资源对象

        drawerArrowDrawable=new DrawerArrowDrawable(resources); //实例化DrawerArrowDrawable对象。“抽屉/箭头”图标
        drawerArrowDrawable.setStrokeColor(resources.getColor(R.color.light_gray));
        imageView.setImageDrawable(drawerArrowDrawable); //给ImageView设置图片。此处左上角就有了“抽屉/箭头”图标

        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener(){ //添加DrawerLayout的事件监听器

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) { //侧边栏的位置改变时调用
                offset = slideOffset;

                // Sometimes slideOffset ends up so close to but not quite 1 or 0.
                if (slideOffset >= .995) {
                    flipped = true;
                    drawerArrowDrawable.setFlip(flipped);
                } else if (slideOffset <= .005) {
                    flipped = false;
                    drawerArrowDrawable.setFlip(flipped);
                }

                drawerArrowDrawable.setParameter(offset);
            }

            @Override
            public void onDrawerOpened(View drawerView) { //侧边栏打开后的响应函数
                drawerView.setClickable(true); //解决侧边栏打开时，下层的主布局仍然可以响应点击事件的问题
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(drawerLayout.isDrawerVisible(GravityCompat.START)){
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                else
                {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        //-------------------------------------------------
        //填充ListView
        final List<LeftListViewItem> leftListViewItems=new ArrayList<LeftListViewItem>(); //ListView中子项的集合
        initLeftListViewItems(leftListViewItems);
        LeftListAdapter leftListAdapter=new LeftListAdapter(this,android.R.layout.simple_list_item_1,R.layout.left_list_item,leftListViewItems);
        ListView funcModule_listView= (ListView) findViewById(R.id.left_side_listView);
        funcModule_listView.setAdapter(leftListAdapter);
        //ListView中子项的点击响应事件

        funcModule_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LeftListViewItem item=leftListViewItems.get(position);//得到被点击的子项
                drawerLayout.closeDrawers(); //收回DrawerLayout的侧边栏
                switch (item.getText()){
                    case "查询图斑":
                        LeftListViewMethods.searchParcel();
                        break;
                    case "导出属性信息":
                        LeftListViewMethods.exportTableAsExcel(MainActivity.sd,"surveyInfo",CommonUtil.getSdCardPath()+ File.separator+"KLSurveyData"+File.separator+"excel"+File.separator+"output.xlsx");
                        //Toast.makeText(MainActivity.this, "导出属性信息", Toast.LENGTH_SHORT).show();
                        break;
                    case "作者信息":
                        LeftListViewMethods.author_info();
                        break;
                    default:
                }
            }
        });

    }

    private void initLeftListViewItems(List<LeftListViewItem> leftListViewItems){ //初始化侧边栏中ListView的子项列表
        LeftListViewItem list_item_search_parcel=new LeftListViewItem("查询图斑",R.drawable.left_side_listview_item_mark);
        leftListViewItems.add(list_item_search_parcel);

        LeftListViewItem list_item_export_attribute_info=new LeftListViewItem("导出属性信息",R.drawable.left_side_listview_item_mark);
        leftListViewItems.add(list_item_export_attribute_info);

        LeftListViewItem list_item_author_info=new LeftListViewItem("作者信息",R.drawable.left_side_listview_item_mark);
        leftListViewItems.add(list_item_author_info);
    }




}
