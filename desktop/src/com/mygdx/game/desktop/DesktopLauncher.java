package com.mygdx.game.desktop;

import fluddokt.opsu.fake.GameOpsu;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 800;
		config.height = 600;
		config.addIcon("res/icon16.png", FileType.Internal);
		config.addIcon("res/icon32.png", FileType.Internal);
		config.vSyncEnabled = false;
		config.foregroundFPS = 240;
		config.backgroundFPS = 240;
		//config.audioDeviceBufferCount=240;
		new LwjglApplication(new GameOpsu(), config);
	}
}
