package fluddokt.opsu.android;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import fluddokt.ex.DeviceInfo;
import fluddokt.ex.DynamoDB;
import fluddokt.opsu.fake.File;
import fluddokt.opsu.fake.GameOpsu;

public class AndroidLauncher extends AndroidApplication {
	final String identityPool="us-east-1:bef530a1-0efc-45f5-b1a3-6610019fe5fb";
	public InterstitialAd mInterstitialAd;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useImmersiveMode = true;
		config.useWakelock = true;
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
			public File getDownloadDir() {

				return new File(String.valueOf(new FileHandle(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))));
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

		initialize(new GameOpsu(), config);
	}

}
