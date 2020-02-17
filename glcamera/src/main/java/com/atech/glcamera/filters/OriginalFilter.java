package com.atech.glcamera.filters;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.atech.glcamera.R;
import com.atech.glcamera.grafika.gles.GlUtil;

public class OriginalFilter extends BaseFilter {

    public OriginalFilter(Context c) {
        super(c);

    }

    @Override
    public void setPath() {

        path1 = R.raw.base_vertex_shader;
        path2 = R.raw.base_fragment_shader;

    }


    @Override
    public void draw(int textureId, float[] metrix) {

        /**
         * 先清空画面
         */
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);
        GlUtil.checkGlError("glUseProgram");

        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        int mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoordinate");
        int uTextureSamplerLocation = GLES20.glGetUniformLocation(mProgram, "uTextureSampler");
        int mHMatrix = GLES20.glGetUniformLocation(mProgram, "uTextureMatrix");


        Log.v("abcde","program:"+mProgram);
        /**
         * 在程序中加入顶点坐标，strid 0表示紧密相连，等同于4*2
         */

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GlUtil.checkGlError("glEnableVertexAttribArray");
        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer);
        GlUtil.checkGlError("glVertexAttribPointer");
        /**
         * 在程序中加入纹理坐标
         */

        GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
        GlUtil.checkGlError("glEnableVertexAttribArray");
        GLES20.glVertexAttribPointer(mTextureCoordHandle, 2, GLES20.GL_FLOAT, false, 8, textureBuffer);
        GlUtil.checkGlError("glVertexAttribPointer");

        /**
         * 纹理变换矩阵
         */

        GLES20.glUniformMatrix4fv(mHMatrix, 1, false, metrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");
        /**
         * 绑定纹理
         */

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniform1i(uTextureSamplerLocation, 0);
        GlUtil.checkGlError("glUniform1i");
        /**
         * 绘制
         */
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GlUtil.checkGlError("glDrawArrays");
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glUseProgram(0);

    }
}
