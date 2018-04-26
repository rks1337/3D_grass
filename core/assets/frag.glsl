#ifdef GL_ES
#define LOWP lowp
    precision mediump float;
#else
    #define LOWP
#endif

//input
uniform sampler2D u_sampler2D;

//output
varying LOWP vec4 v_color;
varying vec2 v_texCoords;

void main() {
	gl_FragColor =  texture2D(u_sampler2D, v_texCoords) * v_color;
}