package com.at.cancerbero.domain.service.push;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.app.activities.MainActivity;
import com.at.cancerbero.app.MainAppService;
import com.at.cancerbero.domain.data.repository.server.ServerConnector;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.gcm.GcmListenerService;

import java.io.IOException;
import java.util.HashSet;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    private final ObjectMapper mapper = ServerConnector.defaultObjectMapper();

    @Override
    public void onMessageReceived(String from, Bundle data) {

        String eventString = data.getString("event");
        Log.d(TAG, "Event String: " + eventString);
        try {
            Event event = mapper.readValue(eventString, Event.class);
            Log.d(TAG, "Event: " + event);

            MainAppService service = MainAppService.getInstance();
//
            if (service != null) {
                service.handleEvent(event);
            }
//

        } catch (Exception e) {
            Log.e(TAG, "Unable to process push notification: " + eventString, e);
        }


//        MainAppService service = MainAppService.getInstance();
//
//        if (service != null) {
////            service.onNodeStatusChange()
//        }
//
//        sendNotification2("hello", "world");
    }


    public void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("default", "Channel name", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Channel description");
        notificationManager.createNotificationChannel(channel);
    }

    private void sendNotification2(String title, String message) {
        initChannels(this);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.drawable.logo_transparent)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent); // clear notification after click
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }
}