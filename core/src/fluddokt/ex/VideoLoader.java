package fluddokt.ex;

import java.io.IOException;

/**
 * Created by user on 10/6/2018.
 */

public class VideoLoader {
    private String preparedFile="";
    public static VideoLoader loader = new VideoLoader();
    public String setDataSource(String filepath) throws IOException {return "";}
    public void pause(){}
    public void start(){}
    public void stop(){}
    public void startFrom0(){}
    public String getPreparedFile(){return "";}
    public boolean isPlaying(){return false;}
    public void reset(){}
    public String release(){return "";}
    public boolean setupComplete(){return false;}
    public String setupVideo(String path){return "";}
    public void seek(int ms){}
    public void makeInvisible(){}
    public void makeVisible(){}
    public String getState(){return "";}
    public String adjustBrightness(int in){return "";}
    public String prepare() throws IOException {return "";}

}
