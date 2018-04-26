package grass.simulator;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;

public class Boot extends Game implements ApplicationListener {

	public static Assets assets;

	@Override
	public void create() {
		assets = new Assets();
		assets.load();
		Assets.manager.finishLoading();
		Audio.create();
		Audio.wind_sound.setVolume(0);
		Audio.wind_sound.play();
		Audio.wind_sound.setLooping(true);
		setScreen(new Grass());
	}
}
