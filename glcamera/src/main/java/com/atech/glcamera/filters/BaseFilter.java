package com.atech.glcamera.filters;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.atech.glcamera.R;
import com.atech.glcamera.grafika.gles.GlUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public abstract class BaseFilter {

    /*********************************************************************************************
     * 网上有很多关于坐标的文章，说法不一，容易造成混乱，这里和google工程师写的grafika项目保持一致
     *********************************************************************************************/
    private static float squareCoords[] = {

            -1.0f, -1.0f,   // 0 bottom left
            1.0f, -1.0f,   // 1 bottom right
            -1.0f,  1.0f,   // 2 top left
            1.0f,  1.0f,   // 3 top right
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


    public BaseFilter(Context c){

        this.c = c;
        vertexBuffer = createBuffer(squareCoords);
        textureBuffer = createBuffer(textureVertices);
        setPath();
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
        if(vertexShader == 0)
        {
            Log.e("ES20_ERROR", "加载顶点着色器失败");
            return;
        }
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        if(fragmentShader == 0)
        {
            Log.e("ES20_ERROR", "加载顶点着色器失败");
            return ;
        }

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GlUtil.checkGlError("glAttachShader1");
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GlUtil.checkGlError("glAttachShader2");
        GLES20.glLinkProgram(mProgram);                  // creates OpenGL ES program executables
        GlUtil.checkGlError("glLinkProgram");
        if (mProgram == 0) {
            throw new RuntimeException("Unable to create program");
        }
        Log.v("aaaaa","program created");


        //共用句柄
        mGLAttribPosition = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mGLUniformTexture = GLES20.glGetUniformLocation(mProgram, "inputImageTexture");
        mGLAttribTextureCoordinate = GLES20.glGetAttribLocation(mProgram,
                "aTextureCoordinate");

    }

    /**
     * 开始绘制
     */

    public abstract void draw(int textureId,float[]metrix);

    private int loadShader(int type, String shaderCode) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
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

    public void releaseProgram(){

        Log.v("aaaaa", "deleting program " + mProgram);
        GLES20.glDeleteProgram(mProgram);
        mProgram = 0;

    }

    protected void setFloat(final int location, final float floatValue) {

        GLES20.glUniform1f(location, floatValue);

    }

    protected void setFloatVec2(final int location, final float[] arrayValue) {

        GLES20.glUniform2fv(location, 1, FloatBuffer.wrap(arrayValue));

    }

    public void onInputSizeChanged(final int width, final int height){

    }

}
