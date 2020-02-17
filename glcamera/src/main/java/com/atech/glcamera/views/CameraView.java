package com.atech.glcamera.views;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


import com.atech.glcamera.camera.CameraCore;
import com.atech.glcamera.interfaces.FileCallback;
import com.atech.glcamera.interfaces.ImageCallback;

/**
 * 普通的cameraview,实现简单的拍照和录像功能用这个歌即可
 * created by desong 2020 02 13
 */

public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

    private CameraCore cameraHelper;
    Context c;

    public CameraView(Context context) {
        super(context);

        init(context);

    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);

    }

    private void init(Context c) {

        this.c = c;
        SurfaceHolder mHolder = getHolder();
        mHolder.addCallback(this);

    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        cameraHelper = new CameraCore(this);
        cameraHelper.openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {


        cameraHelper.startPreview(holder);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {


        cameraHelper.releaseMediaRecorder();
        cameraHelper.releaseCamera();


    }

    //交换像头
    public void switchCamera(){

        cameraHelper.switchCamera(getHolder());

    }
   //拍照
    public void takePicture(ImageCallback imageCallback){

        cameraHelper.takePicture(imageCallback);

    }

    //开始录像
    public void startRecord(){

        cameraHelper.startRecord();

    }

    //结束录像
    public void stopRecord(FileCallback fileCallback){

        cameraHelper.stopRecord(fileCallback);

    }


}
