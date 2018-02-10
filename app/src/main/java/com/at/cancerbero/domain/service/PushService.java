package com.at.cancerbero.domain.service;

import com.at.cancerbero.app.MainAppService;

public interface PushService {

    void start(MainAppService mainAppService);

    void stop();
}
