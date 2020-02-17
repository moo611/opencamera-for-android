attribute vec4 aPosition;
uniform mat4 uTextureMatrix;
attribute vec4 aTextureCoordinate;
varying vec2 vTextureCoord;

void main()
{
  gl_Position =  aPosition;

  vTextureCoord = (uTextureMatrix * aTextureCoordinate).xy;

}