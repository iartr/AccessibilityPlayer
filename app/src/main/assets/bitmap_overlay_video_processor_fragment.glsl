#extension GL_OES_EGL_image_external : require
precision mediump float;
// External texture containing video decoder output.
uniform samplerExternalOES uTexSampler0;
// Texture containing the overlap bitmap.
uniform sampler2D uTexSampler1;
// Horizontal scaling factor for the overlap bitmap.
uniform float uScaleX;
// Vertical scaling factory for the overlap bitmap.
uniform float uScaleY;
varying vec2 vTexCoords;
void main() {
  vec4 videoColor = texture2D(uTexSampler0, vTexCoords);
  vec4 overlayColor = texture2D(uTexSampler1,
                                vec2(vTexCoords.x * uScaleX,
                                     vTexCoords.y * uScaleY));
  // Blend the video decoder output and the overlay bitmap.
  gl_FragColor = videoColor * (1.0 - overlayColor.a)
      + overlayColor * overlayColor.a;
}