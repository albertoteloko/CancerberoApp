package com.at.cancerbero.app;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.at.cancerbero.domain.service.InstallationService;
import com.at.cancerbero.domain.service.InstallationServiceRemote;
import com.at.cancerbero.domain.service.PushService;
import com.at.cancerbero.domain.service.PushServiceRemote;
import com.at.cancerbero.domain.service.SecurityService;
import com.at.cancerbero.domain.service.SecurityServiceCognito;
import com.at.cancerbero.domain.service.push.QuickstartPreferences;
import com.at.cancerbero.domain.service.push.RegistrationIntentService;

import java.util.UUID;

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

        Context context = getApplicationContext();
        securityService.start(context);
        pushService.start(this);
        installationService.start(context);

        securityService.login().thenAccept(user ->
                Log.i(TAG, "User: " + user)
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
}
