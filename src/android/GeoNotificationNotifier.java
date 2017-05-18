package com.cowbell.cordova.geofence;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.vipvip.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class GeoNotificationNotifier {
    private NotificationManager notificationManager;
    private Context context;
    private BeepHelper beepHelper;
    private Logger logger;

    public GeoNotificationNotifier(NotificationManager notificationManager, Context context) {
        this.notificationManager = notificationManager;
        this.context = context;
        this.beepHelper = new BeepHelper();
        this.logger = Logger.getLogger();
    }

    public void notify(Notification notification) throws IOException, JSONException {
        //color company
        String str = notification.getText();
        JSONObject j =new JSONObject(notification.getDataJson());
        String company = j.getString("name");

        String str_end = str.substring(str.indexOf(company)+company.length());
        String str_first = str.substring(0,str.indexOf(company));
        String color1 = "<font color=\"#ff0000\">";
        String color2 = "</font>";
        String new_str = str_first.concat(color1).concat(company).concat(color2).concat(str_end);

        JSONObject _url = new JSONObject(notification.getDataJson());


        RemoteViews expandedView = new RemoteViews(context.getPackageName(), R.layout.custom_notification);


        if(_url.getString("company_logo")!=""){
            //Loading image from URL
            URL newurl = new URL(_url.getString("company_logo"));
            Bitmap mIcon_val = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());



            expandedView.setTextViewText(R.id.notification_text, Html.fromHtml(new_str));

            //set custom image from URL

            expandedView.setBitmap(R.id.imageView2,"setImageBitmap",mIcon_val);
        }




        notification.setContext(context);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
            .setVibrate(notification.getVibrate())
            .setSmallIcon(notification.getSmallIcon())
            .setLargeIcon(notification.getLargeIcon())
            .setAutoCancel(true)
            .setContentTitle(notification.getTitle())
            .setContentText(notification.getText());





        if (notification.openAppOnClick) {
            String packageName = context.getPackageName();
            Intent resultIntent = context.getPackageManager()
                .getLaunchIntentForPackage(packageName);

            if (notification.data != null) {
                resultIntent.putExtra("geofence.notification.data", notification.getDataJson());
            }

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                notification.id, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
        }
        try {
            Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notificationSound);
            r.play();
        } catch (Exception e) {
            beepHelper.startTone("beep_beep_beep");
            e.printStackTrace();
        }
        notificationManager.notify(notification.id, mBuilder.setCustomBigContentView(expandedView).build());
        logger.log(Log.DEBUG, notification.toString());
    }


}
