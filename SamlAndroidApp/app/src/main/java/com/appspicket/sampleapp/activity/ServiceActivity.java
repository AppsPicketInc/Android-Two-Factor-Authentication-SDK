package com.appspicket.sampleapp.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import com.appspicket.sampleapp.R;
import com.facebook.drawee.backends.pipeline.Fresco;

/**
* This class is used after successfully signup or login
* */
public class ServiceActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(getApplicationContext());
        setContentView(R.layout.activity_service);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowTitleEnabled(true);
    }

}