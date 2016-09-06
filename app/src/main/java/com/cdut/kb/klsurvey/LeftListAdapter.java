package com.cdut.kb.klsurvey;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Ren on 2016/8/20.
 */
public class LeftListAdapter extends ArrayAdapter<LeftListViewItem> { //侧边栏中ListView对应的自定义适配器

    private int resourceId; //用于保存某一个子项布局对应得资源id

    public LeftListAdapter(Context context, int resource, int textViewResourceId, List<LeftListViewItem> objects) {
        super(context, resource, textViewResourceId, objects);
        resourceId=textViewResourceId;//保存下子项布局对应得资源id
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) { //当子项进入屏幕时调用该函数
        LeftListViewItem leftListViewItem=getItem(position); //得到某一个子项对应得实体对象
        View view;
        if(convertView==null){ //子项第一次进入屏幕，为子项创建视图
            view= LayoutInflater.from(getContext()).inflate(resourceId,null);
        }else{ //不是第一次进入，则复用之前的View
            view=convertView;
        }
        //从子项的布局中取出其图片和文本容器
        ImageView left_list_item_image= (ImageView) view.findViewById(R.id.left_list_item_image);
        TextView left_list_item_name= (TextView) view.findViewById(R.id.left_list_item_name);
        //为子项设置图片和文本
        left_list_item_image.setImageResource(leftListViewItem.getImageId());
        left_list_item_name.setText(leftListViewItem.getText());
        return view; //返回子项的布局
    }
}
