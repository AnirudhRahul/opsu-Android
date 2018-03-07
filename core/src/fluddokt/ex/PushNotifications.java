package fluddokt.ex;

/**
 * Created by user on 1/17/2018.
 */

public class PushNotifications {
    public static PushNotifications notif=new PushNotifications();
    public String message(String name){
        if(DeviceInfo.info.isMusicPlaying()){
            return "Hey "+name+" how about playing beatmaps of your songs instead of just listening to them "+"\uD83C\uDFB5";
        }
        else
            if(Math.random()<0.5)
                return "Hey "+name+" come back and get a new high score on(Popular bmp)";
            else
                return "Hey "+name+" don't forget to come back and practice so you're not rusty";

    }
}
