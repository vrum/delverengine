#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;
uniform vec4 u_FogColor;

uniform float u_time;
uniform float u_tex_width;
uniform float u_tex_height;
uniform int u_sprite_columns;
uniform int u_sprite_rows;

varying vec4 v_color;
varying vec2 v_texCoords;
varying float v_fogFactor;
varying float v_eyeDistance;

vec4 tex_Color;

void main() {
  vec4 color;

  tex_Color = texture2D( u_texture, v_texCoords );
  color = v_color * tex_Color;
  if(tex_Color.a < 0.01) discard;

  gl_FragColor = mix(u_FogColor, color, v_fogFactor);
}