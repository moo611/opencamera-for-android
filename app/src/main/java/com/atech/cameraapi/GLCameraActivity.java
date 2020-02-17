package com.atech.cameraapi;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.atech.glcamera.filters.BlackFilter;

import com.atech.glcamera.interfaces.FilteredBitmapCallback;
import com.atech.glcamera.utils.FileUtils;
import com.atech.glcamera.views.GLCameraView;

import java.io.File;
import java.io.FileOutputStream;


public class GLCameraActivity extends AppCompatActivity implements View.OnClickListener {



    ImageView imgSwitch;
    Button btncapture;
    Button btnRecord;
    Button btnfilter;
    GLCameraView mCameraView;

    private boolean mRecordingEnabled;      // controls button state
    private int curpos = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glcamera);


        btncapture = findViewById(R.id.btn_capture);
        imgSwitch = findViewById(R.id.img_switch);
        btnRecord = findViewById(R.id.btn_record);
        mCameraView = findViewById(R.id.glcamera);
        btnfilter = findViewById(R.id.btn_filter);

        btncapture.setOnClickListener(this);
        imgSwitch.setOnClickListener(this);
        btnRecord.setOnClickListener(this);
        btnfilter.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {


            case R.id.img_switch:


                mCameraView.switchCamera();

                break;

            case R.id.btn_capture:

                onCapture();

                break;

            case R.id.btn_record:

                onRecord();

                break;


            case R.id.btn_filter:

                if (curpos==0){

                    curpos = 1;
                    btnfilter.setText("原生");

                }else{
                    curpos = 0;
                    btnfilter.setText("黑白");
                }
                mCameraView.updateFilter(curpos);


                break;

        }


    }

    private void onCapture(){

        mCameraView.takePicture(new FilteredBitmapCallback() {
            @Override
            public void onData(Bitmap bitmap) {

                File file = FileUtils.createImageFile();
                //重新写入文件
                try {
                    // 写入文件
                    FileOutputStream fos;
                    fos = new FileOutputStream(file);
                    //默认jpg
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                    bitmap.recycle();

                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.fromFile(file)));

                    Toast.makeText(GLCameraActivity.this,"finished",Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });


    }

   private void onRecord(){

       mRecordingEnabled = !mRecordingEnabled;

       mCameraView.queueEvent(new Runnable() {
           @Override public void run() {
               // notify the renderer that we want to change the encoder's state
               mCameraView.changeRecordingState(mRecordingEnabled);
           }
       });

       if (mRecordingEnabled){

           btnRecord.setText("结束录制");

           Toast.makeText(GLCameraActivity.this,"start",Toast.LENGTH_SHORT).show();

       }else{

           btnRecord.setText("开始录制");
           Toast.makeText(GLCameraActivity.this,"finished",Toast.LENGTH_SHORT).show();

       }

   }

}
