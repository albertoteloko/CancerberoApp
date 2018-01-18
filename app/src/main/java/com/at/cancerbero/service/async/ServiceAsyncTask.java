package com.at.cancerbero.service.async;

import android.os.AsyncTask;

import com.at.cancerbero.service.events.Event;

public abstract class ServiceAsyncTask extends AsyncTask<Void, Void, Event> {

    protected final String TAG = getClass().getSimpleName();

    protected final AsyncGateway asyncGateway;

    protected ServiceAsyncTask(AsyncGateway asyncGateway) {
        this.asyncGateway = asyncGateway;
    }

    public abstract Event run();

    @Override
    protected Event doInBackground(Void... params) {
        try {
            return run();
        } catch (Exception e) {
            e.printStackTrace();
            return new ServerError(e);
        }
    }

    @Override
    protected void onPostExecute(Event event) {
        asyncGateway.sendEvent(event);
    }
}
