#extension GL_OES_EGL_image_external : require

precision mediump float;
varying mediump vec2 vTextureCoord;
uniform samplerExternalOES inputImageTexture;
uniform vec2 singleStepOffset;
uniform mediump float params;
const highp vec3 W = vec3(0.299,0.587,0.114);
vec2 blurCoordinates[20];

float hardLight(float color)
{
	if(color <= 0.5)
		color = color * color * 2.0;
	else
		color = 1.0 - ((1.0 - color)*(1.0 - color) * 2.0);
	return color;
}

void main(){

    vec3 centralColor = texture2D(inputImageTexture, vTextureCoord).rgb;
    blurCoordinates[0] = vTextureCoord.xy + singleStepOffset * vec2(0.0, -10.0);
    blurCoordinates[1] = vTextureCoord.xy + singleStepOffset * vec2(0.0, 10.0);
    blurCoordinates[2] = vTextureCoord.xy + singleStepOffset * vec2(-10.0, 0.0);
    blurCoordinates[3] = vTextureCoord.xy + singleStepOffset * vec2(10.0, 0.0);
    blurCoordinates[4] = vTextureCoord.xy + singleStepOffset * vec2(5.0, -8.0);
    blurCoordinates[5] = vTextureCoord.xy + singleStepOffset * vec2(5.0, 8.0);
    blurCoordinates[6] = vTextureCoord.xy + singleStepOffset * vec2(-5.0, 8.0);
    blurCoordinates[7] = vTextureCoord.xy + singleStepOffset * vec2(-5.0, -8.0);
    blurCoordinates[8] = vTextureCoord.xy + singleStepOffset * vec2(8.0, -5.0);
    blurCoordinates[9] = vTextureCoord.xy + singleStepOffset * vec2(8.0, 5.0);
    blurCoordinates[10] = vTextureCoord.xy + singleStepOffset * vec2(-8.0, 5.0);
    blurCoordinates[11] = vTextureCoord.xy + singleStepOffset * vec2(-8.0, -5.0);
    blurCoordinates[12] = vTextureCoord.xy + singleStepOffset * vec2(0.0, -6.0);
    blurCoordinates[13] = vTextureCoord.xy + singleStepOffset * vec2(0.0, 6.0);
    blurCoordinates[14] = vTextureCoord.xy + singleStepOffset * vec2(6.0, 0.0);
    blurCoordinates[15] = vTextureCoord.xy + singleStepOffset * vec2(-6.0, 0.0);
    blurCoordinates[16] = vTextureCoord.xy + singleStepOffset * vec2(-4.0, -4.0);
    blurCoordinates[17] = vTextureCoord.xy + singleStepOffset * vec2(-4.0, 4.0);
    blurCoordinates[18] = vTextureCoord.xy + singleStepOffset * vec2(4.0, -4.0);
    blurCoordinates[19] = vTextureCoord.xy + singleStepOffset * vec2(4.0, 4.0);

    float sampleColor = centralColor.g * 20.0;
    sampleColor += texture2D(inputImageTexture, blurCoordinates[0]).g;
    sampleColor += texture2D(inputImageTexture, blurCoordinates[1]).g;
    sampleColor += texture2D(inputImageTexture, blurCoordinates[2]).g;
    sampleColor += texture2D(inputImageTexture, blurCoordinates[3]).g;
    sampleColor += texture2D(inputImageTexture, blurCoordinates[4]).g;
    sampleColor += texture2D(inputImageTexture, blurCoordinates[5]).g;
    sampleColor += texture2D(inputImageTexture, blurCoordinates[6]).g;
    sampleColor += texture2D(inputImageTexture, blurCoordinates[7]).g;
    sampleColor += texture2D(inputImageTexture, blurCoordinates[8]).g;
    sampleColor += texture2D(inputImageTexture, blurCoordinates[9]).g;
    sampleColor += texture2D(inputImageTexture, blurCoordinates[10]).g;
    sampleColor += texture2D(inputImageTexture, blurCoordinates[11]).g;
    sampleColor += texture2D(inputImageTexture, blurCoordinates[12]).g * 2.0;
    sampleColor += texture2D(inputImageTexture, blurCoordinates[13]).g * 2.0;
    sampleColor += texture2D(inputImageTexture, blurCoordinates[14]).g * 2.0;
    sampleColor += texture2D(inputImageTexture, blurCoordinates[15]).g * 2.0;
    sampleColor += texture2D(inputImageTexture, blurCoordinates[16]).g * 2.0;
    sampleColor += texture2D(inputImageTexture, blurCoordinates[17]).g * 2.0;
    sampleColor += texture2D(inputImageTexture, blurCoordinates[18]).g * 2.0;
    sampleColor += texture2D(inputImageTexture, blurCoordinates[19]).g * 2.0;

    sampleColor = sampleColor / 48.0;

    float highPass = centralColor.g - sampleColor + 0.5;

    for(int i = 0; i < 5;i++)
    {
        highPass = hardLight(highPass);
    }
    float luminance = dot(centralColor, W);

    float alpha = pow(luminance, params);

    vec3 smoothColor = centralColor + (centralColor-vec3(highPass))*alpha*0.1;

    gl_FragColor = vec4(mix(smoothColor.rgb, max(smoothColor, centralColor), alpha), 1.0);
}