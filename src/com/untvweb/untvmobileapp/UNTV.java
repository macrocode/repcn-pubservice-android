package com.untvweb.untvmobileapp;


import org.apache.cordova.DroidGap;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class UNTV extends DroidGap {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.loadUrl("file:///android_asset/www/index.html");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.untv, menu);
        return true;
    }
    
}