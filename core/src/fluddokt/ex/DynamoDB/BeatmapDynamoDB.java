package fluddokt.ex.DynamoDB;
/**
 * Created by user on 12/12/2017.
 */


import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;


//Holder class for important Beatmap information used to push data to DynamoDB
//Essentially a copy of a scoreData class that can Communicate with DynamoDB
@DynamoDBTable(tableName = "Beatmap")
public class BeatmapDynamoDB {
    private long timestamp;
    private int MID;
    private int MSID;
    private String title;
    private String creator;
    private String artist;
    private String version;
    private int hit300;
    private int hit100;
    private int hit50;
    private int geki;
    private int katu;
    private int miss;
    private long score;
    private int combo;
    private boolean perfect;
    private int mods;
    private String username;

    @DynamoDBAttribute(attributeName = "Artist")
    public String getArtist() {
        return artist;
    }
    public void setArtist(String artist) {
        this.artist = artist;
    }

    @DynamoDBAttribute(attributeName = "timestamp")
    public long getTimestamp() {return timestamp;}
    public void setTimestamp(long timestamp) {this.timestamp = timestamp;}

    @DynamoDBHashKey(attributeName = "MID")
    public int getMID() {return MID;}
    public void setMID(int MID) {this.MID = MID;}

    @DynamoDBAttribute(attributeName = "MSID")
    public int getMSID() {return MSID;}
    public void setMSID(int MSID) {this.MSID = MSID;}

    @DynamoDBAttribute(attributeName = "Title")
    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}

    @DynamoDBAttribute(attributeName = "Creator")
    public String getCreator() {return creator;}
    public void setCreator(String creator) {this.creator = creator;}

    @DynamoDBAttribute(attributeName = "Version")
    public String getVersion() {return version;}
    public void setVersion(String version) {this.version = version;}

    @DynamoDBAttribute(attributeName = "300")
    public int getHit300() {return hit300;}
    public void setHit300(int hit300) {this.hit300 = hit300;}

    @DynamoDBAttribute(attributeName = "100")
    public int getHit100() {return hit100;}
    public void setHit100(int hit100) {this.hit100 = hit100;}

    @DynamoDBAttribute(attributeName = "50")
    public int getHit50() {return hit50;}
    public void setHit50(int hit50) {this.hit50 = hit50;}

    @DynamoDBAttribute(attributeName = "Geki")
    public int getGeki() {return geki;}
    public void setGeki(int geki) {this.geki = geki;}

    @DynamoDBAttribute(attributeName = "Katu")
    public int getKatu() {return katu;}
    public void setKatu(int katu) {this.katu = katu;}

    @DynamoDBAttribute(attributeName = "Miss")
    public int getMiss() {return miss;}
    public void setMiss(int miss) {this.miss = miss;}

    @DynamoDBRangeKey(attributeName = "Score")
    public long getScore() {return score;}
    public void setScore(long score) {this.score = score;}

    @DynamoDBAttribute(attributeName = "Combo")
    public int getCombo() {return combo;}
    public void setCombo(int combo) {this.combo = combo;}

    @DynamoDBAttribute(attributeName = "Perfect")
    public boolean isPerfect() {return perfect;}
    public void setPerfect(boolean perfect) {this.perfect = perfect;}

    @DynamoDBAttribute(attributeName = "Mods")
    public int getMods() {return mods;}
    public void setMods(int mods) {this.mods = mods;}

    @DynamoDBAttribute(attributeName = "Username")
    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}
}