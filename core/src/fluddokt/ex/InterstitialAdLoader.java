package fluddokt.ex;

import java.util.concurrent.Callable;

import itdelatrisu.opsu.ui.UI;

/**
 * Created by user on 12/25/2017.
 */
//Loader class so we can load ads from libgdx
public class InterstitialAdLoader {
    public static InterstitialAdLoader ad = new InterstitialAdLoader();
    public void load(){}
    public void onShowCompleted(final Callable<Boolean> c){}
    public void loadAndShow(){}
    public void sendNotification(String s){UI.getNotificationManager().sendNotification(s);}
}
