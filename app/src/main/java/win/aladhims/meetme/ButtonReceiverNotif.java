package win.aladhims.meetme;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Aladhims on 22/03/2017.
 */

public class ButtonReceiverNotif extends BroadcastReceiver {

    DatabaseReference inviteRef;

    @Override
    public void onReceive(Context context, Intent intent) {

        int notificationID = intent.getIntExtra("notificationID",0);
        String myUID = intent.getStringExtra("invitedID");
        inviteRef = FirebaseDatabase.getInstance().getReference().child("invite").child(myUID);
        inviteRef.removeValue();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationID);

    }
}
