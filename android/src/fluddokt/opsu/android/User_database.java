package fluddokt.opsu.android;

/**
 * Created by user on 12/12/2017.
 */


import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by user on 12/10/2017.
 */
//Holder class for important User information used to
@DynamoDBTable(tableName = "Username")
public class User_database {
    private String username;
    private String password;

    //Use SHA256 for security
    @DynamoDBAttribute(attributeName = "Password")
    public String getPassword() {
        if(password==null)
            return null;
        return sha256(password);
    }

    public String retrieveRawPassword() {

        return (password);
    }

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