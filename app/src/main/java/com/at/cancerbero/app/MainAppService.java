package com.at.cancerbero.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.app.activities.MainActivity;
import com.at.cancerbero.domain.service.InstallationService;
import com.at.cancerbero.domain.service.InstallationServiceRemote;
import com.at.cancerbero.domain.service.PushService;
import com.at.cancerbero.domain.service.PushServiceRemote;
import com.at.cancerbero.domain.service.SecurityService;
import com.at.cancerbero.domain.service.SecurityServiceCognito;
import com.at.cancerbero.domain.service.push.AlarmStatusChanged;
import com.at.cancerbero.domain.service.push.Event;

public class MainAppService extends Service {

    private static MainAppService instance;

    public static MainAppService getInstance() {
        return instance;
    }

    public class MainBinder extends Binder {
        public MainAppService getService() {
            return MainAppService.this;
        }
    }

    protected final String TAG = getClass().getSimpleName();

    private final SecurityService securityService = new SecurityServiceCognito();

    private final InstallationService installationService = new InstallationServiceRemote(securityService);

    private final PushService pushService = new PushServiceRemote();

    private final IBinder mBinder = new MainBinder();

    public SecurityService getSecurityService() {
        return securityService;
    }

    public InstallationService getInstallationService() {
        return installationService;
    }

    public PushService getPushService() {
        return pushService;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.i(TAG, "Create!!");

        securityService.start(this);
        pushService.start(this);
        installationService.start(this);

        securityService.login().thenAccept(user -> {
                    Log.i(TAG, "User: " + user);
                    installationService.loadInstallations();
                }
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        installationService.stop();
        pushService.stop();
        securityService.stop();
        Log.i(TAG, "Destroy :(");
        instance = null;
    }


    public void handleEvent(Event event) {
        if (event instanceof AlarmStatusChanged) {
            sendNodeAlarmed(event.getNodeId());
        }
    }

    private void sendNodeAlarmed(String nodeId) {

    }

    private void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            NotificationChannel channel = new NotificationChannel("default", "Channel name", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel description");
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(String title, String message) {
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
