package com.at.cancerbero.domain.service;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.app.MainAppService;
import com.at.cancerbero.domain.data.repository.BackEndClient;
import com.at.cancerbero.domain.data.repository.InstallationRepository;
import com.at.cancerbero.domain.data.repository.NodesRepository;
import com.at.cancerbero.domain.model.Installation;
import com.at.cancerbero.domain.model.Node;
import com.at.cancerbero.domain.model.User;
import com.at.cancerbero.domain.service.converters.InstallationConverter;
import com.at.cancerbero.domain.service.converters.NodeConverter;

import java.util.Set;
import java.util.UUID;

import java8.util.concurrent.CompletableFuture;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class InstallationServiceRemote implements InstallationService {

    private static final boolean IGNORE_HOST_VERIFICATION = false;

    protected final String TAG = getClass().getSimpleName();

    private final NodeConverter nodeConverter = new NodeConverter();
    private final InstallationConverter installationConverter = new InstallationConverter(nodeConverter);

    private final SecurityService securityService;
    private MainAppService mainAppService;

    private String baseUrl;

    public InstallationServiceRemote(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Override
    public void start(MainAppService mainAppService) {
        this.mainAppService = mainAppService;
        baseUrl = mainAppService.getResources().getString(R.string.backEndUrl);
    }

    @Override
    public void stop() {
        this.mainAppService = null;
        baseUrl = null;
    }

    @Override
    public CompletableFuture<Set<Installation>> loadInstallations() {
        return securityService.getCurrentUser()
                .thenApplyAsync(user -> installationConverter.convert(getInstallationRepository(user).loadInstallations(), getNodesRepository(user)))
                .thenApplyAsync(installations -> {
                    subscribeToPushInstallations(installations);
                    return installations;
                });
    }

    private void subscribeToPushInstallations(Set<Installation> installations) {
        mainAppService.getPushService().subscribeTopics(StreamSupport.stream(installations).map(i -> "/topics/" + i.id).collect(Collectors.toSet()));
    }

    @Override
    public CompletableFuture<Installation> loadInstallation(UUID installationId) {
        return securityService.getCurrentUser()
                .thenApplyAsync(user -> installationConverter.convert(getInstallationRepository(user).loadInstallation(installationId), getNodesRepository(user)));
    }

    @Override
    public CompletableFuture<Node> loadNode(String nodeId) {
        return securityService.getCurrentUser()
                .thenApplyAsync(user -> nodeConverter.convert(getNodesRepository(user).loadNode(nodeId)));
    }

    @Override
    public CompletableFuture<Boolean> alarmKey(String nodeId) {
        return securityService.getCurrentUser()
                .thenApplyAsync(user -> getNodesRepository(user).alarmKey(nodeId));
    }

    @Override
    public CompletableFuture<Boolean> addCard(String nodeId, String cardId, String name) {
        return securityService.getCurrentUser()
                .thenApplyAsync(user -> getNodesRepository(user).addCard(nodeId, cardId, name));
    }

    @Override
    public CompletableFuture<Boolean> removeCard(String nodeId, String cardId) {
        return securityService.getCurrentUser()
                .thenApplyAsync(user -> getNodesRepository(user).removeCard(nodeId, cardId));
    }

    private InstallationRepository getInstallationRepository(User user) {
        return new InstallationRepository(getServerClient(user));
    }

    private NodesRepository getNodesRepository(User user) {
        return new NodesRepository(getServerClient(user));
    }

    private BackEndClient getServerClient(User user) {
        BackEndClient serverClient = new BackEndClient(baseUrl, IGNORE_HOST_VERIFICATION);
        serverClient.setToken(user.getToken());
        return serverClient;
    }
}
