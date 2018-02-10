package com.at.cancerbero.domain.service;

import com.at.cancerbero.app.MainAppService;

import java.util.Set;

public interface PushService {

    void start(MainAppService mainAppService);

    void stop();

    void subscribeTopics(Set<String> topics);

    void onTokenRefresh();
}
