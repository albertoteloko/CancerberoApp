package com.at.hal9000.domain.service.push;

import com.at.hal9000.app.MainAppService;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyInstanceIDListenerService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        MainAppService.getInstance().getPushService().onTokenRefresh();
    }
}