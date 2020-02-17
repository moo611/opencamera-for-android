package com.atech.glcamera.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import com.atech.glcamera.interfaces.CaptureListener;
import com.atech.glcamera.interfaces.RecordCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** unfinished, don't use
 * created by desong
 * 2019 12.08
 */

public class Camera2Core {


    private CameraManager cameraManager;
    private CameraDevice mCameraDevice;
    private Handler mCameraHandler;
    private HandlerThread mHandlerThread;
    private CameraCaptureSession mPreviewSession;//session
    private MediaRecorder mMediaRecorder;
    private SurfaceView surfaceView;
    private CaptureRequest.Builder captureRequestBuilder;
    private ImageReader imageReader;
    public CaptureListener captureListener;
    private boolean isRecording = false;
    private Size previewSize;

    public static int mCameraId = 0;
    public static int mOrientation = 0;
    public int fitWidth;
    public int fitHeight;
    public int videoSizes[] = new int[2];

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    //旋转屏幕
    static {

        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);

    }

    public Camera2Core(SurfaceView surfaceView) {

        this.surfaceView = surfaceView;

    }

    /**
     * 打开相机
     */

    public void openCamera(int mCameraId) {

        this.mCameraId = mCameraId;

        cameraManager = (CameraManager) surfaceView.getContext().getSystemService(Context.CAMERA_SERVICE);
        mHandlerThread = new HandlerThread("cameraThread");
        mHandlerThread.start();
        mCameraHandler = new Handler(mHandlerThread.getLooper());

        //打开相机
        try {


            //打开相机
            if (ActivityCompat.checkSelfPermission(surfaceView.getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            cameraManager.openCamera(mCameraId+"", stateCallback, mCameraHandler);


        } catch (CameraAccessException e) {

            e.printStackTrace();

        }

    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        /**
         * 当相机打开的时候，调用
         * @param cameraDevice
         */
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {

            Log.v("aaaaa", "camera opened");
            mCameraDevice = cameraDevice;
            createPreviewSession();

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {

            Log.e("error", error + "");

        }

    };


    /**
     * 预览session
     */
    private void createPreviewSession() {

        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
        try {
            captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            List<Surface>surfaces = new ArrayList<>();
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(mCameraId+"");
            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            //预览尺寸
            previewSize = getOptimalSize(map.getOutputSizes(SurfaceTexture.class), surfaceView.getWidth(), surfaceView.getHeight());
            SurfaceHolder holder = surfaceView.getHolder();
            holder.setFixedSize(previewSize.getWidth(),previewSize.getHeight());
            Surface previewSurface = holder.getSurface();
            captureRequestBuilder.addTarget(previewSurface);
            surfaces.add(previewSurface);

            //保存图片尺寸
            imageReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ImageFormat.JPEG, 1);
            imageReader.setOnImageAvailableListener(readerListener, mCameraHandler);
            Surface imageSurface = imageReader.getSurface();
            captureRequestBuilder.addTarget(imageSurface);
            surfaces.add(imageSurface);

            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            mCameraDevice.createCaptureSession(surfaces, sessionCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }

    /**
     * 录像sesson
     */

    private void createRecordSession() {

        if (mPreviewSession != null) {

            mPreviewSession.close();
            mPreviewSession = null;
        }

        try {


            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(mCameraId+"");
            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            //预览尺寸
            SurfaceHolder holder = surfaceView.getHolder();
            Size previewSize = getOptimalSize(map.getOutputSizes(SurfaceTexture.class), surfaceView.getWidth(), surfaceView.getHeight());
            holder.setFixedSize(previewSize.getWidth(),previewSize.getHeight());
            Surface previewSurface = holder.getSurface();
            surfaces.add(previewSurface);
            captureRequestBuilder.addTarget(previewSurface);

            // Set up Surface for the MediaRecorder
            Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            captureRequestBuilder.addTarget(recorderSurface);

            mCameraDevice.createCaptureSession(surfaces, sessionCallback, mCameraHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }

    /**
     * "session"的回调，当配置好实例后，设置预览
     */

    private CameraCaptureSession.StateCallback sessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {

            mPreviewSession = session;
            try {
                mPreviewSession.setRepeatingRequest(captureRequestBuilder.build(), null, mCameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {


        }
    };


    /**
     * 拍照
     */
    public void capture(CaptureListener captureListener) {

        if (this.captureListener == null) {

            this.captureListener = captureListener;
        }

        try {

            //用CameraDevice创建一个CaptureRequest.Builder,类型为CameraDevice.TEMPLATE_STILL_CAPTURE，也就是说我们需要请求以个静态的图像。
            CaptureRequest.Builder captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START);
            //将imageReader的Surface设置为请求的目标Surface
            captureRequestBuilder.addTarget(imageReader.getSurface());
            // 获取手机方向
            int rotation = ((Activity)surfaceView.getContext()).getWindowManager().getDefaultDisplay().getRotation();
            // 根据设备方向计算设置照片的方向
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            CaptureRequest captureRequest = captureRequestBuilder.build();
            //停止预览
            mPreviewSession.stopRepeating();
            mPreviewSession.abortCaptures();
            //开始请求拍照
            mPreviewSession.capture(captureRequest, captureCallback, mCameraHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }


    /**
     * 拍照的回调
     */
    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

            //Toast.makeText(CameraActivity2.this, "saved", Toast.LENGTH_SHORT).show();
            Log.v("bbbbb", "saved");


            //重启预览
            createPreviewSession();

        }
    };


    private class ImageSaver implements Runnable {

        /**
         * The JPEG image
         */
        private final Image mImage;
        /**
         * The file we save the image into.
         */
        private Context c;


        ImageSaver(Image image, Context c) {

            mImage = image;
            this.c = c;

        }

        @Override
        public void run() {

            //创建根目录
            File appDir = new File(Environment.getExternalStorageDirectory() + "/isport");

            if (!appDir.exists()) {

                boolean wasSuccessful = appDir.mkdir();

                if (!wasSuccessful) {
                    System.out.println("创建根目录失败");
                }

            }

            String fileName = System.currentTimeMillis() + ".jpg";

            //图像文件
            File file = new File(appDir, fileName);

            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {

                output = new FileOutputStream(file);
                output.write(bytes);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                mImage.close();

                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (file == null) {

                Log.v("aaaaa","保存失败");
                return;
            }

            captureListener.onFinish(file);

            // 最后通知图库更新
//            c.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
//                    Uri.fromFile(file)));

        }

    }

    /**
     * 拍照保存回调
     */
    private ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {

            mCameraHandler.post(new ImageSaver(reader.acquireNextImage(), surfaceView.getContext()));

        }
    };


    private Size getOptimalSize(Size[] sizes, int currentWidth, int currentHeight) {

        int i = 1;
        //大头
        float bestWidth = (float) sizes[0].getWidth();
        //小头
        float bestHeight = (float) sizes[0].getHeight();

         /*
         很重要，第一项一定是高/宽
         */
        float min = Math.abs(bestHeight / bestWidth - (float) currentWidth / (float) currentHeight);

        while (i < sizes.length) {


            float current = Math.abs((float) sizes[i].getHeight() / (float) sizes[i].getWidth() - (float) currentWidth / (float) currentHeight);

            if (current < min) {

                min = current;
                bestWidth = (float) sizes[i].getWidth();
                bestHeight = (float) sizes[i].getHeight();

            }

            i++;

        }

        Log.v("aaaaa", (int) bestWidth + "////" + (int) bestHeight);
        return new Size((int) bestWidth, (int) bestHeight);

    }


    private File file;

    private void setUpMediaRecorder() {

        mMediaRecorder = new MediaRecorder();
        //创建根目录
        File appDir = new File(Environment.getExternalStorageDirectory(),
                "isport");

        if (!appDir.exists()) {

            boolean wasSuccessful = appDir.mkdir();

            if (!wasSuccessful) {
                System.out.println("创建根目录失败");
            }

        }

        String fileName = "/" + System.currentTimeMillis() + ".mp4";
        file = new File(appDir, fileName);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        mMediaRecorder.setOutputFile(appDir.getAbsolutePath() + fileName);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);


        mMediaRecorder.setOrientationHint(90);
        mMediaRecorder.setVideoSize(previewSize.getWidth(), previewSize.getHeight());

        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {

            mMediaRecorder.prepare();


        } catch (IOException e) {

            Log.v("aaaaa", e.getMessage());

            e.printStackTrace();

        }

    }

    /**
     * 开始录制
     */

    public void startRecord() {

        setUpMediaRecorder();
        createRecordSession();

        if (mMediaRecorder != null) {

            mCameraHandler.post(new Runnable() {
                @Override
                public void run() {

                    mMediaRecorder.start();
                    isRecording = true;

                }
            });


        }

    }

    public void stopRecord(RecordCallback recordCallback) {

        if (mMediaRecorder != null && file.exists()) {

            // Stop recording

            mMediaRecorder.stop();
            mMediaRecorder.release();

            recordCallback.onFinish(file);

            //回到拍照session
            createPreviewSession();

            // 最后通知图库更新
//            c.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
//                    Uri.fromFile(file)));


        } else {

            Log.v("aaaaa","录制失败");
        }

    }


    public void releaseAll() {

        if (null != mPreviewSession) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

        if (null != mMediaRecorder) {

            mMediaRecorder.release();
            mMediaRecorder = null;
        }


        /*
         关闭子线程
         */
        mHandlerThread.quitSafely();
        try {
            mHandlerThread.join();
            mHandlerThread = null;
            mCameraHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }




}
