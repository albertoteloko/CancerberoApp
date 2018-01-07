package com.at.cancerbero.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChooseMfaContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.CognitoIdentityProviderContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.NewPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.regions.Regions;
import com.at.cancerbero.activities.MainActivity;
import com.at.cancerbero.service.handlers.AuthenticationChallenge;
import com.at.cancerbero.service.handlers.Event;
import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.service.handlers.LogInFail;
import com.at.cancerbero.service.handlers.LogInSucess;
import com.at.cancerbero.service.handlers.MultiFactorAuthentication;

import java.util.Map;
import java.util.Set;

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


    private CognitoUserPool userPool;
    private CognitoDevice newDevice;
    private CognitoUser currentUser;
    private CognitoDevice device;

    private CognitoUserSession currSession;
    private CognitoUserDetails userDetails;

    private MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation;
    private ForgotPasswordContinuation forgotPasswordContinuation;
    private NewPasswordContinuation newPasswordContinuation;
    private ChooseMfaContinuation mfaOptionsContinuation;

    // User details to display - they are the current values, including any local modification
    private boolean phoneVerified;
    private boolean emailVerified;

    private boolean phoneAvailable;
    private boolean emailAvailable;

    private Set<String> currUserAttributes;

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

        Context context = getApplicationContext();
        String userPoolId = context.getResources().getString(R.string.userPoolId);
        String clientId = context.getResources().getString(R.string.clientId);
        String clientSecret = context.getResources().getString(R.string.clientSecret);
        Regions cognitoRegion = Regions.fromName(context.getResources().getString(R.string.region));

        // Create a user pool with default ClientConfiguration
        userPool = new CognitoUserPool(context, userPoolId, clientId, clientSecret, cognitoRegion);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Destroy :(");
        instance = null;
    }

    public CognitoUser getCurrentUser() {
        return currentUser;
    }

    void sendEvent(Event event) {
        MainActivity mainActivity = MainActivity.getInstance();
        if ((mainActivity != null) && (event != null)) {
            mainActivity.handle(event);
        }
    }

    void sendEventUI(final Event event) {
        final MainActivity mainActivity = MainActivity.getInstance();
        mainActivity.runOnUiThread(new Runnable() {
            public void run() {
                mainActivity.handle(event);
            }
        });
    }

    public void login() {
        AuthenticationHandler handler = getAuthenticationHandler(null, null);
        userPool.getCurrentUser().getSessionInBackground(handler);
    }

    public void login(final String email, final String password) {
        AuthenticationHandler handler = getAuthenticationHandler(email, password);
        userPool.getUser(email).getSessionInBackground(handler);
    }

    public void continueWithFirstTimeSignIn(String newPassword, Map<String, String> newAttributes) {
        if (newPasswordContinuation != null) {
            newPasswordContinuation.setPassword(newPassword);
            if (newAttributes != null) {
                for (Map.Entry<String, String> attr : newAttributes.entrySet()) {
                    Log.d(TAG, String.format(" -- Adding attribute: %s, %s", attr.getKey(), attr.getValue()));
                    newPasswordContinuation.setUserAttribute(attr.getKey(), attr.getValue());
                }
            }
            newPasswordContinuation.continueTask();
            newPasswordContinuation = null;
        } else {
            throw new IllegalStateException("There is no continue with sign in");
        }
    }

    @NonNull
    private AuthenticationHandler getAuthenticationHandler(final String email, final String password) {
        return new AuthenticationHandler() {
            @Override
            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                MainService.this.currSession = userSession;
                MainService.this.device = newDevice;
                sendEvent(new LogInSucess(userSession, newDevice));
            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
                AuthenticationDetails authenticationDetails = new AuthenticationDetails(email, password, null);
                authenticationContinuation.setAuthenticationDetails(authenticationDetails);
                authenticationContinuation.continueTask();
            }

            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation continuation) {
                sendEvent(new MultiFactorAuthentication(continuation));
            }

            @Override
            public void authenticationChallenge(ChallengeContinuation continuation) {
                setChallengeContinuation(continuation);
                sendEvent(new AuthenticationChallenge(continuation));
            }

            @Override
            public void onFailure(Exception exception) {
                sendEvent(new LogInFail(exception));
            }
        };
    }

    public NewPasswordContinuation getNewPasswordContinuation() {
        return newPasswordContinuation;
    }

    private void setChallengeContinuation(ChallengeContinuation continuation) {
        if (continuation instanceof NewPasswordContinuation) {
            newPasswordContinuation = (NewPasswordContinuation) continuation;
        } else if (continuation instanceof ChooseMfaContinuation) {
            mfaOptionsContinuation = (ChooseMfaContinuation) continuation;
        }
    }


//    AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
//        @Override
//        public void onSuccess(CognitoUserSession cognitoUserSession, CognitoDevice device) {
//            Log.d(TAG, " -- Auth Success");
//            MainService.this.currSession = cognitoUserSession;
//            MainService.this.device = device;
//            sendEvent(new LogInSucess(cognitoUserSession, device));
//        }
//
//        @Override
//        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String username) {
//            Locale.setDefault(Locale.US);
//            getUserAuthentication(authenticationContinuation, username);
//        }
//
//        @Override
//        public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {
//            mfaAuth(multiFactorAuthenticationContinuation);
//        }
//
//        @Override
//        public void onFailure(Exception e) {
//            closeWaitDialog();
//            TextView label = (TextView) findViewById(R.id.textViewUserIdMessage);
//            label.setText("Sign-in failed");
//            inPassword.setBackground(getDrawable(R.drawable.text_border_error));
//
//            label = (TextView) findViewById(R.id.textViewUserIdMessage);
//            label.setText("Sign-in failed");
//            inUsername.setBackground(getDrawable(R.drawable.text_border_error));
//
//            showDialogMessage("Sign-in failed", AppHelper.formatException(e));
//        }
//
//        @Override
//        public void authenticationChallenge(ChallengeContinuation continuation) {
//            /**
//             * For Custom authentication challenge, implement your logic to present challenge to the
//             * user and pass the user's responses to the continuation.
//             */
//            if ("NEW_PASSWORD_REQUIRED".equals(continuation.getChallengeName())) {
//                // This is the first sign-in attempt for an admin created user
//                newPasswordContinuation = (NewPasswordContinuation) continuation;
//                AppHelper.setUserAttributeForDisplayFirstLogIn(newPasswordContinuation.getCurrentUserAttributes(),
//                        newPasswordContinuation.getRequiredAttributes());
//                closeWaitDialog();
//                firstTimeSignIn();
//            } else if ("SELECT_MFA_TYPE".equals(continuation.getChallengeName())) {
//                closeWaitDialog();
//                mfaOptionsContinuation = (ChooseMfaContinuation) continuation;
//                List<String> mfaOptions = mfaOptionsContinuation.getMfaOptions();
//                selectMfaToSignIn(mfaOptions, continuation.getParameters());
//            }
//        }
//    };
//
//    private void getUserAuthentication(AuthenticationContinuation continuation, String username) {
//        if (username != null) {
//            this.username = username;
//            AppHelper.setUser(username);
//        }
//        if (this.password == null) {
//            inUsername.setText(username);
//            password = inPassword.getText().toString();
//            if (password == null) {
//                TextView label = (TextView) findViewById(R.id.textViewUserPasswordMessage);
//                label.setText(inPassword.getHint() + " enter password");
//                inPassword.setBackground(getDrawable(R.drawable.text_border_error));
//                return;
//            }
//
//            if (password.length() < 1) {
//                TextView label = (TextView) findViewById(R.id.textViewUserPasswordMessage);
//                label.setText(inPassword.getHint() + " enter password");
//                inPassword.setBackground(getDrawable(R.drawable.text_border_error));
//                return;
//            }
//        }
//        AuthenticationDetails authenticationDetails = new AuthenticationDetails(this.username, password, null);
//        continuation.setAuthenticationDetails(authenticationDetails);
//        continuation.continueTask();
//    }
}
