#version 100

precision highp float;
varying vec2 v_texcoord;
uniform lowp sampler2D s_textureY;
uniform lowp sampler2D s_textureU;
uniform lowp sampler2D s_textureV;
uniform vec2 texSize;
vec4 YuvToRgb(vec2 uv) {
    float y, u, v, r, g, b;
    y = texture2D(s_textureY, uv).r;
    u = texture2D(s_textureU, uv).r;
    v = texture2D(s_textureV, uv).r;
    u = u - 0.5;
    v = v - 0.5;
    r = y + 1.403 * v;
    g = y - 0.344 * u - 0.714 * v;
    b = y + 1.770 * u;
    return vec4(r, g, b, 1.0);
}
void main() {
    vec2 tileNum = vec2(40.0, 20.0);
    vec2 uv = v_texcoord;
    vec2 uv2 = floor(uv * tileNum) / tileNum;
    uv -= uv2;
    uv *= tileNum;
    vec3 color = YuvToRgb(uv2 + vec2(step(1.0 - uv.y, uv.x) / (2.0 * tileNum.x),
    step(uv.x, uv.y) / (2.0 * tileNum.y))).rgb;
    gl_FragColor = vec4(color, 1.0);
}