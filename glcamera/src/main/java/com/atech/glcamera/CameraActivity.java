package com.atech.glcamera;

import static com.atech.glcamera.render.ByteFlowRender.IMAGE_FORMAT_I420;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraCharacteristics;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Size;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.atech.glcamera.camera.Camera2FrameCallback;
import com.atech.glcamera.camera.Camera2Wrapper;
import com.atech.glcamera.camera.CameraUtil;
import com.atech.glcamera.render.GLByteFlowRender;

public class CameraActivity extends AppCompatActivity implements Camera2FrameCallback {
    private static final String[] REQUEST_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    GLSurfaceView mGLSurfaceView;
    Camera2Wrapper mCamera2Wrapper;
    GLByteFlowRender mByteFlowRender;
    Size mRootViewSize, mScreenSize;
    RelativeLayout rootView;

    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        rootView = findViewById(R.id.root_view);

        mGLSurfaceView = new GLSurfaceView(this);
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        rootView.addView(mGLSurfaceView, p);
        mByteFlowRender = new GLByteFlowRender();
        mByteFlowRender.init(mGLSurfaceView);
        mByteFlowRender.readRawTextFile(this, com.atech.glcamera.R.raw.base_fragment_shader);
        //注意先执行render后初始化相机
        mCamera2Wrapper = new Camera2Wrapper(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (hasPermissionsGranted(REQUEST_PERMISSIONS)) {
            mCamera2Wrapper.startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUEST_PERMISSIONS, CAMERA_PERMISSION_REQUEST_CODE);
        }
        updateTransformMatrix(mCamera2Wrapper.getCameraId());
        if (rootView != null) {
            updateGLSurfaceViewSize(mCamera2Wrapper.getPreviewSize());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (hasPermissionsGranted(REQUEST_PERMISSIONS)) {
            mCamera2Wrapper.stopCamera();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mByteFlowRender.unInit();

    }

    private void updateTransformMatrix(String cameraId) {
        if (Integer.valueOf(cameraId) == CameraCharacteristics.LENS_FACING_FRONT) {
            mByteFlowRender.setTransformMatrix(270, 0);
        } else {
            mByteFlowRender.setTransformMatrix(90, 1);
        }

    }

    public void updateGLSurfaceViewSize(Size previewSize) {
        Size fitSize = null;
        fitSize = CameraUtil.getFitInScreenSize(previewSize.getWidth(), previewSize.getHeight(), getScreenSize().getWidth(), getScreenSize().getHeight());
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mGLSurfaceView
                .getLayoutParams();
        params.width = fitSize.getWidth();
        params.height = fitSize.getHeight();
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP | RelativeLayout.CENTER_HORIZONTAL);

        mGLSurfaceView.setLayoutParams(params);
    }

    public Size getScreenSize() {
        if (mScreenSize == null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            mScreenSize = new Size(displayMetrics.widthPixels, displayMetrics.heightPixels);
        }
        return mScreenSize;
    }

    private boolean hasPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }





    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (hasPermissionsGranted(REQUEST_PERMISSIONS)) {
                mCamera2Wrapper.startCamera();
                updateTransformMatrix(mCamera2Wrapper.getCameraId());
            } else {
                Toast.makeText(this, "We need the camera permission.", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }


    @Override
    public void onPreviewFrame(byte[] data, int width, int height) {

        mByteFlowRender.setRenderFrame(IMAGE_FORMAT_I420, data, width, height);
        mByteFlowRender.requestRender();
    }

    @Override
    public void onCaptureFrame(byte[] data, int width, int height) {

    }
}