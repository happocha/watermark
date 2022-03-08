#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES usTextureOes;
varying vec2 vvTextureCoordinate;
void main() {
    vec4 vCameraColor = texture2D(usTextureOes, vvTextureCoordinate);
    gl_FragColor = vec4(vCameraColor.r, vCameraColor.g, vCameraColor.b, 1.0);
}
