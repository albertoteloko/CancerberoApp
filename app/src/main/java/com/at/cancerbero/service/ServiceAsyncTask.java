package com.at.cancerbero.service;

import android.os.AsyncTask;

import com.at.cancerbero.service.handlers.Event;
import com.at.cancerbero.service.handlers.ServerError;

public abstract class ServiceAsyncTask extends AsyncTask<Void, Void, Event> {

    protected final String TAG = getClass().getSimpleName();

    private final MainService service;

    public ServiceAsyncTask(MainService service) {
        this.service = service;
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
        service.sendEvent(event);
    }
}
