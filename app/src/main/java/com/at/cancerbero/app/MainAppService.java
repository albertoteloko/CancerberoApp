package com.at.cancerbero.app;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.app.activities.MainActivity;
import com.at.cancerbero.domain.data.repository.BackEndClient;
import com.at.cancerbero.domain.data.repository.InstallationRepository;
import com.at.cancerbero.domain.data.repository.NodesRepository;
import com.at.cancerbero.domain.service.SecurityService;
import com.at.cancerbero.domain.service.SecurityServiceCognito;
import com.at.cancerbero.installations.async.LoadInstallation;
import com.at.cancerbero.installations.async.LoadInstallations;
import com.at.cancerbero.installations.async.LoadNode;
import com.at.cancerbero.service.async.AsyncGateway;
import com.at.cancerbero.service.events.Event;

import java.util.UUID;

public class MainAppService extends Service implements AsyncGateway {

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

    private final IBinder mBinder = new MainBinder();


    private BackEndClient serverClient;

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void loadInstallations(boolean force) {
        new LoadInstallations(this).execute();
    }

    public void loadInstallation(UUID installationId) {
        new LoadInstallation(this, installationId).execute();
    }

    public void loadNode(String nodeId) {
        new LoadNode(this, nodeId).execute();
    }

    public NodesRepository getNodesRepository() {
        return new NodesRepository(getServerClient());
    }

    public InstallationRepository getiInstallationRepository() {
        return new InstallationRepository(getServerClient());
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

        securityService.start(getApplicationContext());

        securityService.login().thenAccept(user ->
                Log.i(TAG, "User: " + user)
        );

        Context context = getApplicationContext();

        String baseUrl = context.getResources().getString(R.string.backEndUrl);
        serverClient = new BackEndClient(baseUrl, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        securityService.stop();
        Log.i(TAG, "Destroy :(");
        instance = null;
    }


    @Override
    public void sendEvent(Event event) {
        MainActivity mainActivity = MainActivity.getInstance();
        if ((mainActivity != null) && (event != null)) {
//            mainActivity.handle(event);
        }
    }

    void sendEventUI(final Event event) {
        final MainActivity mainActivity = MainActivity.getInstance();
        mainActivity.runOnUiThread(new Runnable() {
            public void run() {
//                mainActivity.handle(event);
            }
        });
    }


    private BackEndClient getServerClient() {
//        serverClient.setToken(currSession.getIdToken().getJWTToken());
        return serverClient;
    }

}
