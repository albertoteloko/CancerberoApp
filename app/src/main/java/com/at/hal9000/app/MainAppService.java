package com.at.hal9000.app;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.at.hal9000.domain.service.NodeService;
import com.at.hal9000.domain.service.NodeServiceRemote;
import com.at.hal9000.domain.service.PushService;
import com.at.hal9000.domain.service.PushServiceRemote;
import com.at.hal9000.domain.service.SecurityService;
import com.at.hal9000.domain.service.SecurityServiceCognito;

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

    private final NodeService nodeService = new NodeServiceRemote(securityService);

    private final PushService pushService = new PushServiceRemote();

    private final IBinder mBinder = new MainBinder();

    public SecurityService getSecurityService() {
        return securityService;
    }

    public NodeService getNodeService() {
        return nodeService;
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
        nodeService.start(this);

        securityService.login().thenAccept(user -> {
                    Log.i(TAG, "User: " + user);
                    nodeService.loadNodes();
                }
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        nodeService.stop();
        pushService.stop();
        securityService.stop();
        Log.i(TAG, "Destroy :(");
        instance = null;
    }
}
