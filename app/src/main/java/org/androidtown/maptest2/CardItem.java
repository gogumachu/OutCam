package org.androidtown.maptest2;

import android.widget.ImageView;

public class CardItem {
    private String cameraLocationTitle; //상세 위치
    private String content; //상세 내용
    private String date; //날짜

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private String url;
    //private ImageView imageView;

    public CardItem(String cameraLocationTitle, String content, String date,String url) {
        this.cameraLocationTitle = cameraLocationTitle;
        this.content = content;
        this.date = date;
        this.url = url;
        //this.imageView = imageView;
    }

    public String getCameraLocationTitle() {
        return cameraLocationTitle;
    }

    public void setCameraLocationTitle(String cameraLocationTitle) {
        this.cameraLocationTitle = cameraLocationTitle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
