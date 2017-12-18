package fluddokt.ex;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import itdelatrisu.opsu.ScoreData;

/**
 * Created by user on 12/10/2017.
 */

public class DynamoDB {
    //Holder class to make accessing DynamoDB easier
    public static DynamoDB database = new DynamoDB();
    public CognitoCachingCredentialsProvider retrieveCredentials(){
        return null;
    }

        public boolean dataBaseContainsUsername(String username) {
            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(retrieveCredentials());
            final DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
            UserDB userTofind=new UserDB();
            userTofind.setUsername(username);
            DynamoDBQueryExpression query=new DynamoDBQueryExpression().withHashKeyValues(userTofind);
            PaginatedQueryList<UserDB> result = mapper.query(UserDB.class, query);
            return (result.size()!=0);
        }

    public boolean dataBaseContainsUsernameAndPassword(String username, String password) {
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(retrieveCredentials());
        final DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

        UserDB userTofind=new UserDB();
        userTofind.setUsername(username);
        DynamoDBQueryExpression query=new DynamoDBQueryExpression().withHashKeyValues(userTofind);
        PaginatedQueryList<UserDB> result = mapper.query(UserDB.class, query);
        if(result.size()!=0){
            return result.get(0).retrieveRawPassword().equals(sha256(password));
        }
        else
            return false;
    }

    public void addUserToDataBase(String username, String password){
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(retrieveCredentials());
        final DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
        UserDB userToAdd=new UserDB();
        userToAdd.setUsername(username);
        userToAdd.setPassword(password);
        mapper.save(userToAdd);
    }
    public void addBeatmapScore(long timestamp, int MID, int MSID, String title, String creator, String artist, String version, int hit300, int hit100, int hit50, int geki, int katu, int miss, long score, int combo, boolean perfect, int mods, String username){
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(retrieveCredentials());
        final DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
        BeatmapDynamoDB bmp=new BeatmapDynamoDB();
        bmp.setTimestamp(timestamp);bmp.setMID(MID);bmp.setMSID(MSID);bmp.setTitle(title);bmp.setCreator(creator);bmp.setArtist(artist);bmp.setVersion(version);bmp.setHit300(hit300);bmp.setHit100(hit100);bmp.setHit50(hit50);bmp.setGeki(geki);bmp.setKatu(katu);bmp.setMiss(miss);bmp.setScore(score);bmp.setCombo(combo);bmp.setPerfect(perfect);bmp.setMods(mods);bmp.setUsername(username);
        mapper.save(bmp);
    }
    public PaginatedQueryList<BeatmapDynamoDB> getBeatmapScore(int hashkey){
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(retrieveCredentials());
        final DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
        BeatmapDynamoDB beatmapToFind=new BeatmapDynamoDB();
        beatmapToFind.setMID(hashkey);
        DynamoDBQueryExpression query=new DynamoDBQueryExpression().withHashKeyValues(beatmapToFind).withScanIndexForward(false);
        query.setLimit(100);
        PaginatedQueryList<BeatmapDynamoDB> result = mapper.query(BeatmapDynamoDB.class, query);

        return result;
    }
    public ScoreData[] createScoreData(PaginatedQueryList<BeatmapDynamoDB> queryList) throws SQLException {
        ScoreData[] list=new ScoreData[queryList.size()];
        int i=0;
        for(BeatmapDynamoDB e: queryList){
            list[i]=new ScoreData(e);
            i++;
        }
        return list;
    }
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
    private static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte b : bytes) result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

}
