package com.at.cancerbero.domain.service;

import android.content.Context;

import com.at.cancerbero.domain.model.Installation;
import com.at.cancerbero.domain.model.Node;

import java.util.Set;
import java.util.UUID;

import java8.util.concurrent.CompletableFuture;

public interface InstallationService {

    void start(Context context);

    void stop();

    CompletableFuture<Set<Installation>> loadInstallations();

    CompletableFuture<Installation> loadInstallation(UUID installationId);

    CompletableFuture<Node> loadNode(String nodeId);

}
