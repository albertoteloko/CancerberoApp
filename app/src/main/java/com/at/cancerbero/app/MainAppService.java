package com.at.cancerbero.app;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.at.cancerbero.domain.service.InstallationService;
import com.at.cancerbero.domain.service.InstallationServiceRemote;
import com.at.cancerbero.domain.service.SecurityService;
import com.at.cancerbero.domain.service.SecurityServiceCognito;

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

    private final IBinder mBinder = new MainBinder();


    public SecurityService getSecurityService() {
        return securityService;
    }

    public InstallationService getInstallationService() {
        return installationService;
    }

    public void loadInstallation(UUID installationId) {
    }

    public void loadNode(String nodeId) {
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
        installationService.start(context);

        securityService.login().thenAccept(user ->
                Log.i(TAG, "User: " + user)
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        installationService.stop();
        securityService.stop();
        Log.i(TAG, "Destroy :(");
        instance = null;
    }
}
