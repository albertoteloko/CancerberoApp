package com.at.hal9000.domain.service;

import com.at.hal9000.app.MainAppService;
import com.at.hal9000.domain.model.Node;
import com.at.hal9000.domain.service.push.model.Event;

import java.util.List;

import java8.util.concurrent.CompletableFuture;

public interface NodeService {

    void start(MainAppService mainAppService);

    void stop();

    void handleEvent(Event event);

    CompletableFuture<List<Node>> loadNodes();

    CompletableFuture<Node> loadNode(String nodeId);

    CompletableFuture<Boolean> alarmKey(String nodeId);

    CompletableFuture<Boolean> addCard(String nodeId, String cardId, String name);

    CompletableFuture<Boolean> removeCard(String nodeId, String cardId);

    CompletableFuture<Boolean> setup(String nodeId);
}
