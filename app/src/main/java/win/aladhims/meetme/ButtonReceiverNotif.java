package win.aladhims.meetme;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Aladhims on 22/03/2017.
 */

public class ButtonReceiverNotif extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int notificationID = intent.getIntExtra("notificationID",0);


        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationID);

    }
}
