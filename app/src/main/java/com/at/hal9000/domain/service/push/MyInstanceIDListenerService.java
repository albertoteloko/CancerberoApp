package com.at.hal9000.domain.service.push;

import com.at.hal9000.app.MainAppService;
import com.google.android.gms.iid.InstanceIDListenerService;

public class MyInstanceIDListenerService extends InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        MainAppService.getInstance().getPushService().onTokenRefresh();
    }
}