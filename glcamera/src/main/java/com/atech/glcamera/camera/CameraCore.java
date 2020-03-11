package com.atech.glcamera.camera;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.atech.glcamera.interfaces.FileCallback;
import com.atech.glcamera.interfaces.ImageCallback;
import com.atech.glcamera.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * cameraAPI核心类
 * created by desong
 * 2020 0216
 */
public class CameraCore {

    public Camera mCamera;
    public static int mCameraId = 0;
    public static int mOrientation = 0;
    public SurfaceView surfaceView;
    public int fitWidth;
    public int fitHeight;
    public int videoSizes[] = new int[2];

    
    public CameraCore(SurfaceView surfaceView) {

        this.surfaceView = surfaceView;


    }

    /**
     * 打开相机
     */

    public void openCamera(int mCameraId0){


            try {

                mCameraId = mCameraId0;
                mCamera = Camera.open(mCameraId0);


                Camera.Parameters parameters = mCamera.getParameters();

                if (parameters.getSupportedFocusModes().contains(
                        Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }

                //1.设置预览尺寸，防止预览画面变形
                List<Camera.Size> sizes1 = parameters.getSupportedPreviewSizes(); //得到的比例，宽是大头
                int[] result1 = getOptimalSize(sizes1, surfaceView.getWidth(), surfaceView.getHeight());
                parameters.setPreviewSize(result1[0], result1[1]);
                fitWidth = result1[0];
                fitHeight = result1[1];

                //2.设置拍照取得的图片尺寸
                List<Camera.Size>sizes2 = parameters.getSupportedPictureSizes();
                int[] result2 = getOptimalSize(sizes2,surfaceView.getWidth(),surfaceView.getHeight());
                parameters.setPictureSize(result2[0],result2[1]);

                //3.得到video尺寸，传给mediarecorder
                List<Camera.Size>sizes3 = parameters.getSupportedVideoSizes();
                videoSizes=getOptimalSize(sizes3,surfaceView.getWidth(),surfaceView.getHeight());


                mCamera.setParameters(parameters);

                //设置相机方向
                setCameraDisplayOrientation(mCameraId);

            }catch (Exception e){

                Log.v("aaaaa",e.getMessage());
            }


    }


    /**
     * 释放相机
     */

    public void releaseCamera() {


            if (mCamera!=null){

                mCamera.stopPreview();
                //释放向机
                mCamera.release();
                mCamera = null;

            }


    }

    /**
     * *找出最接近的尺寸，以保证图像不会被拉伸
     * @param sizes
     * @param currentWidth
     * @param currentHeight
     * @return
     */

    private int[] getOptimalSize(List<Camera.Size> sizes, int currentWidth, int currentHeight) {

        int i = 1;
        //大头
        int bestWidth = sizes.get(0).width;
        //小头
        int bestHeight = sizes.get(0).height;


        //很重要，第一项一定是高/宽
        float min = Math.abs((float) bestHeight / (float) bestWidth - (float) currentWidth / (float) currentHeight);

        while (i < sizes.size()) {


            float current = Math.abs((float) sizes.get(i).height / (float) sizes.get(i).width - (float) currentWidth / (float) currentHeight);

            if (current < min) {

                min = current;
                bestWidth = sizes.get(i).width;
                bestHeight = sizes.get(i).height;

            }

            i++;

        }

        int[] result = new int[2];
        result[0] = bestWidth;
        result[1] = bestHeight;

        Log.v("glcamera", bestWidth + "//" + bestHeight);

        return result;


    }

    /**
     * 根据手机屏幕以及前后摄来调整相机角度
     *
     * @param cameraId
     */

    private void setCameraDisplayOrientation(int cameraId) {
        Activity targetActivity = (Activity) surfaceView.getContext();
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = targetActivity.getWindowManager().getDefaultDisplay()
                .getRotation();



        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        mCamera.setDisplayOrientation(result);
        mOrientation = result;

    }


    /*******************************************************************************************
     * only for glsurfaceview
     * @param texture
     ********************************************************************************************/

    public SurfaceTexture texture;

    public void startPreview(SurfaceTexture texture0) {

            texture = texture0;

            try {
                mCamera.setPreviewTexture(texture);
                mCamera.startPreview();

            } catch (IOException e) {
                Log.v("glcamera",e.getMessage());
            }



    }



    public void switchCamera() {



            if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
            } else {
                mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
            }

            releaseCamera();
            openCamera(mCameraId);
            startPreview(texture);



    }


    /**************************************************************************************************************
     * only for surfaceview
     **************************************************************************************************************/

    private File outputFile;
    private MediaRecorder mMediaRecorder;

    public void startPreview(SurfaceHolder holder) {


            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }

    }

    public void switchCamera(SurfaceHolder holder) {


            if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
            } else {
                mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
            }

            releaseCamera();
            openCamera(mCameraId);
            startPreview(holder);


    }

    /**
     * 拍照
     * @param imageCallback
     */
    public void takePicture(final ImageCallback imageCallback) {

         mCamera.takePicture(null, null, (data, camera) ->
                imageCallback.onData(data));


    }

    /**
     * 录像功能
     */

    public void startRecord(){


            prepareMediarecorder();

            try {
                mMediaRecorder.start();
            }catch (Exception e){
                Log.v("aaaaa",e.getMessage());
            }


    }

    public void stopRecord(FileCallback fileCallback){



           if(mMediaRecorder != null){
               mMediaRecorder.release();
               mMediaRecorder = null;
           }

           if(mCamera != null){
               mCamera.release();
           }
           openCamera(mCameraId);
           //并设置预览
           startPreview(surfaceView.getHolder());

           fileCallback.onData(outputFile);

    }

    private void prepareMediarecorder(){

        mCamera.unlock();

        mMediaRecorder = new MediaRecorder();

        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        outputFile = FileUtils.createVideoFile();
        mMediaRecorder.setOutputFile(outputFile.toString());
        mMediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());
        mMediaRecorder.setVideoSize(videoSizes[0],videoSizes[1]);

        //调整视频旋转角度 如果不设置 后置和前置都会被旋转播放
        if (mCameraId==Camera.CameraInfo.CAMERA_FACING_BACK){
            mMediaRecorder.setOrientationHint(90);
        }else{
            mMediaRecorder.setOrientationHint(270);
        }

        try {
            mMediaRecorder.prepare();
        } catch ( IllegalStateException e) {
            Log.d("aaaaa", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();

        } catch ( IOException e) {
            Log.d("aaaaa", "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();

        }

    }

    public void releaseMediaRecorder(){

           if(mMediaRecorder != null){
               mMediaRecorder.release();
               mMediaRecorder = null;
           }

    }

}
