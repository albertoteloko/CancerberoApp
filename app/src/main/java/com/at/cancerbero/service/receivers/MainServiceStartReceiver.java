package com.at.cancerbero.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.at.cancerbero.service.MainService;

public class MainServiceStartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, MainService.class);
        context.startService(service);
    }
}