package com.at.hal9000.domain.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.at.hal9000.Hal9000App.R;
import com.at.hal9000.app.MainAppService;
import com.at.hal9000.app.activities.MainActivity;
import com.at.hal9000.app.fragments.node.NodeFragment;
import com.at.hal9000.domain.data.repository.BackEndClient;
import com.at.hal9000.domain.data.repository.NodesRepository;
import com.at.hal9000.domain.model.AlarmStatus;
import com.at.hal9000.domain.model.Node;
import com.at.hal9000.domain.model.User;
import com.at.hal9000.domain.service.converters.NodeConverter;
import com.at.hal9000.domain.service.push.model.AlarmStatusChanged;
import com.at.hal9000.domain.service.push.model.Event;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java8.util.concurrent.CompletableFuture;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class NodeServiceRemote implements NodeService {

    private static final boolean IGNORE_HOST_VERIFICATION = false;

    protected final String TAG = getClass().getSimpleName();

    private final NodeConverter nodeConverter = new NodeConverter();

    private final Map<String, AlarmStatus> nodeStatuses = new HashMap<>();

    private final SecurityService securityService;
    private MainAppService mainAppService;

    private String baseUrl;

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
    public synchronized CompletableFuture<List<Node>> loadNodes() {
        return securityService.getCurrentUser()
                .thenApplyAsync(user -> nodeConverter.convert(getNodesRepository(user).loadNodes()))
                .thenApplyAsync(nodes -> {
                    subscribeToPushNodes(nodes);
                    checkNodesNotifications(nodes);
                    return nodes;
                });
    }

    @Override
    public synchronized CompletableFuture<Node> loadNode(String nodeId) {
        return securityService.getCurrentUser()
                .thenApplyAsync(user -> nodeConverter.convert(getNodesRepository(user).loadNode(nodeId)))
                .thenApplyAsync(nodes -> {
                    subscribeToPushNode(nodes);
                    onStatusChange(nodes);
                    return nodes;
                });
    }

    @Override
    public CompletableFuture<Boolean> alarmKey(String nodeId) {
        return securityService.getCurrentUser()
                .thenApplyAsync(user -> getNodesRepository(user).alarmKey(nodeId));
    }

    @Override
    public CompletableFuture<Boolean> setup(String nodeId) {
        return securityService.getCurrentUser()
                .thenApplyAsync(user -> getNodesRepository(user).setup(nodeId));
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
                if (MainActivity.getInstance() != null) {
                    MainActivity.getInstance().onNodeChanged(node);
                }
                if (event instanceof AlarmStatusChanged) {
                    onStatusChange(node);
                }
            } else {
                Log.w(TAG, "Node not found: " + event.getNodeId());
            }

            return null;
        });
    }

    private void checkNodesNotifications(List<Node> nodes) {
        StreamSupport.stream(nodes).forEach(this::onStatusChange);
    }

    private void onStatusChange(Node node) {
        AlarmStatus status = node.modules.alarm.status.value;

        AlarmStatus oldStatus = nodeStatuses.get(node.id);

        if ((oldStatus == null) || (status != oldStatus)) {
            if (status == AlarmStatus.ALARMED) {
                String title = mainAppService.getResources().getString(R.string.title_notification_node_alarmed);
                String message = MessageFormat.format(mainAppService.getResources().getString(R.string.message_notification_node_alarmed), node.name);
                sendNotification(node, title, message);
            } else if (status == AlarmStatus.SABOTAGE) {
                String title = mainAppService.getResources().getString(R.string.title_notification_node_sabotage);
                String message = MessageFormat.format(mainAppService.getResources().getString(R.string.message_notification_node_sabotage), node.name);
                sendNotification(node, title, message);
            } else if (status == AlarmStatus.SAFETY) {
                String title = mainAppService.getResources().getString(R.string.title_notification_node_safety);
                String message = MessageFormat.format(mainAppService.getResources().getString(R.string.message_notification_node_safety), node.name);
                sendNotification(node, title, message);
            } else {
                silentChannels(node);
            }
            nodeStatuses.put(node.id, status);
        }
    }

    private void initChannels(Context context, Node node) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            NotificationChannel channel = new NotificationChannel(node.id, node.name, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel description");
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void silentChannels(Node node) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager = (NotificationManager) mainAppService.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.cancel(node.id.hashCode());
        }
    }

    private void sendNotification(Node node, String title, String message) {
        initChannels(mainAppService, node);
        Intent intent = new Intent(mainAppService, MainActivity.class);
        intent.putExtra(MainActivity.CURRENT_FRAGMENT, NodeFragment.class.getName());
        intent.putExtra("nodeId", node.id);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(mainAppService, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mainAppService, "default")
                .setSmallIcon(R.drawable.logo_transparent)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent); // clear notification after click
        NotificationManager mNotificationManager = (NotificationManager) mainAppService.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(node.id.hashCode(), mBuilder.build());
    }

    private void subscribeToPushNodes(List<Node> nodes) {
        mainAppService.getPushService().subscribeTopics(StreamSupport.stream(nodes).map(i -> "/topics/" + i.id).collect(Collectors.toSet()));
    }

    private void subscribeToPushNode(Node node) {
        mainAppService.getPushService().subscribeTopic("/topics/" + node.id);
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
