package win.aladhims.meetme.Utility;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import win.aladhims.meetme.ButtonReceiverNotif;
import win.aladhims.meetme.DirectMeActivity;
import win.aladhims.meetme.ListFriendActivity;
import win.aladhims.meetme.R;

import static win.aladhims.meetme.NotifyMeService.AGREEEXTRA;
import static win.aladhims.meetme.NotifyMeService.NOTIFYID;

/**
 * Created by Aladhims on 23/03/2017.
 */

public class NotificationUtils {

    public static Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void NotifyMe(String inviterName, String inviterId,String meetID,Bitmap largeIcon,Context context,String myUID){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        builder.setAutoCancel(false)
                .setVibrate(new long[]{})
                .setContentTitle(inviterName + " ingin ketemuan")
                .setContentText(inviterName + " ingin ketemuan dengan anda")
                .setSmallIcon(R.drawable.ic_photo_library)
                .setOngoing(true)
                .setColor(Color.BLUE);


        builder.setLargeIcon(getCroppedBitmap(largeIcon));

        Intent yesIntent = new Intent(context,DirectMeActivity.class);
        yesIntent.putExtra(AGREEEXTRA,true);
        yesIntent.putExtra(ListFriendActivity.FRIENDUID,inviterId)
                .putExtra(ListFriendActivity.MEETID,meetID);

        Intent NoIntent = new Intent(context,ButtonReceiverNotif.class);
        NoIntent.putExtra("notificationID",NOTIFYID)
                .putExtra("invitedID",myUID);

        PendingIntent yesPendingIntent = PendingIntent.getActivity(context,0,yesIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context,1,NoIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(yesPendingIntent);

        NotificationCompat.Action actionYes = new NotificationCompat.Action(R.drawable.ic_check,"Okey!",yesPendingIntent);
        NotificationCompat.Action actionNo = new NotificationCompat.Action(R.drawable.ic_close,"Nggak!",dismissPendingIntent);
        builder.addAction(actionYes);
        builder.addAction(actionNo);
        builder.setPriority(Notification.PRIORITY_MAX);

        Notification notification = builder.build();
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_SOUND;

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(NOTIFYID,notification);
    }
}
