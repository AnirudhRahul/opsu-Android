package fluddokt.ex.DynamoDB;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.QueryResultPage;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import itdelatrisu.opsu.ErrorHandler;
import itdelatrisu.opsu.ScoreData;
import itdelatrisu.opsu.ui.badges.BadgeGroup;
import itdelatrisu.opsu.user.User;

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
        userTofind.setUsername(username);
        DynamoDBQueryExpression query=new DynamoDBQueryExpression().withHashKeyValues(userTofind);
        PaginatedQueryList<UserDB> result = mapper.query(UserDB.class, query);
        //Check if any result was returned
        return (result.size()!=0);
    }

    public User createUser(UserDB foundUser){
        User user=new User(foundUser.getUsername(),foundUser.getScore(),foundUser.getAccuracy(),foundUser.getPlaysPassed(),foundUser.getPlaysTotal(),0);
        user.setHashedPassword(foundUser.getPassword());
        user.setAvailableIcons(foundUser.getAvailableIcons());

        List<Integer> defaultBadges = new ArrayList<>();
        for(int i=0;i<BadgeGroup.ALL_BADGES.length;i++)
            defaultBadges.add(0);
        user.setBadges(coalesce(foundUser.getBadges(), defaultBadges));
        while (user.getBadges().size()<defaultBadges.size())
            user.getBadges().add(0);

        user.tokens = coalesce(foundUser.getTokens(), 0);
        user.friendLimit = coalesce(foundUser.getFriendLimit(), 5);
        user.consecutiveLogins = coalesce(foundUser.getConsecutiveLogins(), 0);
        user.lastLogin = coalesce(foundUser.getLastLogin(), (long)0);
        user.topped = coalesce(foundUser.getTopped(), false);
        user.setFriendNames(coalesce(foundUser.getFriends(), new ArrayList<String>()));
        return user;
    }

    //Checks if the database has a user with a certain username and password
    public User dataBaseContainsUsernameAndPassword(String username, String password) {
        //Search for a user with a specific username
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(retrieveCredentials());
        final DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
        UserDB userTofind=new UserDB();
        userTofind.setUsername(username);
        DynamoDBQueryExpression query=new DynamoDBQueryExpression().withHashKeyValues(userTofind);
        PaginatedQueryList<UserDB> result = mapper.query(UserDB.class, query);

        //Check if the passwords match
        //Note we only check the first index because our database doesn't have duplicate usernames
        if(result.size()>0){
            //The result returned is already hashed so to make sure we don't hash it twice we use rawPassword
            if(result.get(0).getPassword().equals(sha256(password)))
                return createUser(result.get(0));
        }

        return null;
    }

    public User getUserFromDB(String username){
        //Search for a user with a specific username

        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(retrieveCredentials());
        final DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

        UserDB userTofind=new UserDB();
        userTofind.setUsername(username);
        DynamoDBQueryExpression query=new DynamoDBQueryExpression().withHashKeyValues(userTofind);
        PaginatedQueryList<UserDB> result = mapper.query(UserDB.class, query);
        if(result.size()>0)
            return createUser(result.get(0));
        return null;

//        UserDB foundUser=result.get(0);
//        User user=new User(foundUser.getUsername(),foundUser.getScore(),foundUser.getAccuracy(),foundUser.getPlaysPassed(),foundUser.getPlaysTotal(),0);
//
//        user.setHashedPassword(foundUser.getPassword());
//        user.setAvailableIcons(foundUser.getAvailableIcons());
//
//        List<Integer> defaultBadges = new ArrayList<>();
//        for(int i=0;i<BadgeGroup.ALL_BADGES.length;i++)
//            defaultBadges.add(0);
//        user.setBadges(coalesce(foundUser.getBadges(), defaultBadges));
//        while (user.getBadges().size()<defaultBadges.size())
//            user.getBadges().add(0);
//
//        user.tokens = coalesce(foundUser.getTokens(), 0);
//        user.friendLimit = coalesce(foundUser.getFriendLimit(), 5);
//        user.consecutiveLogins = coalesce(foundUser.getConsecutiveLogins(), 0);
//        user.lastLogin = coalesce(foundUser.getLastLogin(), (long)0);
//        user.topped = coalesce(foundUser.getTopped(), false);
//        user.setFriendNames(coalesce(foundUser.getFriends(), new ArrayList<String>()));
//
//        UI.getNotificationManager().sendNotification("Badges : " + (user.getBadges()));
//        UI.getNotificationManager().sendNotification("Tops : " + user.topped);
//        UI.getNotificationManager().sendNotification("Friends list : " + user.getFriendNames());
//
//        return user;

    }

    public void updateUser(User currentUser){
        try {
            String username = currentUser.getName();

            //Search for a user with a specific username
            ClientConfiguration clientConfig = new ClientConfiguration();
            clientConfig.setConnectionTimeout(1000);
            clientConfig.setMaxErrorRetry(3);

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(retrieveCredentials(), clientConfig);
            final DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

            UserDB userTofind = new UserDB();
            userTofind.setUsername(username);
            DynamoDBQueryExpression query = new DynamoDBQueryExpression().withHashKeyValues(userTofind);
            PaginatedQueryList<UserDB> result = mapper.query(UserDB.class, query);
            UserDB foundUser = result.get(0);

            currentUser.score = foundUser.getScore();
            currentUser.accuracy = foundUser.getAccuracy();
            currentUser.playsPassed = foundUser.getPlaysPassed();
            currentUser.playsTotal = foundUser.getPlaysTotal();


            currentUser.setHashedPassword(foundUser.getPassword());
            currentUser.setAvailableIcons(foundUser.getAvailableIcons());

            List<Integer> defaultBadges = new ArrayList<>();
            for (int i = 0; i < BadgeGroup.ALL_BADGES.length; i++)
                defaultBadges.add(0);
            currentUser.setBadges(coalesce(foundUser.getBadges(), defaultBadges));
            while (currentUser.getBadges().size() < defaultBadges.size())
                currentUser.getBadges().add(0);

            currentUser.tokens = coalesce(foundUser.getTokens(), 0);
            currentUser.friendLimit = coalesce(foundUser.getFriendLimit(), 5);
            currentUser.consecutiveLogins = coalesce(foundUser.getConsecutiveLogins(), 0);
            currentUser.lastLogin = coalesce(foundUser.getLastLogin(), (long) 0);
            currentUser.setFriendNames(coalesce(foundUser.getFriends(), new ArrayList<String>()));
            currentUser.topped = coalesce(foundUser.getTopped(), false);
        }catch (Exception e){ErrorHandler.error("Update Error",e, false);}
//        UI.getNotificationManager().sendNotification("Badges from DB : "+foundUser.getBadges());
//        UI.getNotificationManager().sendNotification("Badges : "+ (currentUser.getBadges()));
//        UI.getNotificationManager().sendNotification("Tops : "+ currentUser.topped);
//        UI.getNotificationManager().sendNotification("Friends list : "+ currentUser.getFriendNames());

    }

    //Returns two if one is null
    public static <T> T coalesce(T one, T two)
    {
        return one != null ? one : two;
    }

    //Adds a user to the database
    public void addUserToDataBase(User user){
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(retrieveCredentials());
        final DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
        UserDB userToAdd=new UserDB();
        //Add all 9 user attributes
        userToAdd.setUsername(user.getName());
        //Checks is the hashed password field exists and is valid
        if(user.getHashedPassword()!=null&&user.getHashedPassword().length()==64)
            userToAdd.setPassword(user.getHashedPassword());
        else
            userToAdd.setPassword(sha256(user.getPassword()));

        userToAdd.setPlaysTotal(user.getTotalPlays());
        userToAdd.setPlaysPassed(user.getPassedPlays());
        userToAdd.setAccuracy(user.getAccuracy());
        userToAdd.setAvailableIcons(user.getAvailableIcons());
        userToAdd.setScore(user.getScore());

//        List<Integer> list =new ArrayList<>(user.getBadges().length);
//        for(int i=0;i<user.getBadges().length;i++)
//            list.add(user.getBadges()[i]);
        userToAdd.setBadges(user.getBadges());
        userToAdd.setFriends(user.getFriendNames());
        userToAdd.setConsecutiveLogins(user.consecutiveLogins);
        userToAdd.setFriendLimit(user.friendLimit);
        userToAdd.setLastLogin(user.lastLogin);
        userToAdd.setTokens(user.tokens);
        userToAdd.setTopped(user.topped);

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
    public QueryResultPage<BeatmapDynamoDB> getBeatmapScore(int id){
        BeatmapDynamoDB beatmapToFind=new BeatmapDynamoDB();
        beatmapToFind.setMID(id);
        //Use ScanIndexForward to sort by the sort key(boolean values makes it ascending or descending order)
        DynamoDBQueryExpression query=new DynamoDBQueryExpression().withHashKeyValues(beatmapToFind).withScanIndexForward(false).withLimit(500);
        return beatmapQuery(query);
    }

    //***Load pagination asynchronously
    public QueryResultPage<BeatmapDynamoDB> beatmapQuery(DynamoDBQueryExpression query){
        try {
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(retrieveCredentials());
        DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
        QueryResultPage<BeatmapDynamoDB>  resultPage = mapper.queryPage(BeatmapDynamoDB.class, query);
        return resultPage;
        }catch (Exception e){
//            UI.getNotificationManager().sendNotification(e.getMessage());
            ErrorHandler.error("db retrieval error",e,true);
        }

        return null;
    }
    //Turns the results of a query into a ScoreData object which is usable locally
    public ArrayList<ScoreData> createScoreData(QueryResultPage<BeatmapDynamoDB> queryList) throws SQLException {
        List<BeatmapDynamoDB> queryListResults = queryList.getResults();
        ArrayList<ScoreData> list=new ArrayList<>(queryListResults.size());

        for(int i=0;i<queryListResults.size();i++){
            ScoreData toAdd = new ScoreData(queryListResults.get(i));
            toAdd.rank=i;
            list.add(toAdd);
        }
        return list;
    }

//    //Same as the previous function except the data is filtered by a specific username.
//    public ScoreData[] createScoreData(QueryResultPage<BeatmapDynamoDB> queryList, String username) throws SQLException {
//        List<BeatmapDynamoDB> queryListResults = queryList.getResults();
//        ArrayList<ScoreData> list=new ArrayList<>(queryListResults.size());
//        int i=0;
//        for(BeatmapDynamoDB e: queryListResults){
//            if(e.getUsername().equals(username)) {
//                ScoreData temp=new ScoreData(e);
//                temp.rank=i;
//                list.add(temp);
//            }
//            i++;
//        }
//        ScoreData[] array=new ScoreData[list.size()];
//        for(int k=0;k<array.length;k++)
//            array[k]=list.get(k);
//        return array;
//    }
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
