package com.atech.glcamera.filters;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.atech.glcamera.R;
import com.atech.glcamera.grafika.gles.GlUtil;
import com.atech.glcamera.utils.OpenGlUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;

public abstract class BaseFilter {

    /*********************************************************************************************
     * 网上有很多关于坐标的文章，说法不一，容易造成混乱，这里和google工程师写的grafika项目保持一致
     *********************************************************************************************/
    private static float squareCoords[] = {

            -1.0f, -1.0f,   // 0 bottom left
            1.0f, -1.0f,   // 1 bottom right
            -1.0f, 1.0f,   // 2 top left
            1.0f, 1.0f,   // 3 top right
    };

    private static float textureVertices[] = {

            0.0f, 0.0f,     // 0 bottom left
            1.0f, 0.0f,     // 1 bottom right
            0.0f, 1.0f,     // 2 top left
            1.0f, 1.0f      // 3 top right

    };

    public Context c;
    public int mProgram;
    public FloatBuffer vertexBuffer;
    public FloatBuffer textureBuffer;
    public int path1;
    public int path2;
    protected int mGLAttribPosition;
    protected int mGLUniformTexture;
    protected int mGLAttribTextureCoordinate;
    protected int mHMatrix;

    public BaseFilter(Context c) {

        this.c = c;
        vertexBuffer = createBuffer(squareCoords);
        textureBuffer = createBuffer(textureVertices);
        setPath();
        mRunOnDraw = new LinkedList<>();
    }

    public abstract void setPath();

    private FloatBuffer createBuffer(float[] vertexData) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertexData.length * 4);//要求用allocateDirect()方法,只有ByteBuffer有该方法,so
        byteBuffer.order(ByteOrder.nativeOrder());          //要求nativeOrder  Java 是大端字节序(BigEdian)，
        // 而 OpenGL 所需要的数据是小端字节序(LittleEdian)
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        floatBuffer.put(vertexData);
        floatBuffer.position(0);
        return floatBuffer;
    }


    /**
     * 创建绘制脚本程序
     */
    public void createProgram() {

        String vertexShaderCode = readRawTextFile(c, path1);
        String fragmentShaderCode = readRawTextFile(c, path2);
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // creates OpenGL ES program executables

        if (mProgram == 0) {
            throw new RuntimeException("Unable to create program");
        }
        Log.v("aaaaa", "program created");


        //共用句柄
        mGLAttribPosition = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mGLUniformTexture = GLES20.glGetUniformLocation(mProgram, "inputImageTexture");
        mGLAttribTextureCoordinate = GLES20.glGetAttribLocation(mProgram,
                "aTextureCoordinate");
        mHMatrix = GLES20.glGetUniformLocation(mProgram, "uTextureMatrix");
    }

    /**
     * 开始绘制
     */

    public void draw(int textureId, float[] metrix) {

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);
        runPendingOnDrawTasks();


        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);
        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);

        if (textureId != OpenGlUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
            GLES20.glUniform1i(mGLUniformTexture, 0);
        }


        GLES20.glUniformMatrix4fv(mHMatrix, 1, false, metrix, 0);

        onDrawArraysPre();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mGLAttribPosition);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);
        onDrawArraysAfter();
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glUseProgram(0);

    }

    ;


    protected abstract void onDrawArraysPre();

    protected abstract void onDrawArraysAfter();

    public void releaseProgram() {

        Log.v("aaaaa", "deleting program " + mProgram);
        GLES20.glDeleteProgram(mProgram);
        mProgram = 0;

    }

    /**
     * 注意此处一定要用runondraw,因为要在useprogram之后执行
     *
     * @param location
     * @param floatValue
     */

    protected void setFloat(final int location, final float floatValue) {

        runOnDraw(new Runnable() {
            @Override
            public void run() {

                GLES20.glUniform1f(location, floatValue);
            }
        });


    }

    protected void setFloatVec2(final int location, final float[] arrayValue) {

        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform2fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });


    }


    protected void setFloatVec4(final int location, final float[] arrayValue) {

        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform4fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });

    }


    protected void setInteger(final int location, final int intValue) {

        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1i(location, intValue);
            }
        });

    }

    public void onInputSizeChanged(final int width, final int height) {

    }

    private int loadShader(int type, String shaderCode) {

        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }


    private String readRawTextFile(Context context, int rawId) {

        InputStream is = context.getResources().openRawResource(rawId);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuilder sb = new StringBuilder();

        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();

    }

    /**
     * 绑定纹理
     *
     * @return
     */

    public static int bindTexture() {
        int[] texture = new int[1];

        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);

        return texture[0];
    }

    private final LinkedList<Runnable> mRunOnDraw;

    protected void runPendingOnDrawTasks() {
        while (!mRunOnDraw.isEmpty()) {
            mRunOnDraw.removeFirst().run();
        }
    }

    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.addLast(runnable);
        }
    }
}
