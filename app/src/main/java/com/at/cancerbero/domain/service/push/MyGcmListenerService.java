package com.at.cancerbero.domain.service.push;

import android.os.Bundle;
import android.util.Log;

import com.at.cancerbero.app.MainAppService;
import com.at.cancerbero.domain.data.repository.server.ServerConnector;
import com.at.cancerbero.domain.service.push.model.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.gcm.GcmListenerService;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    private final ObjectMapper mapper = ServerConnector.defaultObjectMapper();

    @Override
    public void onMessageReceived(String from, Bundle data) {

        String eventString = data.getString("event");
        Log.d(TAG, "Event String: " + eventString);
        try {
            Event event = mapper.readValue(eventString, Event.class);
            Log.d(TAG, "Event: " + event);

            MainAppService service = MainAppService.getInstance();

            if (service != null) {
                service.getNodeService().handleEvent(event);
            }
        } catch (Exception e) {
            Log.e(TAG, "Unable to process push notification: " + eventString, e);
        }
    }
}