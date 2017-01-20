package com.scms.org.scms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

public class ResultActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);
        if(CameraActivity.imgResult!=null){
            ImageView imgResult = (ImageView) findViewById(R.id.imgResult);
            imgResult.setImageBitmap(CameraActivity.imgResult);
        }
    }
}