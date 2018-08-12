package me.timbals.ld42.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import me.timbals.ld42.Game;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1280;
		config.height = 720;

		config.vSyncEnabled = true;
		config.foregroundFPS = 0;
		config.backgroundFPS = 15;

		config.addIcon("icon32.png", Files.FileType.Internal);
        config.addIcon("icon16.png", Files.FileType.Internal);

		new LwjglApplication(new Game(), config);
	}
}
