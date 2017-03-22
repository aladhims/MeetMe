package win.aladhims.meetme;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import win.aladhims.meetme.Model.User;

/**
 * Created by Aladhims on 21/03/2017.
 */

public class NotifyMeService extends IntentService {

    public static final int NOTIFYID = 1;
    public static final String AGREEEXTRA = "AGREEEXTRA";

    private DatabaseReference rootRef,checkRef;
    private Context context;
    private String friendID,friendName,meetID,userUID ;
    public NotifyMeService() {
        super("NotifyMeService");
    }

    @Override
    protected void onHandleIntent(@Nullable final Intent intent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                rootRef = FirebaseDatabase.getInstance().getReference();
                context = getApplicationContext();
                userUID = intent.getStringExtra(ListFriendActivity.MYUIDEXTRAINTENT);
                checkRef = rootRef.child("invite").child(userUID);
                checkRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        //TODO ADA BUG DI FRIEND ID SM MEETID nilainya doi null, salah di pathnya mungkin
                        friendID = "Gbg4SOOPnuOY39sLXlPf9drGgMr2";
                        if(friendID == null){
                            Log.d("SERVICE","friendid is " + friendID);
                        }
                        meetID = (String) dataSnapshot.child("meetID").getValue();
                        rootRef.child("users").child(friendID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                User user = dataSnapshot.getValue(User.class);
                                friendName = user.getName();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        NotifyMe(friendName, friendID, "1");
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {}
                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
        }).run();
    }

    private void NotifyMe(String inviterName, String inviterId,String meetID){

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_send);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setColor(ContextCompat.getColor(context,R.color.colorPrimary));
        builder.setAutoCancel(true)
                .setContentTitle(inviterName + " ingin ketemuan")
                .setContentText(inviterName + " ingin ketemuan dengan anda")
                .setSmallIcon(R.drawable.ic_photo_library)
                .setLargeIcon(bitmap);

        Intent yesIntent = new Intent(context,DirectMeActivity.class);
        yesIntent.putExtra(AGREEEXTRA,true);
        yesIntent.putExtra(ListFriendActivity.FRIENDUID,inviterId)
                .putExtra(ListFriendActivity.MEETID,meetID);

        Intent NoIntent = new Intent(context,ButtonReceiverNotif.class);
        NoIntent.putExtra("notificationID",NOTIFYID);

        PendingIntent yesPendingIntent = PendingIntent.getActivity(context,0,yesIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context,1,NoIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action actionYes = new NotificationCompat.Action(R.drawable.ic_send,"Okey!",yesPendingIntent);
        NotificationCompat.Action actionNo = new NotificationCompat.Action(R.drawable.ic_photo_library,"Nggak!",dismissPendingIntent);
        builder.addAction(actionYes);
        builder.addAction(actionNo);
        builder.setPriority(Notification.PRIORITY_HIGH);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(NOTIFYID,builder.build());
    }
}
