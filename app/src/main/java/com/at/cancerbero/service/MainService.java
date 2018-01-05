package com.at.cancerbero.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MainService extends Service {

    private static MainService instance;

    public static MainService getInstance() {
        return instance;
    }

    public class MainBinder extends Binder {
        public MainService getService() {
            return MainService.this;
        }
    }

    protected final String TAG = getClass().getSimpleName();

    private final IBinder mBinder = new MainBinder();

//    public void delegateLogin(final LoginController loginController) {
//        new ServiceAsyncTask(this, R.string.message_invalid_credentials) {
//
//            @Override
//            public Event run() {
//                MainService.this.loginController = loginController;
//                currentUser = serverClient.delegateLogin(loginController.getSession());
//                Log.i(TAG, "Delegate login: " + currentUser);
//                return new Logged(currentUser);
//            }
//
//            @Override
//            protected void onPostExecute(Event event) {
//                super.onPostExecute(event);
//                loadMyFood(true);
//            }
//        }.execute();
//    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.i(TAG, "Create!!");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Destroy :(");
        instance = null;
    }


//    void sendEvent(Event event) {
//        MainActivity mainActivity = MainActivity.getInstance();
//        if ((mainActivity != null) && (event != null)) {
//            mainActivity.handle(event);
//        }
//    }
//
//    void sendEventUI(final Event event) {
//        final MainActivity mainActivity = MainActivity.getInstance();
//        mainActivity.runOnUiThread(new Runnable() {
//            public void run() {
//                mainActivity.handle(event);
//            }
//        });
//    }

}
