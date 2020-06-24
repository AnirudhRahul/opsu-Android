/*
 * opsu! - an open-source osu! client
 * Copyright (C) 2014-2017 Jeffrey Han
 *
 * opsu! is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * opsu! is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with opsu!.  If not, see <http://www.gnu.org/licenses/>.
 */

package itdelatrisu.opsu.states;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import fluddokt.ex.DynamoDB.DynamoDB;
import fluddokt.ex.InterstitialAdLoader;
import fluddokt.newdawn.slick.state.transition.EasedFadeOutTransition;
import fluddokt.newdawn.slick.state.transition.FadeInTransition;
import fluddokt.opsu.fake.BasicGameState;
import fluddokt.opsu.fake.Color;
import fluddokt.opsu.fake.Display;
import fluddokt.opsu.fake.GameContainer;
import fluddokt.opsu.fake.Graphics;
import fluddokt.opsu.fake.Image;
import fluddokt.opsu.fake.Input;
import fluddokt.opsu.fake.Log;
import fluddokt.opsu.fake.SlickException;
import fluddokt.opsu.fake.StateBasedGame;
import itdelatrisu.opsu.GameData;
import itdelatrisu.opsu.GameImage;
import itdelatrisu.opsu.GameMod;
import itdelatrisu.opsu.Opsu;
import itdelatrisu.opsu.ScoreData;
import itdelatrisu.opsu.audio.MusicController;
import itdelatrisu.opsu.audio.SoundController;
import itdelatrisu.opsu.audio.SoundEffect;
import itdelatrisu.opsu.beatmap.Beatmap;
import itdelatrisu.opsu.leaderboard.TopGlobal;
import itdelatrisu.opsu.options.Options;
import itdelatrisu.opsu.replay.Replay;
import itdelatrisu.opsu.ui.MenuButton;
import itdelatrisu.opsu.ui.UI;
import itdelatrisu.opsu.ui.animations.AnimatedValue;
import itdelatrisu.opsu.ui.animations.AnimationEquation;
import itdelatrisu.opsu.user.User;
import itdelatrisu.opsu.user.UserList;

/*
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.EasedFadeOutTransition;
import org.newdawn.slick.state.transition.FadeInTransition;
import org.newdawn.slick.util.Log;

*/

/**
 * "Game Ranking" (score card) state.
 * <p>
 * Players are able to view their score statistics, retry the beatmap (if applicable),
 * or watch a replay of the game from this state.
 * </ul>
 */
public class GameRanking extends BasicGameState {
	/** Associated GameData object. */
	private GameData data;

	/** "Retry" and "Replay" buttons. */
	private MenuButton retryButton, replayButton, leaderBoardButton;

	/** Button coordinates. */
	private float retryY, replayY;

	/** Animation progress. */
	private AnimatedValue animationProgress = new AnimatedValue(6000, 0f, 1f, AnimationEquation.LINEAR);

	/** The loaded replay, or null if it couldn't be loaded. */
	private Replay replay = null;

	//Executor Service to send scores to DB
	private ExecutorService executorService;


	// game-related variables
	private StateBasedGame game;
	private final int state;
	private Input input;
	private ScoreData scoreData;
	private Beatmap beatmap;
	public GameRanking(int state) {
		this.state = state;
	}

	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
		this.game = game;
		this.input = container.getInput();
		executorService= Executors.newSingleThreadExecutor();
		int width = container.getWidth();
		int height = container.getHeight();

		// buttons
		Image retry = GameImage.PAUSE_RETRY.getImage();
		Image replay = GameImage.PAUSE_REPLAY.getImage();
		Image leaderboard = GameImage.LEADERBOARD.getImage();
		replayY = (height * 0.985f) - replay.getHeight() / 2f;
		retryY = replayY - (replay.getHeight() / 2f) - (retry.getHeight() / 1.975f);
		retryButton = new MenuButton(retry, width - (retry.getWidth() / 2f), retryY);
		replayButton = new MenuButton(replay, width - (replay.getWidth() / 2f), replayY);
		leaderBoardButton = new MenuButton(leaderboard, width - (leaderboard.getWidth())/2f-replay.getWidth()+width*0.05f, replayY-0.07f*height);
		retryButton.setHoverFade();
		replayButton.setHoverFade();
		leaderBoardButton.setHoverFade(0.7f);


	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		int width = container.getWidth();
		int height = container.getHeight();
		int mouseX = input.getMouseX(), mouseY = input.getMouseY();


		// background
		float parallaxX = 0, parallaxY = 0;
		if (Options.isParallaxEnabled()) {
			int offset = (int) (height * (GameImage.PARALLAX_SCALE - 1f));
			parallaxX = -offset / 2f * (mouseX - width / 2) / (width / 2);
			parallaxY = -offset / 2f * (mouseY - height / 2) / (height / 2);
		}
		if (!beatmap.drawBackground(width, height, parallaxX, parallaxY, 0.5f, true,g,false)) {
			Image bg = GameImage.MENU_BG.getImage();
			if (Options.isParallaxEnabled()) {
				bg = bg.getScaledCopy(GameImage.PARALLAX_SCALE);
				bg.setAlpha(0.5f);
				bg.drawCentered(width / 2 + parallaxX, height / 2 + parallaxY);
			} else {
				bg.setAlpha(0.5f);
				bg.drawCentered(width / 2, height / 2);
				bg.setAlpha(1f);
			}
		}

		// ranking screen elements
		data.drawRankingElements(g, beatmap, animationProgress.getTime());
		// buttons
		replayButton.draw();
		if (data.isGameplay() && !GameMod.AUTO.isActive())
			retryButton.draw();
		leaderBoardButton.draw();
		UI.getBackButton().draw(g);

		UI.draw(g);
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		UI.update(delta);
		int mouseX = input.getMouseX(), mouseY = input.getMouseY();
		replayButton.hoverUpdate(delta, mouseX, mouseY);
		leaderBoardButton.hoverUpdate(delta, mouseX, mouseY);
		if (data.isGameplay())
			retryButton.hoverUpdate(delta, mouseX, mouseY);
		else
			MusicController.loopTrackIfEnded(true);
		UI.getBackButton().hoverUpdate(delta, mouseX, mouseY);
		animationProgress.update(delta);
		data.updateRankingDisplays(delta, mouseX, mouseY);
	}

	@Override
	public int getID() { return state; }

	@Override
	public void mouseWheelMoved(int newValue) {
		UI.globalMouseWheelMoved(newValue, true);
	}

	@Override
	public void keyPressed(int key, char c) {
		if (UI.globalKeyPressed(key))
			return;

		switch (key) {
		case Input.KEY_ESCAPE:
			returnToSongMenu();
			break;
		}
	}

	@Override
	public void mousePressed(int button, int x, int y) {
		// check mouse button
		if (button == Input.MOUSE_MIDDLE_BUTTON)
			return;

		// back to menu
		if (UI.getBackButton().contains(x, y)) {
			returnToSongMenu();
			return;
		}

		// replay
		Game gameState = (Game) game.getState(Opsu.STATE_GAME);
		boolean returnToGame = false;
		boolean replayButtonPressed = replayButton.contains(x, y);
		boolean retryButtonPressed = retryButton.contains(x, y);
		boolean leaderButtonPressed = leaderBoardButton.contains(x, y);


		if (replayButtonPressed && !(data.isGameplay() && GameMod.AUTO.isActive())) {
			if (replay != null) {
				gameState.setReplay(replay);
				gameState.setPlayState((data.isGameplay()) ? Game.PlayState.REPLAY : Game.PlayState.FIRST_LOAD);
				returnToGame = true;
			} else
				UI.getNotificationManager().sendBarNotification("Replay file not found.");
		}
		// retry
		else if (data.isGameplay() &&
		         (!GameMod.AUTO.isActive() && retryButtonPressed) ||
		         (GameMod.AUTO.isActive() && replayButtonPressed)) {
			gameState.setReplay(null);
			gameState.setPlayState(Game.PlayState.NORMAL);
			returnToGame = true;
		}
		else if (!GameMod.AUTO.isActive() && leaderButtonPressed && !UserList.get().getCurrentUser().getName().equals("Guest") && scoreData.playerName.equals(UserList.get().getCurrentUser().getName())&&scoreData.settings==0) {
			if(Options.GameOption.USE_WIFI.getBooleanValue()){
				InterstitialAdLoader.ad.loadAndShow();
				execute(new leaderboard());
				TopGlobal.mode.addScore(beatmap, scoreData);
			}
			else
				UI.getNotificationManager().sendNotification("Please restart with an internet connection to use online features");


			returnToGame = false;
		}
		else if(!GameMod.AUTO.isActive() && leaderButtonPressed && !UserList.get().getCurrentUser().getName().equals("Guest") && !scoreData.playerName.equals(UserList.get().getCurrentUser().getName())&&scoreData.settings==0){
			UI.getNotificationManager().sendNotification("Can't submit scores from a different account");
		}
		if (data.isGameplay() && !GameMod.AUTO.isActive() && leaderButtonPressed && UserList.get().getCurrentUser().getName().equals("Guest")) {
			UI.getNotificationManager().sendNotification("Can't submit scores as a guest");
		}
		if(data.isGameplay() && !GameMod.AUTO.isActive() && leaderButtonPressed&& scoreData.settings!=0)
			UI.getNotificationManager().sendNotification("Can't submit scores without having the default fixed Settings");


		if (returnToGame) {
			beatmap = MusicController.getBeatmap();
			gameState.loadBeatmap(beatmap);
			SoundController.playSound(SoundEffect.MENUHIT);
			game.enterState(Opsu.STATE_GAME, new EasedFadeOutTransition(), new FadeInTransition());
			return;
		}

		// otherwise, finish the animation
		animationProgress.setTime(animationProgress.getDuration());
	}
//	private class leaderboard implements AsyncTask<Integer> {
//		@Override
//		public Integer call() throws Exception {
//			DynamoDB.database.addBeatmapScore(scoreData.timestamp,scoreData.MID,scoreData.MSID,scoreData.title,scoreData.creator,scoreData.artist,scoreData.version,scoreData.hit300,scoreData.hit100,scoreData.hit50,scoreData.geki,scoreData.katu,scoreData.miss,scoreData.score,scoreData.combo,scoreData.perfect,scoreData.mods,scoreData.playerName);
//			UI.getNotificationManager().sendNotification("Score sent");
//			return 0 ;
//		}
//	}
	private class leaderboard implements Callable<Boolean> {
		@Override
		public Boolean call(){
			try {
				DynamoDB.database.addBeatmapScore(scoreData.timestamp, scoreData.MID, scoreData.MSID, scoreData.title, scoreData.creator, scoreData.artist, scoreData.version, scoreData.hit300, scoreData.hit100, scoreData.hit50, scoreData.geki, scoreData.katu, scoreData.miss, scoreData.score, scoreData.combo, scoreData.perfect, scoreData.mods, scoreData.playerName);
				UI.getNotificationManager().sendNotification("Score sent");
			}catch (Exception e){UI.getNotificationManager().sendNotification("Error\n"+e.getMessage());}
			return true;
		}
	}

	private class leaderboardAndUserUpdate implements Callable<Boolean> {
		@Override
		public Boolean call(){
			try {
				DynamoDB.database.addBeatmapScore(scoreData.timestamp, scoreData.MID, scoreData.MSID, scoreData.title, scoreData.creator, scoreData.artist, scoreData.version, scoreData.hit300, scoreData.hit100, scoreData.hit50, scoreData.geki, scoreData.katu, scoreData.miss, scoreData.score, scoreData.combo, scoreData.perfect, scoreData.mods, scoreData.playerName);
				UI.getNotificationManager().sendNotification("Score sent");

				DynamoDB.database.addUserToDataBase(UserList.get().getCurrentUser());
				UI.getNotificationManager().sendNotification("User Info Synced");
			}catch (Exception e){UI.getNotificationManager().sendNotification("Error\n"+e.getMessage());}
			return true;
		}
	}
	@Override
	public void enter(GameContainer container, StateBasedGame game)
			throws SlickException {
		UI.enter();
		beatmap = MusicController.getBeatmap();
		Display.setTitle(game.getTitle());
		if (!data.isGameplay()) {
			if (!MusicController.isTrackDimmed())
				MusicController.toggleTrackDimmed(0.5f);
			replayButton.setY(retryY);
			animationProgress.setTime(animationProgress.getDuration());
		} else {
			SoundController.playSound(SoundEffect.APPLAUSE);
			retryButton.resetHover();
			if (GameMod.AUTO.isActive()) {
				replayButton.setY(retryY);
				animationProgress.setTime(animationProgress.getDuration());
			} else {
				replayButton.setY(replayY);
				animationProgress.setTime(0);
			}
		}
		Beatmap beatmap = MusicController.getBeatmap();
		scoreData=data.getScoreData(beatmap);
		float gameSettings= Options.getFixedAR()+
				Options.getFixedCS()+
				Options.getFixedHP()+
				Options.getFixedOD()+
				Options.getFixedSpeed();
		if(scoreData.settings==-1)
			scoreData.settings=gameSettings;
		if(data.isGameplay()&&Options.GameOption.SYNC_USER_INFO.getBooleanValue()) {
			boolean isGuest=UserList.get().getCurrentUser().getName().equals("Guest");
			boolean isCorrectUser=scoreData.playerName.equals(UserList.get().getCurrentUser().getName());
			boolean fairSettings=scoreData.settings==0;
			UI.drawLoadingProgress(container.getGraphics(), 1f);
			InterstitialAdLoader.ad.loadAndShow();
			if(isGuest)
				UI.getNotificationManager().sendNotification("Can't submit scores as a guest");
			else if(!isCorrectUser)
				UI.getNotificationManager().sendNotification("Can't submit scores from a different account");
			else if(!fairSettings)
				UI.getNotificationManager().sendNotification("Can't submit scores without having the default fixed Settings "+scoreData.settings);
			else if(!Options.GameOption.USE_WIFI.getBooleanValue()){
				UI.getNotificationManager().sendNotification("Please restart with an internet connection to use online features");
			}
			else {
				long currentTime = System.currentTimeMillis();
				long day = TimeUnit.DAYS.toMillis(1);
				User currentUser = UserList.get().getCurrentUser();
				long diff = currentTime - currentUser.lastLogin;
				if(diff>=day && diff<= 2*day){
					currentUser.consecutiveLogins++;
					currentUser.lastLogin = currentTime;
				}
				else
					currentUser.consecutiveLogins=0;

				if(currentUser.consecutiveLogins==0){
					currentUser.consecutiveLogins++;
					currentUser.lastLogin = currentTime;
				}
				boolean changeMade = currentUser.updateBadges();
				if(changeMade)
					UI.getNotificationManager().sendNotification("Congratulations you have been awarded a badge");

				TopGlobal.mode.addScore(beatmap, scoreData);
				//Update scores for when you go back
				TopGlobal.mode.getCache();
				execute(new leaderboardAndUserUpdate());

			}
			InterstitialAdLoader.ad.load();
		}

		replayButton.resetHover();
		loadReplay();
	}

	@Override
	public void leave(GameContainer container, StateBasedGame game)
			throws SlickException {
		this.data = null;
		if (MusicController.isTrackDimmed())
			MusicController.toggleTrackDimmed(1f);

		SoundController.stopSound(SoundEffect.APPLAUSE);
	}
//	private boolean execute(Callable<Boolean> c){
//		Future<Boolean> task = executorService.submit(c);
//		try {
//			return task.get(1500, TimeUnit.MILLISECONDS);
//		} catch (Exception e) {
//			return false;
//		}
//	}
	/**
	 * Returns to the song menu.
	 */
	private void returnToSongMenu() {
		SoundController.playSound(SoundEffect.MENUBACK);
		SongMenu songMenu = (SongMenu) game.getState(Opsu.STATE_SONGMENU);
		if (data.isGameplay())
			songMenu.resetTrackOnLoad();
		songMenu.resetGameDataOnLoad();
		if (UI.getCursor().isBeatmapSkinned())
			UI.getCursor().reset();
		game.enterState(Opsu.STATE_SONGMENU, new EasedFadeOutTransition(), new FadeInTransition());
	}

	/** Loads the replay data. */
	private void loadReplay() {
		this.replay = null;
		Replay r = data.getReplay(null, null, null);
		if (r != null) {
			try {
				r.load();
				this.replay = r;
			} catch (FileNotFoundException e) {
				// file not found
			} catch (IOException e) {
				Log.error("Failed to load replay data.", e);
				UI.getNotificationManager().sendNotification("Failed to load replay data.\nSee log for details.", Color.red);
			}
		}
		// else file not found
	}

	/**
	 * Sets the associated GameData object.
	 * @param data the GameData
	 */
	public void setGameData(GameData data) { this.data = data; }

	/**
	 * Returns the current GameData object (usually null unless state active).
	 */
	public GameData getGameData() { return data; }


	private boolean execute(Callable<Boolean> c){
		Future<Boolean> task = executorService.submit(c);
		try {
			return task.get(1000, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			UI.getNotificationManager().sendNotification("Connection timed out");
			return false;
		}
	}

}
