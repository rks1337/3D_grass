package grass.simulator;

import com.badlogic.gdx.audio.Music;

public class Audio {

	public static Music wind_sound;

	public static void create() {
		wind_sound = Assets.manager.get(Assets.wind_sound);
	}

	public static void dispose() {
		wind_sound.dispose();
	}
}
