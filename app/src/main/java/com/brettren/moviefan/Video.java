package com.brettren.moviefan;

/**
 * Created by Brett on 15/9/27.
 */
public class Video {
    public String url;
    public String name;
    public String screenshot_path;
    public Video(String url, String name, String screenshot_path){
        this.url = url;
        this.name = name;
        this.screenshot_path = screenshot_path;
    }
}
