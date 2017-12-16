package fluddokt.ex;

/**
 * Created by user on 12/10/2017.
 */

public class DynamoDB {
    //Holder class to make accessing DynamoDB easier
    public static DynamoDB database = new DynamoDB();
    public String getInfo() {
        return "";
    }
    public boolean dataBaseContainsUsername(String username) {
        return false;
    }
    public boolean dataBaseContainsUsernameAndPassword(String username, String password){return false;}
    public void addUserToDataBase(String username, String password){}
    public void addBeatmapScore(long timestamp, int MID, int MSID, String title, String creator, String version, int hit300, int hit100, int hit50, int geki, int katu, int miss, long score, int combo, boolean perfect, int mods, String username){}


}
