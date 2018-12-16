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


    //SHA 256 hash implementation
//    public String sha256(String a){
//        MessageDigest digest = null;
//        try {
//            digest = MessageDigest.getInstance("SHA-256");
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//        byte[] hash = digest.digest(a.getBytes());
//        return bytesToHex(hash);
//    }
//    private static String bytesToHex(byte[] bytes) {
//        StringBuffer result = new StringBuffer();
//        for (byte b : bytes) result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
//        return result.toString();
//    }


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