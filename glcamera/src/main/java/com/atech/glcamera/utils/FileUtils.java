package com.atech.glcamera.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;

public class FileUtils {


    public static File createVideoFile() {

        File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "glcamera");

        if (!dir.exists()) {
            boolean isSuccess = dir.mkdirs();
            Log.i("glcamera", "文件夹创建状态--->" + isSuccess);
        }


        return new File(dir.getPath() + File.separator + "img_" + System.currentTimeMillis() + ".mp4");

    }


    public static File createImageFile() {

        File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "glcamera");

        if (!dir.exists()) {
            boolean isSuccess = dir.mkdirs();
            Log.i("glcamera", "文件夹创建状态--->" + isSuccess);
        }
        return new File(dir.getPath() + File.separator + "img_" + System.currentTimeMillis() + ".jpg");

    }


}
