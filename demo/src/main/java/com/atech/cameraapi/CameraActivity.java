package com.atech.cameraapi;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.atech.glcamera.camera.CameraCore;
import com.atech.glcamera.interfaces.FileCallback;
import com.atech.glcamera.interfaces.ImageCallback;
import com.atech.glcamera.utils.FileUtils;
import com.atech.glcamera.views.CameraView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    Button btncapture;
    ImageView imgSwitch;
    Button btnRecord;
    CameraView mCameraView;

    boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        btncapture = findViewById(R.id.btn_capture);
        imgSwitch = findViewById(R.id.img_switch);
        btnRecord = findViewById(R.id.btn_record);
        mCameraView = findViewById(R.id.glcamera);


        btncapture.setOnClickListener(this);
        imgSwitch.setOnClickListener(this);
        btnRecord.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_capture:

                onCapture();
                break;

            case R.id.btn_record:

                onRecord();

                break;

            case R.id.img_switch:

                mCameraView.switchCamera();



                break;
        }

    }

    private void onCapture() {


        mCameraView.takePicture(new ImageCallback() {
            @Override
            public void onData(final byte[] bytes) {


                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        //创建路径

                        File newfile = FileUtils.createImageFile();

                        FileOutputStream fos;
                        try {
                            fos = new FileOutputStream(newfile);
                            fos.write(bytes);
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //如果直接保存，你会发现照片是旋转了的。所以需要做处理
                        rotateImageView(CameraCore.mCameraId, CameraCore.mOrientation, newfile.getAbsolutePath());
                    }
                }).start();

            }

        });

    }

    /**
     * 旋转图片
     *
     * @param cameraId    前置还是后置
     * @param orientation 拍照时传感器方向
     * @param path        图片路径
     */
    private void rotateImageView(int cameraId, int orientation, String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        Matrix matrix = new Matrix();
        //0是后置
        if (cameraId == 0) {
            if (orientation == 90) {
                matrix.postRotate(90);
            }
        }
        //1是前置
        if (cameraId == 1) {
            //顺时针旋转270度
            matrix.postRotate(270);
        }
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        File file = new File(path);
        //重新写入文件
        try {
            // 写入文件
            FileOutputStream fos;
            fos = new FileOutputStream(file);
            //默认jpg
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            resizedBitmap.recycle();

            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(file)));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 录像
     */
    public void onRecord() {

        if (isRecording) {

            mCameraView.stopRecord(new FileCallback() {
                @Override
                public void onData(File file) {

                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.fromFile(file)));
                    Toast.makeText(CameraActivity.this, "record finished", Toast.LENGTH_SHORT).show();

                }
            });

            btnRecord.setText("开始录制");

        } else {

            mCameraView.startRecord();
            btnRecord.setText("结束录制");

        }

        isRecording = !isRecording;

    }
}
