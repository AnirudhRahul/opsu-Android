package fluddokt.opsu.fake;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;

import fluddokt.opsu.fake.gui.GInputListener;
import fluddokt.opsu.fake.gui.GUIContext;

public class GameContainer extends GUIContext{
	public static StateBasedGame sbg;
	
	public int width = 800;
	public int height = 600;
	public boolean hasFocus = true;
	
	protected boolean running;
	protected boolean forceExit = true;
	boolean exited = false;
	
	public GameContainer(StateBasedGame game) {
		sbg =(StateBasedGame)game;
		sbg.setContainer(this);
	}
	protected void setup(){}
	protected void getDelta(){}
	
	protected boolean running(){return false;}
	protected void gameLoop() throws SlickException{}
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void closing(){
		if(music != null){
			music.dispose();
		}
		close_sub();
	}
	public void exit() {
		closing();
		if (forceExit) {
			Gdx.app.exit();
			System.exit(0);
		}
		exited = true;
	}

	protected void close_sub() {}
	

	public boolean hasFocus() {
		return hasFocus;
	}

	public void setTargetFrameRate(int targetFPS) {
		// TODO Auto-generated method stub
		
	}

	static float musvolume;
	public void setMusicVolume(float musicVolume) {
		musvolume = musicVolume;
		if(music!=null)
			music.setMusicVolume(musvolume);
	}

	public void setShowFPS(boolean b) {
		// TODO Auto-generated method stub
		
	}

	public void setAlwaysRender(boolean b) {
		// TODO Auto-generated method stub
		
	}

	public int getFPS() {
		return Gdx.graphics.getFramesPerSecond();
	}

	public Graphics getGraphics() {
		return Graphics.getGraphics();
	}

	public int getScreenWidth() {return Gdx.graphics.getWidth();}

	public int getScreenHeight() {return Gdx.graphics.getHeight();}

	public void setVSync(boolean b) {
		Gdx.graphics.setVSync(b);
	}

	public void start() throws SlickException {
		// TODO Auto-generated method stub
		
	}

	protected void updateAndRender(int delta) throws SlickException {
		// TODO Auto-generated method stub
		
	}
	
	static Music music;
	public static synchronized void setMusic(Music imusic) {
		if(music!=null)
			music.dispose();
		music = imusic;
		music.setVolume(musvolume);
	}
	boolean musicWasPlaying = false;
	public void loseFocus() {
		hasFocus = false;
		
	}
	public void lostFocus() {
		if(music!=null){
			musicWasPlaying = music.playing();
			if(Gdx.app.getType() == ApplicationType.Android){
				music.pause();
			}
		}
	}
	public void focus() {
		hasFocus = true;
		if(music!=null && musicWasPlaying){
			if(Gdx.app.getType() == ApplicationType.Android){
				music.resume();
			}
		}
	}
	public void setForceExit(boolean b) {
		this.forceExit = b;
	}
	@Override
	public void addInputListener(GInputListener listener) {
		sbg.addInputListener(listener);
	}
	@Override
	public void removeInputListener(GInputListener listener) {
		sbg.removeInputListener(listener);
		
	}
	public void setUpdateOnlyWhenVisible(boolean b) {
		// TODO Auto-generated method stub
		
	}
	public void setDefaultMouseCursor() {
		Gdx.graphics.setCursor(null);
	}
	public void setMouseCursor(Cursor cursor, int x, int y) throws SlickException {
		Gdx.graphics.setCursor(Gdx.graphics.newCursor(cursor.getPixmap(), x, y));
		
	}
	public double getAspectRatio() {
		return (double)width / height;
	}

}
