package com.atech.glcamera.filters;

import android.content.Context;
import android.opengl.GLES20;

import com.atech.glcamera.R;
import com.atech.glcamera.utils.OpenGlUtils;


public class AmaroFilter extends BaseFilter {
    private int[] inputTextureHandles = {-1, -1, -1};
    private int[] inputTextureUniformLocations = {-1, -1, -1};
    private int mGLStrengthLocation;

    public AmaroFilter(Context c) {
        super(c);
    }

    @Override
    public void setPath() {
        path1 = R.raw.base_vertex_shader;
        path2 = R.raw.amaro;
    }

    @Override
    public void createProgram() {
        super.createProgram();

        GLES20.glDeleteTextures(inputTextureHandles.length, inputTextureHandles, 0);
        for (int i = 0; i < inputTextureHandles.length; i++)
            inputTextureHandles[i] = -1;

        for (int i = 0; i < inputTextureUniformLocations.length; i++)
            inputTextureUniformLocations[i] = GLES20.glGetUniformLocation(mProgram, "inputImageTexture" + (2 + i));
        mGLStrengthLocation = GLES20.glGetUniformLocation(mProgram,
                "strength");

        setFloat(mGLStrengthLocation, 1.0f);
        runOnDraw(new Runnable() {
            @Override
            public void run() {

                inputTextureHandles[0] = OpenGlUtils.loadTexture(c, "filter/brannan_blowout.png");
                inputTextureHandles[1] = OpenGlUtils.loadTexture(c, "filter/overlaymap.png");
                inputTextureHandles[2] = OpenGlUtils.loadTexture(c, "filter/amaromap.png");
            }
        });


    }


    protected void onDrawArraysAfter() {
        for (int i = 0; i < inputTextureHandles.length
                && inputTextureHandles[i] != OpenGlUtils.NO_TEXTURE; i++) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + (i + 3));
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        }
    }


    protected void onDrawArraysPre() {

        for (int i = 0; i < inputTextureHandles.length
                && inputTextureHandles[i] != OpenGlUtils.NO_TEXTURE; i++) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + (i + 3));
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, inputTextureHandles[i]);
            GLES20.glUniform1i(inputTextureUniformLocations[i], (i + 3));
        }

    }

}
