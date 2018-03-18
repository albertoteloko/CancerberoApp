package com.at.hal9000.domain.service;

import android.os.AsyncTask;
import android.util.Log;

import com.at.hal9000.Hal9000App.R;
import com.at.hal9000.app.MainAppService;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class PushServiceRemote implements PushService {

    private static final String GLOBAL_TOPIC = "/topics/global";

    protected final String TAG = getClass().getSimpleName();

    private String token;

    private MainAppService mainAppService;

    private Set<String> topics = new HashSet<>();

    @Override
    public void start(MainAppService mainAppService) {
        this.mainAppService = mainAppService;

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                onTokenRefresh();
                return null;
            }

            @Override
            protected void onPostExecute(Void msg) {
            }
        }.execute(null, null, null);

    }

    @Override
    public void stop() {
        if (mainAppService != null) {
            unsubscribeTopics();
            token = null;
            topics.clear();
        }
    }

    @Override
    public void subscribeTopics(Set<String> topics) {
        if (token != null) {
            unsubscribeTopics();
            this.topics = topics;
            subscribeTopics();
        } else {

            this.topics = topics;
        }
    }

    @Override
    public void subscribeTopic(String topic) {
        this.topics.add(topic);
        if (token != null) {
            subscribeTopics();
        }
    }

    @Override
    public void onTokenRefresh() {
        try {
            InstanceID instanceID = InstanceID.getInstance(mainAppService);
            token = instanceID.getToken(
                    mainAppService.getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE,
                    null
            );
            Log.i(TAG, "GCM Registration Token: " + token);

            subscribeTopics();
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
        }
    }

    private void subscribeTopics() {
        try {
            GcmPubSub pubSub = GcmPubSub.getInstance(mainAppService);
            pubSub.subscribe(token, GLOBAL_TOPIC, null);
            for (String topic : topics) {
                Log.i(TAG, "Subscribing to: " + topic);
                pubSub.subscribe(token, topic, null);
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to subscribe topics", e);
        }
    }

    private void unsubscribeTopics() {
        try {
            GcmPubSub pubSub = GcmPubSub.getInstance(mainAppService);
            pubSub.unsubscribe(token, GLOBAL_TOPIC);
            for (String topic : topics) {
                pubSub.unsubscribe(token, topic);
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to unsubscribe topics", e);
        }
    }
}
