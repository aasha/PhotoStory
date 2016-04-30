package com.pixtory.app.model;

/**
 * Created by aasha.medhi on 12/31/15.
 */
public class Category {
    public int id;
    public String name;
    public String imageUrl;
    public int drawable;
    public Category(int id, String name, String imageUrl){
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }
    public Category(int id, String name, int drawable){
        this.id = id;
        this.name = name;
        this.drawable = drawable;
    }
    public Category(){

    }
}
