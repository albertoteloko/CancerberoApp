package com.at.cancerbero.domain.service;

import android.content.Context;

import com.at.cancerbero.app.MainAppService;
import com.at.cancerbero.domain.model.Installation;
import com.at.cancerbero.domain.model.Node;

import java.util.Set;
import java.util.UUID;

import java8.util.concurrent.CompletableFuture;

public interface InstallationService {

    void start(MainAppService mainAppService);

    void stop();

    CompletableFuture<Set<Installation>> loadInstallations();

    CompletableFuture<Installation> loadInstallation(UUID installationId);

    CompletableFuture<Node> loadNode(String nodeId);

    CompletableFuture<Boolean> alarmKey(String nodeId);

    CompletableFuture<Boolean> addCard(String nodeId, String cardId, String name);

    CompletableFuture<Boolean> removeCard(String nodeId, String cardId);
}
