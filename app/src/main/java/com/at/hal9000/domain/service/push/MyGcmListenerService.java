package com.at.hal9000.domain.service.push;

import android.util.Log;

import com.at.hal9000.app.MainAppService;
import com.at.hal9000.domain.data.repository.server.ServerConnector;
import com.at.hal9000.domain.service.push.model.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyGcmListenerService extends FirebaseMessagingService {

    private static final String TAG = "MyGcmListenerService";

    private final ObjectMapper mapper = ServerConnector.defaultObjectMapper();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
        String eventString = remoteMessage.getData().get("event");
            Log.d(TAG, "Event String: " + eventString);
            Event event = mapper.readValue(eventString, Event.class);
            Log.d(TAG, "Event: " + event);

            MainAppService service = MainAppService.getInstance();

            if (service != null) {
                service.getNodeService().handleEvent(event);
            }
        } catch (Exception e) {
            Log.e(TAG, "Unable to process push notification: " + remoteMessage.getData(), e);
        }
    }
}