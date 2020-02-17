package com.atech.glcamera.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import com.atech.glcamera.camera.CameraCore;
import com.atech.glcamera.filters.BaseFilter;
import com.atech.glcamera.filters.BeautyFilter;
import com.atech.glcamera.filters.BlackCatFilter;
import com.atech.glcamera.filters.BlackFilter;
import com.atech.glcamera.filters.HealthyFilter;
import com.atech.glcamera.filters.MagicRomanceFilter;
import com.atech.glcamera.filters.MagicSakuraFilter;
import com.atech.glcamera.filters.OriginalFilter;
import com.atech.glcamera.gpuimage.GPUImageNativeLibrary;
import com.atech.glcamera.grafika.TextureMovieEncoder;
import com.atech.glcamera.interfaces.FilteredBitmapCallback;
import com.atech.glcamera.utils.FileUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * glcameraview
 * created by desong 2020 0213
 */

public class GLCameraView extends GLSurfaceView {

    private BaseFilter mCurrentFilter;
    public GLRenderer renderer;
    private CameraCore mCameraHelper;
    private static final String TAG = "aaaaa";
    private Context c;
    private int mTextureId;
    private SurfaceTexture mSurfaceTexture;
    private float[] mSTMatrix = new float[16];
    private int oldpos = 0;
    private boolean mRecordingEnabled;
    private TextureMovieEncoder mVideoEncoder;

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

        renderer = new GLRenderer(this,new MagicSakuraFilter(c));
        setRenderer(renderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);


    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);

        Log.v("aaaaa", "destroyed");
        mCameraHelper.releaseCamera();

    }

    /**
     * 内部渲染类render
     */

    public class GLRenderer implements Renderer,SurfaceTexture.OnFrameAvailableListener {


        GLSurfaceView surfaceView;
        private File mOutputFile;
        private int mRecordingStatus;
        private static final int RECORDING_OFF = 0;
        private static final int RECORDING_ON = 1;
        private static final int RECORDING_RESUMED = 2;
        private final Queue<Runnable> runOnDraw;
        private final Queue<Runnable> runOnDrawEnd;

        public GLRenderer(GLSurfaceView surfaceView,BaseFilter filter) {

            this.surfaceView = surfaceView;
            mVideoEncoder = new TextureMovieEncoder(c);
            mOutputFile = FileUtils.createVideoFile();
            mRecordingStatus = -1;
            mRecordingEnabled = false;
            mCameraHelper = new CameraCore(surfaceView);
            mCurrentFilter = filter;

            runOnDraw = new LinkedList<>();
            runOnDrawEnd = new LinkedList<>();

        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {


            Log.v("aaaaa","oncreated");

            mCameraHelper.openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
            mCurrentFilter.createProgram();
            mCurrentFilter.onInputSizeChanged(mCameraHelper.fitHeight,mCameraHelper.fitWidth);
            mRecordingEnabled = mVideoEncoder.isRecording();
            if (mRecordingEnabled) {
                mRecordingStatus = RECORDING_RESUMED;
            } else {
                mRecordingStatus = RECORDING_OFF;
            }

            mTextureId = BaseFilter.bindTexture();
            mSurfaceTexture = new SurfaceTexture(mTextureId);
            mSurfaceTexture.setOnFrameAvailableListener(this);
            mCameraHelper.startPreview(mSurfaceTexture);

        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {

            Log.v("aaaaa","on size changed");

            GLES20.glViewport(0, 0, width, height);


        }

        @Override
        public void onDrawFrame(GL10 gl) {

           runAll(runOnDraw);
            mSurfaceTexture.updateTexImage();

            if (mRecordingEnabled) {
                switch (mRecordingStatus) {
                    case RECORDING_OFF:
                        Log.d(TAG, "START recording");
                        // start recording
                        Log.v("aaaaa","value:"+mCameraHelper.fitWidth+"//"+mCameraHelper.fitHeight);
                        mVideoEncoder.startRecording(new TextureMovieEncoder.EncoderConfig(
                                mOutputFile, mCameraHelper.fitHeight, mCameraHelper.fitWidth, 1000000, EGL14.eglGetCurrentContext()));
                        mRecordingStatus = RECORDING_ON;
                        break;
                    case RECORDING_RESUMED:
                        Log.d(TAG, "RESUME recording");
                        mVideoEncoder.updateSharedContext(EGL14.eglGetCurrentContext());
                        mRecordingStatus = RECORDING_ON;
                        break;
                    case RECORDING_ON:
                        // yay
                        break;
                    default:
                        throw new RuntimeException("unknown status " + mRecordingStatus);
                }
            } else {
                switch (mRecordingStatus) {
                    case RECORDING_ON:
                    case RECORDING_RESUMED:
                        // stop recording
                        Log.d(TAG, "STOP recording");
                        mVideoEncoder.stopRecording();
                        mRecordingStatus = RECORDING_OFF;

                        c.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                Uri.fromFile(mOutputFile)));

                        break;
                    case RECORDING_OFF:
                        // yay
                        break;
                    default:
                        throw new RuntimeException("unknown status " + mRecordingStatus);
                }
            }


            // Set the video encoder's texture name.  We only need to do this once, but in the
            // current implementation it has to happen after the video encoder is started, so
            // we just do it here.
            //
            // TODO: be less lame.
            mVideoEncoder.setTextureId(mTextureId);

            // Tell the video encoder thread that a new frame is available.
            // This will be ignored if we're not actually recording.
            mVideoEncoder.frameAvailable(mSurfaceTexture);

            //关于预览出现镜像，旋转等问题，有两种方案:
            // 1.在相机预览的地方进行调整
            // 2.通过opengl的矩阵变换在绘制的时候进行调整
            //这里我采用了前者

            mSurfaceTexture.getTransformMatrix(mSTMatrix);

            mCurrentFilter.draw(mTextureId,mSTMatrix);

            runAll(runOnDrawEnd);

        }

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {

            surfaceView.requestRender();

        }

        private void runAll(Queue<Runnable> queue) {
            synchronized (queue) {
                while (!queue.isEmpty()) {
                    queue.poll().run();
                }
            }
        }


        protected void runOnDraw(final Runnable runnable) {
            synchronized (runOnDraw) {
                runOnDraw.add(runnable);
            }
        }

        protected void runOnDrawEnd(final Runnable runnable) {
            synchronized (runOnDrawEnd) {
                runOnDrawEnd.add(runnable);
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


    /**
     * Notifies the renderer that we want to stop or start recording.
     */
    public void changeRecordingState(boolean isRecording) {
        Log.d(TAG, "changeRecordingState: was " + mRecordingEnabled + " now " + isRecording);
        mRecordingEnabled = isRecording;
    }

    public void switchCamera(){

        mCameraHelper.switchCamera();
        Log.v("aaaaa","switchcamera:"+Thread.currentThread());


    }

    public void updateFilter(final int pos){

        if (pos==oldpos){
            return;
        }

        renderer.runOnDraw(new Runnable() {
            @Override
            public void run() {

                mCurrentFilter.releaseProgram();
                mCurrentFilter = null;
                Log.v("aaaaa","updateFilter:"+Thread.currentThread());
                 //调整预览画面
                if (pos==0){
                    mCurrentFilter = new OriginalFilter(c);
                }else{
                    mCurrentFilter = new BlackFilter(c);
                }

                  mCurrentFilter.createProgram();
                  mCurrentFilter.onInputSizeChanged(mCameraHelper.fitHeight,mCameraHelper.fitWidth);
                //调整录像画面
                mVideoEncoder.setType(pos);

            }
        });

        oldpos = pos;

    }

    /**
     * Capture the current image with the size as it is displayed and retrieve it as Bitmap.
     *
     * @return current output as Bitmap
     * @throws InterruptedException
     */
    public Bitmap capture() throws InterruptedException {
        final Semaphore waiter = new Semaphore(0);

        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();

        // Take picture on OpenGL thread
        final Bitmap resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        renderer.runOnDrawEnd(new Runnable() {
            @Override
            public void run() {

                GPUImageNativeLibrary.adjustBitmap(resultBitmap);
                waiter.release();

                Log.v("aaaaa","curent thread is:"+Thread.currentThread().getName());

            }
        });


        queueEvent(new Runnable() {
            @Override
            public void run() {


                Log.v("aaaaa","curent thread is:"+Thread.currentThread().getName());

            }
        });

        requestRender();
        waiter.acquire();

        return resultBitmap;
    }

}
