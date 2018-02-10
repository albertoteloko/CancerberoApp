package com.at.cancerbero.domain.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.at.cancerbero.app.MainAppService;
import com.at.cancerbero.domain.service.push.QuickstartPreferences;
import com.at.cancerbero.domain.service.push.RegistrationIntentService;

public class PushServiceRemote implements PushService {

    protected final String TAG = getClass().getSimpleName();

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private boolean isReceiverRegistered;

    private MainAppService mainAppService;

    @Override
    public void start(MainAppService mainAppService) {
        this.mainAppService = mainAppService;

        Intent serviceIntent = new Intent(mainAppService, RegistrationIntentService.class);
        mainAppService.startService(serviceIntent);

        mainAppService.bindService(serviceIntent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mainAppService.unbindService(this);

                mRegistrationBroadcastReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                        sharedPreferences.getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                    }
                };
                registerReceiver();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void stop() {
        if (mainAppService != null) {
            LocalBroadcastManager.getInstance(mainAppService).unregisterReceiver(mRegistrationBroadcastReceiver);
            isReceiverRegistered = false;
        }
    }


    private void registerReceiver() {
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(mainAppService).registerReceiver(
                    mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE)
            );
            isReceiverRegistered = true;
        }
    }
}
