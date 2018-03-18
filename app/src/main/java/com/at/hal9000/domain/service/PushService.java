package com.at.hal9000.domain.service;

import com.at.hal9000.app.MainAppService;

import java.util.Set;

public interface PushService {

    void start(MainAppService mainAppService);

    void stop();

    void subscribeTopics(Set<String> topics);

    void onTokenRefresh();
}
