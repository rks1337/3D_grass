package grass.simulator;

import java.util.Comparator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalMaterial;
import com.badlogic.gdx.graphics.g3d.decals.GroupStrategy;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;

public class CameraGrassStrategy implements GroupStrategy, Disposable {
	private static final int GROUP_OPAQUE = 0;
	private static final int GROUP_BLEND = 1;

	private float timer;
	private float x_sway;
	private float y_sway;
	private float z_sway;

	Pool<Array<Decal>> arrayPool = new Pool<Array<Decal>>(16) {
		protected Array<Decal> newObject () {
			return new Array<Decal>();
		}
	};
	Array<Array<Decal>> usedArrays = new Array<Array<Decal>>();
	ObjectMap<DecalMaterial, Array<Decal>> materialGroups = new ObjectMap<DecalMaterial, Array<Decal>>();

	Camera camera;
	ShaderProgram shader;
	private final Comparator<Decal> cameraSorter;

	public CameraGrassStrategy(final Camera camera) {
		this(camera, new Comparator<Decal>() {
			public int compare (Decal o1, Decal o2) {
				float dist1 = camera.position.dst(o1.getPosition());
				float dist2 = camera.position.dst(o2.getPosition());
				return (int)Math.signum(dist2 - dist1);
			}
		});
	}

	public CameraGrassStrategy(Camera camera, Comparator<Decal> sorter) {
		this.camera = camera;
		this.cameraSorter = sorter;
		createDefaultShader();

	}

	public void setCamera (Camera camera) {
		this.camera = camera;
	}

	public Camera getCamera () {
		return camera;
	}

	public int decideGroup (Decal decal) {
		return decal.getMaterial().isOpaque() ? GROUP_OPAQUE : GROUP_BLEND;
	}

	public void beforeGroup (int group, Array<Decal> contents) {
		if (group == GROUP_BLEND) {
			Gdx.gl.glEnable(GL20.GL_BLEND);
			contents.sort(cameraSorter);
		} else {
			for (int i = 0, n = contents.size; i < n; i++) {
				Decal decal = contents.get(i);
				Array<Decal> materialGroup = materialGroups.get(decal.getMaterial());
				if (materialGroup == null) {
					materialGroup = arrayPool.obtain();
					materialGroup.clear();
					usedArrays.add(materialGroup);
					materialGroups.put(decal.getMaterial(), materialGroup);
				}
				materialGroup.add(decal);
			}

			contents.clear();
			for (Array<Decal> materialGroup : materialGroups.values()) {
				contents.addAll(materialGroup);
			}

			materialGroups.clear();
			arrayPool.freeAll(usedArrays);
			usedArrays.clear();
		}
	}

	public void afterGroup (int group) {
		if (group == GROUP_BLEND) {
			Gdx.gl.glDisable(GL20.GL_BLEND);
		}
	}

	public void beforeGroups () {
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		shader.begin();
		shader.setUniformMatrix("u_projectionViewMatrix", camera.combined);
		shader.setUniformi("u_sampler2D", 0);

		//adjust phase
		shader.setUniformf("frequency", .2f);
		shader.setUniformf("time", timer+=Gdx.graphics.getDeltaTime());

		//adjust wind
		shader.setUniformf("x_sway", x_sway);
		shader.setUniformf("y_sway", y_sway);
		shader.setUniformf("z_sway", z_sway);
	}

	public void adjust_wind(float x, float y, float z) {
		this.x_sway = x;
		this.y_sway = y;
		this.z_sway = z;
	}
	
	public float get_x_sway() { return x_sway; }
	public float get_y_sway() { return y_sway; }
	public float get_z_sway() { return z_sway; }

	public void afterGroups () {
		shader.end();
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
	}

	private void createDefaultShader () {
		String vertexShader = Gdx.files.internal("vert.glsl").readString();
		String  fragmentShader = Gdx.files.internal("frag.glsl").readString();
		shader = new ShaderProgram(vertexShader, fragmentShader);
		ShaderProgram.pedantic = false;
		if (shader.isCompiled() == false) throw new IllegalArgumentException("couldn't compile shader: " + shader.getLog());
	}

	public ShaderProgram getGroupShader (int group) {
		return shader;
	}

	public void dispose () {
		if (shader != null) shader.dispose();
	}
}