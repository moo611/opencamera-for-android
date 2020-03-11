attribute vec4 aPosition;
uniform mat4 uTextureMatrix;
attribute vec2 aTextureCoordinate;
 
varying vec2 textureCoordinate;
 
void main()
{
    gl_Position = aPosition;
    textureCoordinate = (uTextureMatrix * vec4(aTextureCoordinate, 0, 1.0)).xy;;
}
