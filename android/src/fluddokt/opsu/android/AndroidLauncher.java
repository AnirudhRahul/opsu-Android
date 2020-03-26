package fluddokt.opsu.android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.core.app.ActivityCompat;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.io.IOException;
import java.util.Arrays;

import fluddokt.ex.DeviceInfo;
import fluddokt.ex.DynamoDB.DynamoDB;
import fluddokt.ex.InterstitialAdLoader;
import fluddokt.ex.VideoLoader;
import fluddokt.opsu.fake.GameOpsu;
import io.fabric.sdk.android.Fabric;


public class AndroidLauncher extends AndroidApplication implements SurfaceHolder.Callback {
	String identityPool;
	private InterstitialAd mInterstitialAd;
	private SharedPreferences prefs;
//Media Player Variables
	String path;
	private MediaPlayer mp;
	private SurfaceView surfaceView;
	private SurfaceHolder holder;
	private String preparedFile="";
	private boolean setupComplete=false;
	public enum PlayerState{SETUP,READY};
	PlayerState state=null;
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		Log.e("BruhBruh",""+Fabric.isInitialized());
//		Fabric.with(this, new Crashlytics());
		identityPool=getString(R.string.aws_identity);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);


		setContentView(R.layout.main);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
				Log.v("Permissions","Good");
			}
			else{
				requestPermissionWrite();
				requestPermissionRead();
			}
		}
		config.useImmersiveMode = true;
		config.useWakelock = true;

		Log.e("BruhBruh",""+Fabric.isInitialized());

		DeviceInfo.info = new DeviceInfo() {
			@Override
			public String getInfo() {

				return
						"BOARD: "+Build.BOARD
						+"\nFINGERPRINT: "+Build.FINGERPRINT
						+"\nHOST: "+Build.HOST
						+"\nMODEL: "+Build.MODEL
						+"\nINCREMENTAL: "+Build.VERSION.INCREMENTAL
						+"\nRELEASE: "+Build.VERSION.RELEASE
						+"\n"
						;
			}

			@Override
			public String getDownloadDir() {

				return (String.valueOf(new FileHandle(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))));
			}

			@Override
			public boolean hasPhysicalButtons(){
				return ViewConfiguration.get(getApplicationContext()).hasPermanentMenuKey();
			}

			@Override
			public boolean isMusicPlaying(){
				AudioManager a=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
				return a.isMusicActive();
			}

			@Override
			public boolean isSynced(){
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				return preferences.getBoolean("Sync", false);

			}

			@Override
			public void setSynced(boolean in){
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				SharedPreferences.Editor editor = preferences.edit();
				editor.putBoolean("Sync", in);
				editor.apply();
			}
			@Override
			public boolean shownNotification(String name){
				return prefs.getBoolean(name,false);
			}
			@Override
			public void setShownNotification(String name,boolean val){
				prefs.edit().putBoolean(name,val).apply();
			}

			@Override
			public boolean getHardReset(){
				return prefs.getBoolean("HardReset",false);
			}
			@Override
			public void setHardReset(boolean val){
				if(val)
					prefs.edit().putBoolean("HardReset",val).commit();
				else
					prefs.edit().putBoolean("HardReset",val).apply();

			}

			@Override
			public void reportError(Throwable e){
				Log.e("Crashlytics Reporting","Error Reported");
				Crashlytics.getInstance().core.logException(e);
			}
			@Override
			public void restart(){
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					finishAffinity();
					Intent intent = new Intent(getApplicationContext(), AndroidLauncher.class);
					startActivity(intent);
				} else {
					ActivityCompat.finishAffinity(AndroidLauncher.this);
					Intent intent = new Intent(getApplicationContext(), AndroidLauncher.class);
					startActivity(intent);
				}
			}

		};
		DynamoDB.database = new DynamoDB(){
			private CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
					getApplicationContext(),
					identityPool, // Identity pool ID
					Regions.US_EAST_1 // Region
			);
			@Override
			public CognitoCachingCredentialsProvider retrieveCredentials(){
				return credentialsProvider;
			}

		};

		//Initialize interstitial ads
		MobileAds.initialize(this,
				getString(R.string.admob_id));
		mInterstitialAd = new InterstitialAd(this);
		mInterstitialAd.setAdUnitId(getString(R.string.ad1_id));
		//Send failure message if ad fails to load
		mInterstitialAd.setAdListener(new AdListener(){
			@Override
			public void onAdFailedToLoad(int errorCode){
//				InterstitialAdLoader.ad.sendNotification("Connection Failed");
			}
		});

		InterstitialAdLoader.ad = new InterstitialAdLoader(){
			//Load ad
			@Override
			public void load(){
				try {
					runOnUiThread(new Runnable() {
						public void run() {
							if(!mInterstitialAd.isLoaded()&&!mInterstitialAd.isLoading()){
							AdRequest interstitialRequest = new AdRequest.Builder().build();
							mInterstitialAd.loadAd(interstitialRequest);
							}
						}
					});
				}
				catch (Exception e) {}

			}
			@Override
			public void loadAndShow(){
				try {

					runOnUiThread(new Runnable() {
						public void run() {
							//If the ad isn't loaded, load it, and then show it immediately after its loaded
							if(!mInterstitialAd.isLoaded()) {
								if (!mInterstitialAd.isLoading()) {
									AdRequest interstitialRequest = new AdRequest.Builder().build();
									mInterstitialAd.loadAd(interstitialRequest);
//									InterstitialAdLoader.ad.sendNotification("Loading...");
								}
								//Show the ad immediately after its loaded
								mInterstitialAd.setAdListener(new AdListener() {
									@Override
									public void onAdLoaded() {
										mInterstitialAd.show();
										//Reset the ad so it doesn't always immediately show the ad when its done loading
										mInterstitialAd.setAdListener(new AdListener() {
											@Override
											public void onAdLoaded() {
												super.onAdLoaded();
											}
										});
									}
								});
							}
							//If the ad is loaded show the add
							else
								mInterstitialAd.show();
						}
					});
				}
				catch (Exception e) {}
			}

			};
		surfaceView = findViewById(R.id.video);
		holder = surfaceView.getHolder();
		holder.addCallback(this);
//		holder.setFormat();
		mp = new MediaPlayer();
		mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				setupComplete=true;
			}
		});
		state=PlayerState.READY;
		VideoLoader.loader = new VideoLoader(){


			@Override
			public String setDataSource(final String filepath) throws IOException {
				try {
					mp.setDataSource(filepath);
					preparedFile = filepath;
					return "True";
				}catch (Exception e){return "False\n"+e.getMessage();}
			}

			@Override
			public void pause(){
				mp.pause();
//				state=PlayerState.PAUSED;
			}

			@Override
			public void start(){
				mp.start();
				setupComplete=false;
//				state=PlayerState.PLAYING;
			}

			public void startFrom0(){
				seek(0);
			}

			@Override
			public void stop(){
				mp.stop();
//				state=PlayerState.STOPPED;
			}

			@Override
			public String getPreparedFile(){
				return preparedFile;
			}

			@Override
			public void reset(){
				mp.reset();
//				state=PlayerState.UNINITIALIZED;
			}

			@Override
			public String adjustBrightness(final int brightness){
				try {
					runOnUiThread(new Runnable() {
						public void run() {
							surfaceView.setBackgroundColor(Color.argb(256-brightness,0, 0, 0));
						}
					});
				}
				catch (Exception e) {return e.toString();}
				return "True";
			}

			@Override
			public  boolean isPlaying(){return mp.isPlaying();}
			@Override
			public boolean setupComplete(){return setupComplete;}

			@Override
			public String setupVideo(String path) {
				setupComplete=false;
					try {
						if(state!=PlayerState.READY) {
							mp.reset();
							mp.release();
							mp = new MediaPlayer();
							try{
								mp.setDisplay(holder);
							}catch (Exception e){mp = new MediaPlayer();}
							mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
								@Override
								public void onPrepared(MediaPlayer mp) {
									setupComplete = true;
									}
							});
						}
						state = PlayerState.SETUP;
						mp.setDataSource(path);
						mp.prepareAsync();
						return "True";
					}catch (Exception e){
						return e.toString()+"\n"+Arrays.toString(e.getStackTrace());
					}

			}

			@Override
			public void makeInvisible(){
				try {
					runOnUiThread(new Runnable() {
						public void run() {
							surfaceView.setVisibility(View.INVISIBLE);
						}
					});
				}
				catch (Exception e) {}
			}

			@Override
			public void makeVisible(){
				try {
					runOnUiThread(new Runnable() {
						public void run() {
							surfaceView.setVisibility(View.VISIBLE);
						}
					});
				}
				catch (Exception e) {}
			}

			@Override
			public void seek(int ms){
//				state=PlayerState.SEEKING;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					mp.seekTo(ms,MediaPlayer.SEEK_CLOSEST);
				}
				else{
					mp.seekTo(ms);
				}

			}

			@Override
			public String prepare() throws IOException {
				try {
//					state = PlayerState.PREPARING;
					mp.prepareAsync();
					return "True";
				}catch (Exception e){return "False\n"+e.toString();}
			}


			@Override
			public String getState(){return state.toString();}
		};

//			initialize(new GameOpsu(), config);
		RelativeLayout layout = findViewById(R.id.layout);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		config.r = config.g = config.b = 8;
		config.a = 8;
		config.useGLSurfaceView20API18 = true;

		layout.addView(initializeForView(new GameOpsu(),config),params);

		if (graphics.getView() instanceof SurfaceView) {
			SurfaceView glView = (SurfaceView) graphics.getView();
			glView.getHolder().setFormat(PixelFormat.RGBA_8888);
			glView.setZOrderOnTop(true);
		}
	}
	private void requestPermissionWrite() {
		if (!ActivityCompat.shouldShowRequestPermissionRationale(AndroidLauncher.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
			ActivityCompat.requestPermissions(AndroidLauncher.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
	}

	private void requestPermissionRead() {
		if (!ActivityCompat.shouldShowRequestPermissionRationale(AndroidLauncher.this, Manifest.permission.READ_EXTERNAL_STORAGE))
			ActivityCompat.requestPermissions(AndroidLauncher.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
//		Toast.makeText(getApplicationContext(),"Created Surface",Toast.LENGTH_LONG).show();
		mp.setDisplay(holder);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//		Toast.makeText(getApplicationContext(),"Changed Surface",Toast.LENGTH_LONG).show();

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
//		Toast.makeText(getApplicationContext(),"Destroyed Surface",Toast.LENGTH_LONG).show();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN
							| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}
	}
}
