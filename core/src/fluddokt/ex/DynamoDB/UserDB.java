package fluddokt.ex.DynamoDB;

/**
 * Created by user on 12/12/2017.
 */


import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;

/**
 * Created by user on 12/10/2017.
 */
//Holder class for important User information used to push data to DynamoDB
@DynamoDBTable(tableName = "Users")
public class UserDB {
    private String username;

    /**Important this is version of the password has already been sha256 hashed */
    private String password;

    /** Total score. */
    private long score;

    /** Total accuracy. */
    private double accuracy;

    /** Total number of plays passed. */
    private int playsPassed;

    /** Total number of plays. */
    private int playsTotal;

    private List<Integer> obtainedBages;

    private List<String> friends;

    private int tokens, friendLimit, consecutiveLogins;

    private boolean topped;

    private long lastLogin;

    /** Available profile icon identifier. */
    private List<Integer> icons;

    //Use SHA256 for security
    @DynamoDBAttribute(attributeName = "Password")
    public String getPassword() {
        //Avoid NPE in sha256 function
        if(password==null)
            return null;

        return password;
    }

//    public String retrieveRawPassword() {
//        return password;
//    }

    public void setPassword(String password) {
        this.password = password;
    }

    @DynamoDBHashKey(attributeName = "Username")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @DynamoDBAttribute(attributeName = "Badges")
    public List<Integer> getBadges() {return obtainedBages;}

    public void setBadges(List<Integer> obtainedBages) {this.obtainedBages = obtainedBages;}

    @DynamoDBAttribute(attributeName = "Tokens")
    public int getTokens() {return tokens;}

    public void setTokens(int tokens) {this.tokens = tokens;}

    @DynamoDBAttribute(attributeName = "Friend Limit")
    public int getFriendLimit() {return friendLimit;}

    public void setFriendLimit(int friendLimit) {this.friendLimit = friendLimit;}

    @DynamoDBAttribute(attributeName = "Consecutive Logins")
    public int getConsecutiveLogins() {return consecutiveLogins;}

    public void setConsecutiveLogins(int consecutiveLogins) {this.consecutiveLogins = consecutiveLogins;}

    @DynamoDBAttribute(attributeName = "Topped")
    public boolean getTopped() {return topped;}

    public void setTopped(boolean topped) {this.topped = topped;}

    @DynamoDBAttribute(attributeName = "Last Login")
    public long getLastLogin() {return lastLogin;}

    public void setLastLogin(long lastLogin) {this.lastLogin = lastLogin;}

    @DynamoDBAttribute(attributeName = "Friends")
    public List<String> getFriends() {return friends;}

    public void setFriends(List<String> friends) {this.friends = friends;}

    @DynamoDBAttribute(attributeName = "Score")
    public long getScore() {return score;}

    public void setScore(long score) {this.score = score;}

    @DynamoDBAttribute(attributeName = "Accuracy")
    public double getAccuracy() {return accuracy;}

    public void setAccuracy(double accuracy) {this.accuracy = accuracy;}

    @DynamoDBAttribute(attributeName = "playsPassed")
    public int getPlaysPassed() {return playsPassed;}

    public void setPlaysPassed(int playsPassed) {this.playsPassed = playsPassed;}

    @DynamoDBAttribute(attributeName = "playsTotal")
    public int getPlaysTotal() {return playsTotal;}

    public void setPlaysTotal(int playsTotal) {this.playsTotal = playsTotal;}


    @DynamoDBAttribute(attributeName = "icon")
    public List<Integer> getAvailableIcons() {return icons;}

    public void setAvailableIcons(List<Integer> icons) {this.icons = icons;}

}