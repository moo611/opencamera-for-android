package com.atech.glcamera.grafika.my;


import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaCodecInfo;
import android.opengl.EGLContext;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.atech.glcamera.filters.BaseFilter;
import com.atech.glcamera.filters.BeautyFilter;
import com.atech.glcamera.grafika.gles.EglCore;
import com.atech.glcamera.grafika.gles.WindowSurface;
import com.atech.glcamera.utils.FilterFactory;


public class HWRecorderWrapper implements AudioRecorder.AudioRecordCallback {

    private static final long MAX_TIMEOUT = 3000;
    private Thread mVExecutor;
    private Thread mAExecutor;
    private HWEncorder mEncoder = new HWEncorder();
    private WindowSurface mInputWindowSurface;
    private EglCore mEglCore;
    private BaseFilter mFullScreen;
    private Context c;
    private FilterFactory.FilterType type = FilterFactory.FilterType.Original;
    private int mImageFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
    private Handler mVideoHandler;
    private Handler mAudioHandler;
    private AudioRecorder mAudioRecorder ;
    private boolean isReady = false;



    public HWRecorderWrapper(Context c) {
        this.c = c;
    }

    public void start(int width, int height, int bitRate, int sampleRate,
                         int channels, String dstFilePath, EGLContext eglContext) {

        try {
            mEncoder.init(width, height, mImageFormat, bitRate, sampleRate, channels, dstFilePath);
        } catch (Exception e) {
            Log.v("aaaaa", e.getMessage() + "and" + e.getLocalizedMessage());
            return ;
        }

        mAudioRecorder = new AudioRecorder();

        mVExecutor = new Thread(new VideoRunnale(eglContext, width, height));
        mAExecutor = new Thread(new AudioRunnale());
        mAudioRecorder.setRecordCallback(this);
        //启动线程
        mVExecutor.start();
        mAExecutor.start();


    }

    public void changeBeautyLevel(float beautyLevel) {

         if (mFullScreen instanceof BeautyFilter){
             ((BeautyFilter) mFullScreen).setSmoothOpacity(beautyLevel);
         }

    }

    public class VideoRunnale implements Runnable {

        int width;
        int height;
        EGLContext eglContext;

        public VideoRunnale(EGLContext eglContext, int width, int height) {

            this.eglContext = eglContext;
            this.width = width;
            this.height = height;
        }

        @Override
        public void run() {

            Looper.prepare();

            mVideoHandler = new Handler();

            prepareEGL(eglContext, width, height);

            Looper.loop();

        }
    }


    public class AudioRunnale implements Runnable {

        @Override
        public void run() {

            if (mAudioRecorder.start()) {
                Log.v("aaaaa", "audiorecorder succeed+" + Thread.currentThread().getName());
            } else {
                Log.v("aaaaa", "audiorecorder failed+" + Thread.currentThread().getName());
            }


            Looper.prepare();
            mAudioHandler = new Handler();
            Looper.loop();

        }
    }


    private void prepareEGL(EGLContext eglContext, int width, int height) {

        mVideoHandler.post(() -> {

            mEglCore = new EglCore(eglContext, EglCore.FLAG_RECORDABLE);
            mInputWindowSurface = new WindowSurface(mEglCore, mEncoder.getInputSurface(), true);
            mInputWindowSurface.makeCurrent();

            mFullScreen = FilterFactory.createFilter(c, type);
            mFullScreen.createProgram();
            mFullScreen.onInputSizeChanged(width, height);

            isReady = true;


        });


    }


    /**
     * 开启视频录制，该方法随着帧数反复调用
     *
     * @param mTextureId
     * @param st
     */

    public void onFrameAvailable(int mTextureId, SurfaceTexture st) {

        if (!isReady) {
           // Log.v("aaaaa", "not ready");
            return;
        }

        if (mVideoHandler == null) {
            Log.v("aaaaa", "return");
            return;
        }
        if (mEncoder == null) {
            Log.v("aaaaa", "encoder null");
            return;
        }

        if (mFullScreen == null) {
            Log.v("aaaaa", "filter null");
            return;
        }

        if (mInputWindowSurface == null) {
            Log.v("aaaaa", "inputsurface null");
            return;
        }
        mVideoHandler.post(() -> {

            float[] transform = new float[16];
            st.getTransformMatrix(transform);

            //每帧间隔多少毫秒
            int loopingInterval = 1000/30;
            //每帧的时间估算。这里原来想用grafika里的算法，但是视频和音频总是不能同步。因此改了算法。
            long timestamp = (SystemClock.uptimeMillis() + loopingInterval) * 1000000;


            try {
                mEncoder.recordImage();
                mFullScreen.draw(mTextureId, transform);
                mInputWindowSurface.setPresentationTime(timestamp);
                mInputWindowSurface.swapBuffers();
            } catch (Exception e) {
                e.printStackTrace();

            }

        });

    }


    /**
     * 开始录制，该方法只调用一次
     *
     * @param data
     */

    @Override
    public void onRecordSample(byte[] data) {

        if (mAudioHandler == null) {
            return;
        }

        mAudioHandler.post(() -> {


            try {
                mEncoder.recordSample(data);
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

    }

    public void stop() {

            releaseEncoder();

            //关闭录音机
            mAudioRecorder.stop();

            //清空handler
            mAudioHandler.removeCallbacksAndMessages(null);
            mVideoHandler.removeCallbacksAndMessages(null);

            mAudioHandler = null;
            mVideoHandler = null;



    }

    public void updateFilter(FilterFactory.FilterType type) {

        this.type = type;

    }
    private void releaseEncoder() {

        mEncoder.stop();

        mVideoHandler.post(() -> {

            if (mInputWindowSurface != null) {
                mInputWindowSurface.release();
                mInputWindowSurface = null;
            }
            if (mFullScreen != null) {
                mFullScreen.releaseProgram();
                mFullScreen = null;
            }
            if (mEglCore != null) {
                mEglCore.release();
                mEglCore = null;
            }
        });


    }

}
