package com.atech.glcamera.filters;

import android.content.Context;
import android.opengl.GLES20;

import com.atech.glcamera.R;

public class BeautyFilter extends BaseFilter {

    private int widthHandle;
    private int heightHandle;

    private int levelHandle;
    private float opacity;//0-1
    private int width, height;

    public BeautyFilter(Context c) {
        super(c);

        setSmoothOpacity(0.5f);//默认

    }

    @Override
    public void setPath() {

        path1 = R.raw.beauty_vertex;
        path2 = R.raw.beauty;

    }

    @Override
    public void createProgram() {
        super.createProgram();

        mGLUniformTexture = GLES20.glGetUniformLocation(mProgram, "inputTexture");
        widthHandle = GLES20.glGetUniformLocation(mProgram,"width");
        heightHandle = GLES20.glGetUniformLocation(mProgram,"height");
        levelHandle = GLES20.glGetUniformLocation(mProgram,"opacity");
    }


    @Override
    public void onInputSizeChanged(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    protected void onDrawArraysPre() {

        setInteger(widthHandle,width);
        setInteger(heightHandle,height);
        setFloat(levelHandle,opacity);


    }

    @Override
    protected void onDrawArraysAfter() {

    }


    /**
     * 设置磨皮程度
     * @param percent 百分比
     */
    public void setSmoothOpacity(float percent) {

        if (percent <= 0) {
            opacity = 0.0f;
        } else {
            opacity = calculateOpacity(percent);
        }

    }

    /**
     * 根据百分比计算出实际的磨皮程度
     * @param percent
     * @return
     */
    private float calculateOpacity(float percent) {
        float result;

        result = (float) (1.0f - (1.0f - percent + 0.02) / 2.0f);

        return result;
    }


}
