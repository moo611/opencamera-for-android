package com.atech.glcamera.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import com.atech.glcamera.camera.CameraCore;
import com.atech.glcamera.filters.BaseFilter;
import com.atech.glcamera.filters.BeautyFilter;
import com.atech.glcamera.gpuimage.GPUImageNativeLibrary;
import com.atech.glcamera.grafika.my.HWRecorderWrapper;
import com.atech.glcamera.interfaces.FileCallback;
import com.atech.glcamera.interfaces.FilteredBitmapCallback;
import com.atech.glcamera.utils.FileUtils;
import com.atech.glcamera.utils.FilterFactory;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class GLCameraView extends GLSurfaceView {

    public GLCameraView.GLRenderer renderer;
    private BaseFilter mCurrentFilter;
    private CameraCore mCameraHelper;
    private static final String TAG = "aaaaa";
    private Context c;
    private int mTextureId;
    private SurfaceTexture mSurfaceTexture;
    private float[] mSTMatrix = new float[16];
    private boolean mRecordingEnabled ;
    private  FilterFactory.FilterType type;
    private HWRecorderWrapper hwRecorderWrapper;
    private FileCallback mFileCallback;

    private static final int DEFAULT_BITRATE = 1000 * 1000;
    private int mChannels = 1;
    private int mSampleRate = 48000;
    private File mOutputFile;
    private static int STATE_ON = 1;
    private static int STATE_OFF = 2;
    private int state = 2;


    public GLCameraView(Context context) {
        super(context);

        init(context);

    }

    public GLCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    private void init(Context context) {

        this.c = context;

        setEGLContextClientVersion(2);

        type = FilterFactory.FilterType.Beauty;

        renderer = new GLCameraView.GLRenderer(this,type);
        setRenderer(renderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);


    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);

        if (mCameraHelper!=null){
            mCameraHelper.releaseCamera();
        }
        if (hwRecorderWrapper!=null&&state==STATE_ON){
            hwRecorderWrapper.stop();
            state = STATE_OFF;
            mRecordingEnabled = false;
        }
    }

    public class GLRenderer implements Renderer,SurfaceTexture.OnFrameAvailableListener {


        GLSurfaceView surfaceView;

        private final Queue<Runnable> runOnDraw;
        private final Queue<Runnable> runOnDrawEnd;



        public GLRenderer(GLSurfaceView surfaceView, FilterFactory.FilterType type) {

            this.surfaceView = surfaceView;
            hwRecorderWrapper = new HWRecorderWrapper(surfaceView.getContext());


            mOutputFile = FileUtils.createSystemVideoFile(c);


            Log.v("outputvideo:",mOutputFile.getAbsolutePath());

            mRecordingEnabled = false;
            mCameraHelper = new CameraCore(surfaceView);
            mCurrentFilter = FilterFactory.createFilter(c,type);

            runOnDraw = new LinkedList<>();
            runOnDrawEnd = new LinkedList<>();


        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {


        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {


            GLES20.glViewport(0, 0, width, height);

            mCameraHelper.openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
            mCurrentFilter.createProgram();
            mCurrentFilter.onInputSizeChanged(getWidth(),getHeight());

            mTextureId = BaseFilter.bindTexture();
            mSurfaceTexture = new SurfaceTexture(mTextureId);
            mSurfaceTexture.setOnFrameAvailableListener(this);

            mCameraHelper.startPreview(mSurfaceTexture);
        }

        /**
         * 关于预览出现镜像，旋转等问题，有两种方案:
         * 1.在相机预览的地方进行调整
         * 2.通过opengl的矩阵变换在绘制的时候进行调整
         * 这里我采用了前者
         */

        @Override
        public void onDrawFrame(GL10 gl) {

            runAll(runOnDraw);
            mSurfaceTexture.updateTexImage();


            if (mRecordingEnabled){

                if (state==STATE_OFF){
                    hwRecorderWrapper.start(
                            mCameraHelper.fitHeight,
                            mCameraHelper.fitWidth,
                            DEFAULT_BITRATE,mSampleRate,
                            mChannels,mOutputFile.getAbsolutePath(),
                            EGL14.eglGetCurrentContext());

                    state = STATE_ON;

                }else{
                    //already on
                    //do nothing
                }

            }else{

                if (state==STATE_ON){

                    hwRecorderWrapper.stop();

                    state = STATE_OFF;

                    if (mFileCallback!=null){

                        mFileCallback.onData(mOutputFile);
                    }

                }else{
                    //already off
                    //do noting
                }

            }


            hwRecorderWrapper.onFrameAvailable(mTextureId,mSurfaceTexture);

            mSurfaceTexture.getTransformMatrix(mSTMatrix);
            mCurrentFilter.draw(mTextureId,mSTMatrix);

            runAll(runOnDrawEnd);

        }

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            //Log.v("aaaaa","avaible");
            surfaceView.requestRender();

        }


        void runOnDraw(final Runnable runnable) {
            synchronized (runOnDraw) {
                runOnDraw.add(runnable);
            }
        }

        void runOnDrawEnd(final Runnable runnable) {
            synchronized (runOnDrawEnd) {
                runOnDrawEnd.add(runnable);
            }
        }

        private void runAll(Queue<Runnable> queue) {
            synchronized (queue) {
                while (!queue.isEmpty()) {
                    queue.poll().run();
                }
            }
        }

    }

    public void takePicture(final FilteredBitmapCallback filteredBitmapCallback) {

        //获取当前的bitmap

        try {
            filteredBitmapCallback.onData(capture());
        } catch (InterruptedException e) {
            Log.v("aaaaa",e.getMessage());
        }

    }


    public void changeRecordingState(boolean mRecordingEnabled){

       this.mRecordingEnabled  = mRecordingEnabled;

    }


    public void switchCamera(){

        mCameraHelper.switchCamera();
        Log.v("aaaaa","switchcamera:"+Thread.currentThread());


    }

    public void updateFilter(final FilterFactory.FilterType type){

        this.type = type;

        renderer.runOnDraw(() -> {


            mCurrentFilter.releaseProgram();
            mCurrentFilter = FilterFactory.createFilter(c,type);

            //调整预览画面
            mCurrentFilter.createProgram();
            mCurrentFilter.onInputSizeChanged(getWidth(),getHeight());
            //调整录像画面
            hwRecorderWrapper.updateFilter(type);

            Log.v("aaaaa","updateFilter:"+Thread.currentThread());

        });



    }

    /**
     * Capture the current image with the size as it is displayed and retrieve it as Bitmap.
     *
     * @return current output as Bitmap
     * @throws InterruptedException
     */
    private Bitmap capture() throws InterruptedException {
        final Semaphore waiter = new Semaphore(0);

        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();

        // Take picture on OpenGL thread
        final Bitmap resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        renderer.runOnDrawEnd(() -> {

            GPUImageNativeLibrary.adjustBitmap(resultBitmap);
            waiter.release();

            Log.v("aaaaa","curent thread is:"+Thread.currentThread().getName());

        });

        requestRender();
        waiter.acquire();
        return resultBitmap;
    }


    public void setOuputMP4File(File file){

        this.mOutputFile = file;

    }

    public void setrecordFinishedListnener(FileCallback fileCallback){

        this.mFileCallback = fileCallback;
    }

    public void enableBeauty(boolean enableBeauty){

        if (enableBeauty){

            type = FilterFactory.FilterType.Beauty;

        }else{

            type = FilterFactory.FilterType.Original;
        }

        updateFilter(type);
    }

    /**
     * 0~1
     * @param beautyLevel
     */
    public void setBeautyLevel(float beautyLevel){

        if (mCurrentFilter instanceof BeautyFilter){

            ((BeautyFilter) mCurrentFilter).setSmoothOpacity(beautyLevel);
            hwRecorderWrapper.changeBeautyLevel(beautyLevel);


        }

    }





}
