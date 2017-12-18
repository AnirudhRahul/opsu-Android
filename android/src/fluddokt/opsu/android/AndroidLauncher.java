package fluddokt.opsu.android;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import fluddokt.ex.DeviceInfo;
import fluddokt.ex.DynamoDB;

import fluddokt.opsu.fake.File;
import fluddokt.opsu.fake.GameOpsu;

public class AndroidLauncher extends AndroidApplication {
	final String identityPool="us-east-1:bef530a1-0efc-45f5-b1a3-6610019fe5fb";

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
//			@Override
//			public boolean dataBaseContainsUsername(String username) {
//
//				AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
//				final DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
//
//				UserDB userTofind=new UserDB();
//				userTofind.setUsername(username);
//				DynamoDBQueryExpression query=new DynamoDBQueryExpression().withHashKeyValues(userTofind);
//				PaginatedQueryList<UserDB> result = mapper.query(UserDB.class, query);
//				return (result.size()!=0);
//			}
//			@Override
//			public boolean dataBaseContainsUsernameAndPassword(String username, String password) {
//				AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
//				final DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
//
//				UserDB userTofind=new UserDB();
//				userTofind.setUsername(username);
//				DynamoDBQueryExpression query=new DynamoDBQueryExpression().withHashKeyValues(userTofind);
//				PaginatedQueryList<UserDB> result = mapper.query(UserDB.class, query);
//				if(result.size()!=0){
//					return result.get(0).retrieveRawPassword().equals(sha256(password));
//				}
//				else
//					return false;
//			}
//			@Override
//			public void addBeatmapScore(long timestamp, int MID, int MSID, String title, String creator, String artist, String version, int hit300, int hit100, int hit50, int geki, int katu, int miss, long score, int combo, boolean perfect, int mods, String username){
//				AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
//				final DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
//				BeatmapDynamoDB bmp=new BeatmapDynamoDB();
//				bmp.setTimestamp(timestamp);bmp.setMID(MID);bmp.setMSID(MSID);bmp.setTitle(title);bmp.setCreator(creator);bmp.setArtist(artist);bmp.setVersion(version);bmp.setHit300(hit300);bmp.setHit100(hit100);bmp.setHit50(hit50);bmp.setGeki(geki);bmp.setKatu(katu);bmp.setMiss(miss);bmp.setScore(score);bmp.setCombo(combo);bmp.setPerfect(perfect);bmp.setMods(mods);bmp.setUsername(username);
//				mapper.save(bmp);
//			}
//			public PaginatedQueryList<BeatmapDynamoDB> getBeatmapScore(int hashkey){
//				AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
//				final DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
//				BeatmapDynamoDB beatmapToFind=new BeatmapDynamoDB();
//				beatmapToFind.setMID(hashkey);
//				DynamoDBQueryExpression query=new DynamoDBQueryExpression().withHashKeyValues(beatmapToFind);
//				PaginatedQueryList<BeatmapDynamoDB> result = mapper.query(BeatmapDynamoDB.class, query);
//
//				return result;
//			}
//
//			@Override
//			public void addUserToDataBase(String username, String password){
//				AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
//				final DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
//				UserDB userToAdd=new UserDB();
//				userToAdd.setUsername(username);
//				userToAdd.setPassword(password);
//				mapper.save(userToAdd);
//			}
//
		};
		initialize(new GameOpsu(), config);
	}
	//SHA 256 hash implementation
	public String sha256(String a){
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		byte[] hash = digest.digest(a.getBytes());
		return bytesToHex(hash);
	}
	private static String bytesToHex(byte[] bytes) {
		StringBuffer result = new StringBuffer();
		for (byte b : bytes) result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
		return result.toString();
	}

}
