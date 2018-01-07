package fluddokt.opsu.fake;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import itdelatrisu.opsu.ErrorHandler;
import itdelatrisu.opsu.GameImage;
import itdelatrisu.opsu.Opsu;
import itdelatrisu.opsu.options.Options;

public class GameOpsu extends com.badlogic.gdx.Game {

	public final static String VERSION = "0.16.0b";
	public StateBasedGame sbg;
	
	Stage stage;
	Table table;
	Skin skin;
	static GameOpsu gameOpsu;
	
	boolean inited = false;

	private int dialogCnt;
	
	Label loadingLabel;
	public GameOpsu() {
		gameOpsu = this;
	}

	@Override
	public void pause() {
		System.out.println("Game pause");
		if(!inited)
			return;
		super.pause();
		sbg.gc.loseFocus();
		try {
			sbg.render();
		} catch (SlickException e) {
			e.printStackTrace();
		}
		sbg.gc.lostFocus();
		
	}

	@Override
	public void resume() {
		System.out.println("Game resume");
		if(!inited)
			return;
		super.resume();
		sbg.gc.focus();
	}

	@Override
	public void dispose() {
		System.out.println("Game Dispose");
		if(!inited)
			return;
		
		for (GameImage img : GameImage.values()) {
			try {
				img.dispose();
			} catch (SlickException e) {
				e.printStackTrace();
			}
		}
		sbg.gc.closing();
		super.dispose();
		
	}

	int delayLoad = 0;
	@Override
	public void render() {
		super.render();
		
		if (delayLoad>2 && dialogCnt == 0){
		try{
			if (sbg == null){
				if (Gdx.graphics.getWidth() > Gdx.graphics.getHeight()){
					sbg = Opsu.start();
					sbg.gc.width = Gdx.graphics.getWidth();
					sbg.gc.height = Gdx.graphics.getHeight();
					
					try {
						sbg.init();
					} catch (SlickException e) {
						e.printStackTrace();
						error("SlickErrorInit", e);
					}
					File dataDir = Options.DATA_DIR;
					System.out.println("dataDir :"+dataDir+" "+dataDir.isExternal()+" "+dataDir.exists());
					if(dataDir.isExternal()){
						File nomediafile = new File(dataDir, ".nomedia");
						if(!nomediafile.exists())
							new FileOutputStream(nomediafile.getIOFile()).close();
						
						File readmefile = new File(new File(Gdx.files.internal("res")), "readme.txt");
						File readmefilecpyto = new File(dataDir, "readme.txt");
						System.out.println("readmeexist: "+readmefile.exists()+" "+readmefilecpyto.exists()+" "+readmefile.length()+" "+readmefilecpyto.length());
						if (readmefile.exists() && !readmefilecpyto.exists() || readmefile.length() != readmefilecpyto.length()) {
							try(
								InputStream in = new FileInputStream(readmefile);
								OutputStream out = new fluddokt.opsu.fake.FileOutputStream(readmefilecpyto);
							) {
								byte[] buf = new byte[512];
								while (true) {
									int read = in.read(buf);
									if (read < 0)
										break;
									out.write(buf, 0, read);
								}
							}
						}
						
					}
					System.out.println("Local Dir:"+Gdx.files.getLocalStoragePath());
					Gdx.input.setInputProcessor(new InputMultiplexer(stage, sbg));
					inited = true;
					table.removeActor(loadingLabel);
				}
			} else {
				Color bgcolor = Graphics.bgcolor;
				if (bgcolor != null)
					Gdx.gl.glClearColor(bgcolor.r, bgcolor.g, bgcolor.b, 1);
				Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
				try {
					if (sbg.gc.exited) {
						sbg = null;
						delayLoad = 0;
						table.addActor(loadingLabel);
					}
					else
						sbg.render();
				} catch (SlickException e) {
					e.printStackTrace();
					error("SlickErrorRender", e);
				}
				//loadingImage.draw(32, 32,128, 128);
				Graphics.checkMode(0);
			}
			} catch (Throwable e){
				e.printStackTrace();
				error("RenderError", e);
			}
		} else {
			if (delayLoad<=2)
				delayLoad++;
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		}
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
		//table.debugAll();
	}

	@Override
	public void resize(int width, int height) {
		System.out.println("Game resize" + width + " " + height);

		super.resize(width, height);
		Graphics.resize(width, height);
		//stage.getViewport().setCamera(Graphics.camera);
		stage.getViewport().update(width, height, true);
		table.invalidate();
		if(!inited)
			return;
		sbg.gc.width = width;
		sbg.gc.height = height;
	}

	@Override
	public void create() {

		stage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(stage);
		table = new Table();
		table.setFillParent(true);
		stage.addActor(table);
		
		skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
		
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				ErrorHandler.error("** Uncaught Exception! A **", e, true);
			}
		});
		
		if(!Gdx.files.isExternalStorageAvailable()){
			if(!Gdx.files.isLocalStorageAvailable()){
				error("No storage is available ... ????", null);
			}else{
				error("External Storage is not available. \n"
						+"Using Local Storage instead.\n"
						+ Gdx.files.getLocalStoragePath() , null);
			}
		}
		
		Gdx.graphics.setVSync(false);
		Gdx.input.setCatchBackKey(true);
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		
		Graphics.init();
		
		loadingLabel = new Label("Loading...", skin);
		table.addActor(loadingLabel);
		
		Opsu.main(new String[0]);
		
	}

	public static void error(String string, Throwable e) {
		gameOpsu.errorDialog(string, e);
	}

	private void errorDialog(final String string, final Throwable e) {
		dialogCnt++;
		String tbodyString = "X";
		if(e != null){
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			tbodyString = sw.toString();
		}
		final String bodyString = tbodyString;
		Dialog dialog = new Dialog("ERROR", skin){
			final String title = string;
			final String body = bodyString;
			@Override
			protected void result(Object object) {
				System.out.println(object);
				
				if("CloseOpsu".equals(object)){
					System.exit(0);
				}
				
				if("R".equals(object)){
					try {
						System.out.println("Reporting");
						Desktop.getDesktop().browse(
							ErrorHandler.getIssueURI(title, e, body)
						);
					}  catch (IOException e) {
						e.printStackTrace();
					}
				}
				if("S".equals(object)){
					
				}
				
				dialogCnt--;
				System.out.println("Dialog count:"+dialogCnt);
			}
			
		}.button("Ignore and Continue","S").button("Report on github","R").button("Close Opsu", "CloseOpsu");

		Label tex =new Label(string+"\n"+bodyString, skin);
		
		dialog.getContentTable().add(new ScrollPane(tex))
			.width(Gdx.graphics.getWidth())
			.height(Gdx.graphics.getHeight());
		dialog.pack();
		table.addActor(dialog);
		
		table.validate();
	}
}
