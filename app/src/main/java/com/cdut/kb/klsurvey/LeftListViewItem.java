package com.cdut.kb.klsurvey;

/**
 * Created by Ren on 2016/8/20.
 */
public class LeftListViewItem { //侧边栏中ListView的子项对应的实体类

    private String text; //子项的文本
    private int imageId; //子项对应得图片资源id

    public LeftListViewItem(String text, int imageId) {
        this.text = text;
        this.imageId = imageId;
    }

    public String getText() {
        return text;
    }

    public int getImageId() {
        return imageId;
    }
}
