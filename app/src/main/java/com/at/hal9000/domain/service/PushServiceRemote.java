package com.at.hal9000.domain.service;

import android.os.AsyncTask;
import android.util.Log;

import com.at.hal9000.app.MainAppService;
import com.google.firebase.messaging.FirebaseMessaging;

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
        if (!this.topics.equals(topics)) {
            unsubscribeTopics();
            this.topics = topics;
            subscribeTopics();
        }
    }

    @Override
    public void onTokenRefresh() {
        subscribeTopics();
    }

    private void subscribeTopics() {
        try {
            FirebaseMessaging instance = FirebaseMessaging.getInstance();
            for (String topic : topics) {
                Log.i(TAG, "Subscribing to: " + topic);
                instance.subscribeToTopic(topic);
            }
        } catch (Exception e) {
            Log.e(TAG, "Unable to subscribe topics", e);
        }
    }

    private void unsubscribeTopics() {
        try {
            FirebaseMessaging instance = FirebaseMessaging.getInstance();
            for (String topic : topics) {
                instance.unsubscribeFromTopic(topic);
            }
        } catch (Exception e) {
            Log.e(TAG, "Unable to unsubscribe topics", e);
        }
    }
}
