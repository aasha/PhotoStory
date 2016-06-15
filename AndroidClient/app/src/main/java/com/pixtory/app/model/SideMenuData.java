package com.pixtory.app.model;

import android.graphics.drawable.Drawable;

/**
 * Created by krish on 15/06/2016.
 */
public class SideMenuData {
    public Drawable drawable = null;
    public String menuText = "";
    public SideMenuData(Drawable drawable,String menuText){
        this.drawable = drawable;
        this.menuText = menuText;
    }
}
