package com.at.cancerbero.domain.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.app.MainAppService;
import com.at.cancerbero.app.activities.MainActivity;
import com.at.cancerbero.app.fragments.node.NodeFragment;
import com.at.cancerbero.domain.data.repository.BackEndClient;
import com.at.cancerbero.domain.data.repository.NodesRepository;
import com.at.cancerbero.domain.model.AlarmStatus;
import com.at.cancerbero.domain.model.AlarmStatusEvent;
import com.at.cancerbero.domain.model.Node;
import com.at.cancerbero.domain.model.User;
import com.at.cancerbero.domain.service.converters.NodeConverter;
import com.at.cancerbero.domain.service.push.model.AlarmStatusChanged;
import com.at.cancerbero.domain.service.push.model.Event;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import java8.util.concurrent.CompletableFuture;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class NodeServiceRemote implements NodeService {

    private static final boolean IGNORE_HOST_VERIFICATION = false;

    protected final String TAG = getClass().getSimpleName();

    private final NodeConverter nodeConverter = new NodeConverter();

    private final SecurityService securityService;
    private MainAppService mainAppService;

    private String baseUrl;

    private final Cache<List<Node>> nodesCache = new Cache<List<Node>>(1L, TimeUnit.MINUTES) {
        @Override
        protected CompletableFuture<List<Node>> getFreshContent() {
            return securityService.getCurrentUser()
                    .thenApplyAsync(user -> nodeConverter.convert(getNodesRepository(user).loadNodes()))
                    .thenApplyAsync(nodes -> {
                        subscribeToPushNodes(nodes);
                        return nodes;
                    });
        }
    };

    public NodeServiceRemote(SecurityService securityService) {
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
    public CompletableFuture<List<Node>> loadNodes() {
        return nodesCache.getContent();
    }

    @Override
    public CompletableFuture<Node> loadNode(String nodeId) {
        return loadNodes().thenApply(nodes ->
                StreamSupport.stream(nodes).filter(node -> node.id.equals(nodeId)).findFirst().orElse(null)
        );
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

    @Override
    public void handleEvent(Event event) {
        loadNode(event.getNodeId()).handle((node, t) -> {
            if (t != null) {
                Log.e(TAG, "Unable to handle event: " + event, t);
            }

            if (node != null) {
                if (node.modules.alarm != null) {
                    if (event instanceof AlarmStatusChanged) {
                        AlarmStatusChanged alarmStatusChanged = (AlarmStatusChanged) event;
                        onStatusChange(node, alarmStatusChanged.getValue(), alarmStatusChanged.getSource());
                    }
                }
                node.handleEvent(event);
            } else {
                Log.w(TAG, "Node not found: " + event.getNodeId());
            }

            return null;
        });
    }

    private void onStatusChange(Node node, AlarmStatus newValue, String source) {
        AlarmStatus status = node.modules.alarm.status.value;
        if (status != newValue) {
            if(newValue == AlarmStatus.ALARMED){
                String title = mainAppService.getResources().getString(R.string.title_notification_node_alarmed);
                String message = MessageFormat.format(mainAppService.getResources().getString(R.string.message_notification_node_alarmed), node.name);
                sendNotification(title, message, node.id);
            }
        }
    }

    private void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            NotificationChannel channel = new NotificationChannel("default", "Channel name", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel description");
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(String title, String message, String nodeId) {
        initChannels(mainAppService);
        Intent intent = new Intent(mainAppService, MainActivity.class);
        intent.putExtra(MainActivity.CURRENT_FRAGMENT, NodeFragment.class.getName());
        intent.putExtra("nodeId", nodeId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(mainAppService, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mainAppService, "default")
                .setSmallIcon(R.drawable.logo_transparent)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent); // clear notification after click
        NotificationManager mNotificationManager = (NotificationManager) mainAppService.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }

    private void subscribeToPushNodes(List<Node> installations) {
        mainAppService.getPushService().subscribeTopics(StreamSupport.stream(installations).map(i -> "/topics/" + i.id).collect(Collectors.toSet()));
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
