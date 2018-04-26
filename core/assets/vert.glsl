//const
#define pi 3.1415926535897932384626433832795

//attributes
attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

//input
uniform mat4 u_projectionViewMatrix;
uniform float frequency;
uniform float time;

uniform float x_sway;
uniform float y_sway;
uniform float z_sway;
 
//output
varying vec4 v_color;
varying vec2 v_texCoords;

void main() {
	float phase = 2*pi*frequency*time;
	
	vec3 movement = vec3(
	x_sway * (1.0 - a_texCoord0.y) * sin(a_position.y + phase),
	y_sway * sin(a_position.x + 1.0 * a_position.z) * (1.0 - a_texCoord0.y),
	z_sway * (cos(time) * (1.0 - a_texCoord0.y)) * sin(a_position.z + phase));
	
	vec4 grass_movement = vec4(
	a_position.x + movement.x, 
	a_position.y + movement.y,
	a_position.z + movement.z + movement.x/5.0 + movement.y/2.0,
	1.0);
	
	v_color = a_color;
	v_texCoords = a_texCoord0;
	gl_Position = u_projectionViewMatrix * grass_movement;
}