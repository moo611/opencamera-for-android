package com.atech.glcamera.filters;

import android.content.Context;
import android.opengl.GLES20;

import com.atech.glcamera.R;
import com.atech.glcamera.utils.OpenGlUtils;


public class BrannanFilter extends BaseFilter{
	private int[] inputTextureHandles = {-1,-1,-1,-1,-1};
	private int[] inputTextureUniformLocations = {-1,-1,-1,-1,-1};
    private int mGLStrengthLocation;

	public BrannanFilter(Context c) {
		super(c);
	}

	@Override
	public void setPath() {

		path1 = R.raw.base_vertex_shader;
		path2 = R.raw.brannan;
		
	}
	

	@Override
	public void createProgram() {
		super.createProgram();

		for(int i=0; i < inputTextureUniformLocations.length; i++)
			inputTextureUniformLocations[i] = GLES20.glGetUniformLocation(mProgram, "inputImageTexture"+(2+i));
		mGLStrengthLocation = GLES20.glGetUniformLocation(mProgram,
				"strength");

		setFloat(mGLStrengthLocation, 1.0f);
		inputTextureHandles[0] = OpenGlUtils.loadTexture(c, "filter/brannan_process.png");
		inputTextureHandles[1] = OpenGlUtils.loadTexture(c, "filter/brannan_blowout.png");
		inputTextureHandles[2] = OpenGlUtils.loadTexture(c, "filter/brannan_contrast.png");
		inputTextureHandles[3] = OpenGlUtils.loadTexture(c, "filter/brannan_luma.png");
		inputTextureHandles[4] = OpenGlUtils.loadTexture(c, "filter/brannan_screen.png");
		
	}


	@Override
	public void releaseProgram() {
		super.releaseProgram();
		GLES20.glDeleteTextures(inputTextureHandles.length, inputTextureHandles, 0);
		for(int i = 0; i < inputTextureHandles.length; i++)
			inputTextureHandles[i] = -1;

	}

	protected void onDrawArraysAfter(){
		for(int i = 0; i < inputTextureHandles.length
				&& inputTextureHandles[i] != OpenGlUtils.NO_TEXTURE; i++){
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + (i+3));
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		}
	}

	

	protected void onDrawArraysPre(){
		for(int i = 0; i < inputTextureHandles.length 
				&& inputTextureHandles[i] != OpenGlUtils.NO_TEXTURE; i++){
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + (i+3) );
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, inputTextureHandles[i]);
			GLES20.glUniform1i(inputTextureUniformLocations[i], (i+3));
		}
	}
	

}
