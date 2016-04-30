package com.pixtory.app.model;

import java.util.ArrayList;

/**
 * Created by aasha.medhi on 11/25/15.
 */
public class Product {
    //public String productImage = null;
    public String productName = null;
    public String merchantName = null;
    public String productPrice = null;
    public String productDesc = null;
    public String textArea = null;
    public int likeCount = 30;

    public String contentType = null;
    public String productUrl = null;
    public String productBullet = null;

    public int productId = -1;
    public String aspectRatio = "1.0";
    public int discountedPercentage;
    public String size;
    public String rating;
    public String material;
    public String type;
    public String style;

    public boolean bookmarkedByUser = false;
    public String location;
    public String productCode;
    public String soldBy;
    public String care;
    public ArrayList<String> comments;
    public ArrayList<String> specialFeatures;
    public boolean deepLink = false;

    public ArrayList<String> productImages;
}
