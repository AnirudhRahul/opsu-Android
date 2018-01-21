package fluddokt.opsu.android;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.Calendar;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by user on 1/17/2018.
 */

public class AlarmReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(context);
        long lastUse=preferences.getLong("LastTimeUsed",-1);
        String name=preferences.getString("CurrentName","");
        long hour=3600000;
        long day=24*hour;
        if((System.currentTimeMillis()-lastUse)>2*hour&&showNotificiation())
            sendNotification(message(name),context,NotificationManager.IMPORTANCE_LOW);
        else if((System.currentTimeMillis()-lastUse)>day&&showNotificiation())
            sendNotification(message(name),context,NotificationManager.IMPORTANCE_DEFAULT);

        // Your code to execute when the alarm triggers
        // and the broadcast is received.

    }

    public boolean showNotificiation(){
        //check if its 10am-1pm or 6pm-11pm
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); //Current hour
        if((currentHour<=23&&currentHour>=18)||(currentHour>=10&&currentHour<=13))
            return true;
        else
            return false;
    }
    public void sendNotification(String message, Context c, int importance){
        Intent intent = new Intent(c, AndroidLauncher.class);
        PendingIntent pIntent = PendingIntent.getActivity(c, (int) System.currentTimeMillis(), intent, 0);
        NotificationManager notificationManager = (NotificationManager) c.getSystemService(NOTIFICATION_SERVICE);


        Notification.Builder n;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Toast.makeText(c, "Oreo", Toast.LENGTH_SHORT).show();
            NotificationChannel mChannel = new NotificationChannel("OpsuReminder", "Opsu Remind Notifications", importance);
            notificationManager.createNotificationChannel(mChannel);
            n  = new Notification.Builder(c,"OpsuReminder")
                    .setContentTitle(message)
                    .setContentText("Click to Play")
                    .setSmallIcon(R.drawable.icon48dp)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true);
        }
        else{
            n  = new Notification.Builder(c)
                    .setContentTitle(message)
                    .setContentText("Click to Play")
                    .setSmallIcon(R.drawable.icon48dp)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true);

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notificationManager.notify(0, n.build());
        }
    }
    public String message(String name){
        if(Math.random()<0.5)
            if(name.length()==0)
                return "Hey come back and get a new high score on your favorite map";
            else
                return "Hey "+name+" come back and get a new high score on your favorite map";
        else
            if(name.length()==0)
                return "Hey don't forget to come back and practice!";
            else
                return "Hey "+name+" don't forget to come back and practice!";

    }


}
