package com.at.cancerbero.domain.service.push;

import android.content.Intent;

import com.at.cancerbero.app.MainAppService;
import com.google.android.gms.iid.InstanceIDListenerService;

public class MyInstanceIDListenerService extends InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        MainAppService.getInstance().getPushService().onTokenRefresh();
    }
}