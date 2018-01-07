package fluddokt.ex;

import itdelatrisu.opsu.ui.UI;

/**
 * Created by user on 12/27/2017.
 */
//Loader class so we can load ads from libgdx
//*Currently not in use
public class RewardVideoAdLoader {
    public static RewardVideoAdLoader ad = new RewardVideoAdLoader();

    public long getLastAdWatched() {
        return lastAdWatched;
    }

    public void setLastAdWatched(long lastAdWatched) {
        this.lastAdWatched = lastAdWatched;
    }

    long lastAdWatched=Long.MIN_VALUE;
    public void init(){}
    public void load(){}
    public void loadAndShow(){}
    public void sendNotification(String s){
        UI.getNotificationManager().sendNotification(s);}
    public void showAds(){}
}
