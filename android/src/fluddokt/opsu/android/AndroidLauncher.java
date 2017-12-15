package fluddokt.opsu.android;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
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

				return new File(new FileHandle(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
			}


		};
		DynamoDB.database = new DynamoDB(){

			@Override
			public boolean dataBaseContainsUsername(String username) {
				CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
						getApplicationContext(),
						"", // Identity pool ID
						Regions.US_EAST_1 // Region
				);
				AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
				final DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

				User_database userTofind=new User_database();
				userTofind.setUsername(username);
				DynamoDBQueryExpression query=new DynamoDBQueryExpression().withHashKeyValues(userTofind);
				PaginatedQueryList<User_database> result = mapper.query(User_database.class, query);
				return (result.size()!=0);
			}
			@Override
			public boolean dataBaseContainsUsernameAndPassword(String username, String password) {
				CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
						getApplicationContext(),
						"", // Identity pool ID
						Regions.US_EAST_1 // Region
				);
				AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
				final DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

				User_database userTofind=new User_database();
				userTofind.setUsername(username);
				DynamoDBQueryExpression query=new DynamoDBQueryExpression().withHashKeyValues(userTofind);
				PaginatedQueryList<User_database> result = mapper.query(User_database.class, query);
				if(result.size()!=0){
					return result.get(0).retrieveRawPassword().equals(sha256(password));
				}
				else
					return false;
			}

			@Override
			public void addUserToDataBase(String username, String password){
				CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
						getApplicationContext(),
						"", // Identity pool ID
						Regions.US_EAST_1 // Region
				);
				AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
				final DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
				User_database userToAdd=new User_database();
				userToAdd.setUsername(username);
				userToAdd.setPassword(password);
				mapper.save(userToAdd);
			}

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
