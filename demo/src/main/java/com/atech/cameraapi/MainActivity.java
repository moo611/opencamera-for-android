package com.atech.cameraapi;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int CameraPermision = 100;

    private int read;
    private int write;
    private int camera;
    private int record;
    private Button btn1;
    private Button btn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);


    }


    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        // this device has a camera
// no camera on this device
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);

    }

    private boolean checkPemission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            read = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            write = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            camera = checkSelfPermission(Manifest.permission.CAMERA);
            record = checkSelfPermission(Manifest.permission.RECORD_AUDIO);

            if (read != PackageManager.PERMISSION_GRANTED
                    || write != PackageManager.PERMISSION_GRANTED
                    || camera != PackageManager.PERMISSION_GRANTED
                    || record != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO

                        },
                        CameraPermision);


                return false;

            }

        }

        return true;

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CameraPermision) {

            for (int i : grantResults) {

                if (i != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

            }

            Toast.makeText(MainActivity.this, "permission allowed", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){


            case R.id.btn1:

                if (checkPemission()&&checkCameraHardware(MainActivity.this)) {

                    startActivity(new Intent(MainActivity.this, CameraActivity.class));

                } else {

                    Toast.makeText(MainActivity.this, "permission request", Toast.LENGTH_SHORT).show();

                }


                break;

            case R.id.btn2:

                if (checkPemission()&&checkCameraHardware(MainActivity.this)) {

                    startActivity(new Intent(MainActivity.this, GLCameraActivity.class));

                } else {

                    Toast.makeText(MainActivity.this, "permission request", Toast.LENGTH_SHORT).show();

                }

                break;

        }

    }
}
