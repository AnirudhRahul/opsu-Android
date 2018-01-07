package fluddokt.ex.DynamoDB;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;

import itdelatrisu.opsu.ScoreData;

/**
 * Created by user on 12/10/2017.
 */

public class DynamoDB {
    //Holder class to make accessing DynamoDB easier
    //Static Reference
    public static DynamoDB database = new DynamoDB();
    //Override this in the AndroidLauncher, because we need a context
    public CognitoCachingCredentialsProvider retrieveCredentials(){
        return null;
    }

    //Checks if the database has a user with a certain username
    public boolean dataBaseContainsUsername(String username) {
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(retrieveCredentials());
        final DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
        UserDB userTofind=new UserDB();
        //Only Set the username because that is the only part we want DynamoDB to match
        userTofind.setUsername(username);
        DynamoDBQueryExpression query=new DynamoDBQueryExpression().withHashKeyValues(userTofind);
        PaginatedQueryList<UserDB> result = mapper.query(UserDB.class, query);
        //Check if any result was returned
        return (result.size()!=0);
    }

    //Checks if the database has a user with a certain username and password
    public boolean dataBaseContainsUsernameAndPassword(String username, String password) {
        //Search for a user with a specific username
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(retrieveCredentials());
        final DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
        UserDB userTofind=new UserDB();
        userTofind.setUsername(username);
        DynamoDBQueryExpression query=new DynamoDBQueryExpression().withHashKeyValues(userTofind);
        PaginatedQueryList<UserDB> result = mapper.query(UserDB.class, query);

        //Check if the passwords match
        //Note we only check the first index because our database doesn't have duplicate usernames
        if(result.size()!=0){
            //The result returned is already hashed so to make sure we don't hash it twice we use rawPassword
            return result.get(0).retrieveRawPassword().equals(sha256(password));
        }
        else
            return false;
    }
    //Adds a user to the database
    public void addUserToDataBase(String username, String password){
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(retrieveCredentials());
        final DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
        UserDB userToAdd=new UserDB();
        userToAdd.setUsername(username);
        userToAdd.setPassword(password);
        mapper.save(userToAdd);
    }
    //Adds a score to the database
    public void addBeatmapScore(long timestamp, int MID, int MSID, String title, String creator, String artist, String version, int hit300, int hit100, int hit50, int geki, int katu, int miss, long score, int combo, boolean perfect, int mods, String username){
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(retrieveCredentials());
        final DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
        BeatmapDynamoDB bmp=new BeatmapDynamoDB();
        //Set the corresponding values in bmp
        bmp.setTimestamp(timestamp);bmp.setMID(MID);bmp.setMSID(MSID);bmp.setTitle(title);bmp.setCreator(creator);bmp.setArtist(artist);bmp.setVersion(version);bmp.setHit300(hit300);bmp.setHit100(hit100);bmp.setHit50(hit50);bmp.setGeki(geki);bmp.setKatu(katu);bmp.setMiss(miss);bmp.setScore(score);bmp.setCombo(combo);bmp.setPerfect(perfect);bmp.setMods(mods);bmp.setUsername(username);
        mapper.save(bmp);
    }
    //Returns all the scores for a specific song
    //Note the hashkey is the MID for the beatmap, which is also the haskey we use for our scores database
    public PaginatedQueryList<BeatmapDynamoDB> getBeatmapScore(int hashkey){
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(retrieveCredentials());
        final DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
        BeatmapDynamoDB beatmapToFind=new BeatmapDynamoDB();
        beatmapToFind.setMID(hashkey);
        //Use ScanIndexForward to sort by the sort key(boolean values makes it ascending or descending order)
        DynamoDBQueryExpression query=new DynamoDBQueryExpression().withHashKeyValues(beatmapToFind).withScanIndexForward(false).withLimit(100);

        PaginatedQueryList<BeatmapDynamoDB> result = mapper.query(BeatmapDynamoDB.class, query);

        return result;
    }
    //Turns the results of a query into a ScoreData object which is usable locally
    public ScoreData[] createScoreData(PaginatedQueryList<BeatmapDynamoDB> queryList) throws SQLException {
        ScoreData[] list=new ScoreData[queryList.size()];
        int i=0;
        for(BeatmapDynamoDB e: queryList){
            list[i]=new ScoreData(e);
            i++;
        }
        return list;
    }
    //Same as the previous function except the data is filtered by a specific username.
    public ScoreData[] createScoreData(PaginatedQueryList<BeatmapDynamoDB> queryList, String username) throws SQLException {
        ArrayList<ScoreData> list=new ArrayList<>(queryList.size());
        int i=0;
        for(BeatmapDynamoDB e: queryList){
            if(e.getUsername().equals(username)) {
                ScoreData temp=new ScoreData(e);
                temp.rank=i;
                list.add(temp);
            }
            i++;
        }
        ScoreData[] array=new ScoreData[list.size()];
        for(int k=0;k<array.length;k++)
            array[k]=list.get(k);
        return array;
    }
    //SHA256 has a string
    private String sha256(String a){
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] hash = digest.digest(a.getBytes());
        return bytesToHex(hash);
    }
    //byte[] to hex String
    private static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte b : bytes) result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

}
