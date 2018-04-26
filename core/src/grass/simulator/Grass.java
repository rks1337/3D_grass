package grass.simulator;

import java.util.Vector;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Grass implements Screen {
	//private ImmediateModeRenderer20 lineRenderer = new ImmediateModeRenderer20(false, true, 0);
	private Preferences prefs = Gdx.app.getPreferences("GrassSimulator3000");
	private FirstPersonCameraController camera_control;
	private PerspectiveCamera camera_perspective;

	private Texture texture_Grass;
	private DecalBatch decalBatch;
	private CameraGrassStrategy grass_cam_strat;

	private Vector<TextureRegion> grass_regions;
	private Vector<Decal> decals;
	private int grass_incremenet = 0;

	private Stage stage;
	private Table table0;
	private Table table1;
	private BitmapFont font;
	private TextButton wind_button;
	private TextButton fps_button;
	private SpriteBatch spriteBatch;

	private int intensity = 0;

	@Override
	public void show() {
		//init
		decals = new Vector<Decal>();
		grass_regions = new Vector<TextureRegion>();
		spriteBatch  = new SpriteBatch();
		
		//camera
		camera_perspective = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera_perspective.near = 0.1f;
		camera_perspective.far = 100f;
		camera_perspective.position.set(11, 4, 27);
		camera_control = new FirstPersonCameraController(camera_perspective);
		camera_control.setVelocity(4);
		camera_control.setDegreesPerPixel(.15f);

		//wind adjustment
		InputProcessor input_0 = new GestureDetector(new Gesture() {
		}) {
			@Override
			public boolean scrolled(int amount) {
				if (amount == -1) {
					if (intensity < 15) {
						grass_cam_strat.adjust_wind(
								grass_cam_strat.get_x_sway()+0.05f,
								grass_cam_strat.get_y_sway()+0.01f,
								grass_cam_strat.get_z_sway()+0.05f);
						decalBatch.setGroupStrategy(grass_cam_strat);
						decalBatch.flush();
						intensity += 1;
					}
				}
				if (amount == 1) {
					if (intensity > 0) {
						grass_cam_strat.adjust_wind(
								grass_cam_strat.get_x_sway()-0.05f,
								grass_cam_strat.get_y_sway()-0.01f,
								grass_cam_strat.get_z_sway()-0.05f);
						decalBatch.setGroupStrategy(grass_cam_strat);
						decalBatch.flush();
						intensity -= 1;
					}
				}
				return super.scrolled(amount);
			}
		};
		
		//input control
		InputMultiplexer input_multi = new InputMultiplexer(camera_control);
		input_multi.addProcessor(input_0);
		Gdx.input.setInputProcessor(input_multi);

		//hud
		table0 = new Table();
		table0.setFillParent(true);
		table1 = new Table();
		table1.setFillParent(true);

		stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()), spriteBatch);
		stage.addActor(table0);
		stage.addActor(table1);

		//hud labels
		TextButtonStyle style = new TextButtonStyle();
		font = Assets.manager.get("galax_48.ttf", BitmapFont.class);
		style.font = font;

		wind_button = new TextButton("", style);
		wind_button.pad(15);

		fps_button = new TextButton("", style);
		fps_button.pad(15);

		table0.top().left().padLeft(25).padTop(10);
		table0.add(wind_button);
		table1.top().right().padRight(25).padTop(10);
		table1.add(fps_button);

		//set up grass textures
		if (prefs.getInteger("grass_type") == 0) {
			texture_Grass = Assets.manager.get(Assets.grass);
		}
		if (prefs.getInteger("grass_type") == 1) {
			texture_Grass = Assets.manager.get(Assets.grass2);
		}
		grass_regions.add(new TextureRegion(texture_Grass));

		//batch grass textures
		grass_cam_strat = new CameraGrassStrategy(camera_perspective);
		grass_cam_strat.adjust_wind(0, 0, 0);
		decalBatch = new DecalBatch(grass_cam_strat);
		decalBatch.setGroupStrategy(grass_cam_strat);

		//set patch thickness (space between grass decals)
		float thickness = 0;
		if (prefs.getInteger("grass_type") == 0) {
			thickness = 4.5f;
		} else {
			thickness = MathUtils.random(2f, 3f);
		}

		for(int i = 0; i < 30000; i++){
			decals.add(Decal.newDecal(0.7f, .9f, grass_regions.get(0), true));
		}

		//grid
		int x_ = 100, y_ = 1, z_ = 100;
		int Max = x_*y_*z_;
		for(int p = 0; p < Max; p++){
			int z = p / (x_ * y_);
			int x = (p - z * x_ * y_) % x_;
			//System.out.println("x: " + x + " y:" + y_ + " z:" + z);
			decals.get(grass_incremenet).setPosition(x/thickness, 0, z/thickness);
			decals.get(grass_incremenet).setRotation(MathUtils.random(1) + 60, 0, 0);
			grass_incremenet+=1;	
		}
		for(int p = 0; p < Max; p++){
			int z = p / (x_ * y_);
			int x = (p - z * x_ * y_) % x_;
			//System.out.println("x: " + x + " y:" + y_ + " z:" + z);
			decals.get(grass_incremenet).setPosition(x/thickness, 0, z/thickness);
			decals.get(grass_incremenet).setRotation(MathUtils.random(60) + 300, 0, 0);
			grass_incremenet+=1;	
		}
		for(int p = 0; p < Max; p++){
			int z = p / (x_ * y_);
			int x = (p - z * x_ * y_) % x_;
			//System.out.println("x: " + x + " y:" + y_ + " z:" + z);
			decals.get(grass_incremenet).setPosition(x/thickness, 0, z/thickness);
			decals.get(grass_incremenet).setRotation(MathUtils.random(360) + 300, 0, 0);
			grass_incremenet+=1;	
		}
	}

	//	private void grid(int width, int height) {
	//		for (int x = 0; x <= width; x++) {
	//			lineRenderer.color(0, 1, 0, 1);
	//			lineRenderer.vertex(x, -.5f, 0);
	//			lineRenderer.color(0, 1, 0, 1);
	//			lineRenderer.vertex(x, -.5f, height);
	//		}
	//
	//		for (int y = 0; y <= height; y++) {
	//			lineRenderer.color(0, 1, 0, 1);
	//			lineRenderer.vertex(0, -.5f, y);
	//			lineRenderer.color(0, 1, 0, 1);
	//			lineRenderer.vertex(width, -.5f, y);
	//		}
	//	}

	@Override
	public void render(float delta) {
		//gl
		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl20.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
		Gdx.gl20.glEnable(GL30.GL_DEPTH_TEST);
		Gdx.gl20.glEnable(GL30.GL_TEXTURE_2D);
		Gdx.gl20.glEnable(GL30.GL_BLEND);
		Gdx.gl20.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl20.glCullFace(GL30.GL_NONE);

		//camera
		camera_control.update();
		//spriteBatch.setProjectionMatrix(camera_perspective.combined);

		//grid
		//		lineRenderer.begin(camera.combined, GL30.GL_LINES);
		//		grid(20, 20);
		//		lineRenderer.end();

		//disable depth writing
		Gdx.gl20.glDepthMask(false);

		//draw grass decals
		for(Decal d : decals){
			decalBatch.add(d);
		}
		decalBatch.flush();

		//hud
		wind_button.setText("wind: " + Integer.toString(intensity));
		fps_button.setText("fps: " + Integer.toString(Gdx.graphics.getFramesPerSecond()));
		stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		stage.act(delta);
		stage.draw();

		//keys
		if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			((Game) Gdx.app.getApplicationListener()).setScreen(new Grass());
		}
		if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
			if (prefs.getInteger("grass_type") == 0) {
				prefs.putInteger("grass_type", 1);
				prefs.flush();
				((Game) Gdx.app.getApplicationListener()).setScreen(new Grass());
			} else {
				prefs.putInteger("grass_type", 0);
				prefs.flush();
				((Game) Gdx.app.getApplicationListener()).setScreen(new Grass());
			}
		}
		
		sound_adjustment();

	}
	
	public void sound_adjustment() {
		if (intensity == 0) {
			Audio.wind_sound.stop();
		} else {
			Audio.wind_sound.play();
		}
		if (intensity == 1) {
			Audio.wind_sound.setVolume(0.1f);
		}
		if (intensity == 2) {
			Audio.wind_sound.setVolume(0.2f);
		}
		if (intensity == 3) {
			Audio.wind_sound.setVolume(0.3f);
		}
		if (intensity == 4) {
			Audio.wind_sound.setVolume(0.4f);
		}
		if (intensity == 5) {
			Audio.wind_sound.setVolume(0.5f);
		}
		if (intensity == 6) {
			Audio.wind_sound.setVolume(0.6f);
		}
		if (intensity == 7) {
			Audio.wind_sound.setVolume(0.7f);
		}
		if (intensity == 8) {
			Audio.wind_sound.setVolume(0.8f);
		}
		if (intensity == 9) {
			Audio.wind_sound.setVolume(0.9f);
		}
		if (intensity == 10) {
			Audio.wind_sound.setVolume(1);
		}
	}

	@Override
	public void resize(int width, int height) {}

	@Override
	public void hide() {}

	@Override
	public void pause() {}

	@Override
	public void resume() {}

	@Override
	public void dispose() {
		texture_Grass.dispose();
		decalBatch.dispose();
		decals.clear();
	}
}
