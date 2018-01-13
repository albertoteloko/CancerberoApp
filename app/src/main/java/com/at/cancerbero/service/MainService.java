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
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.NewPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.regions.Regions;
import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.activities.MainActivity;
import com.at.cancerbero.installations.async.LoadInstallation;
import com.at.cancerbero.installations.repository.BackEndClient;
import com.at.cancerbero.installations.repository.InstallationRepository;
import com.at.cancerbero.installations.repository.NodesRepository;
import com.at.cancerbero.service.async.AsyncGateway;
import com.at.cancerbero.installations.async.LoadInstallations;
import com.at.cancerbero.service.events.AuthenticationChallenge;
import com.at.cancerbero.service.events.ChangePasswordFail;
import com.at.cancerbero.service.events.ChangePasswordSuccess;
import com.at.cancerbero.service.events.Event;
import com.at.cancerbero.service.events.ForgotPasswordFail;
import com.at.cancerbero.service.events.ForgotPasswordStart;
import com.at.cancerbero.service.events.ForgotPasswordSuccess;
import com.at.cancerbero.service.events.LogInFail;
import com.at.cancerbero.service.events.LogInSuccess;
import com.at.cancerbero.service.events.Logout;
import com.at.cancerbero.service.events.MultiFactorAuthentication;
import com.at.cancerbero.service.events.UserDetailsFail;
import com.at.cancerbero.service.events.UserDetailsSuccess;

import java.util.Map;
import java.util.UUID;

public class MainService extends Service implements AsyncGateway {

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
    private CognitoDevice device;

    private CognitoUserSession currSession;
    private CognitoUserDetails userDetails;

    private MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation;
    private ForgotPasswordContinuation forgotPasswordContinuation;
    private NewPasswordContinuation newPasswordContinuation;
    private ChooseMfaContinuation mfaOptionsContinuation;

    private BackEndClient serverClient;

    public void loadInstallations() {
        new LoadInstallations(this).execute();
    }

    public void loadInstallation(UUID installationId) {
        new LoadInstallation(this, installationId).execute();
    }

    public NodesRepository getNodesRepository() {
        return new NodesRepository(getServerClient());
    }

    public InstallationRepository getiInstallationRepository() {
        return new InstallationRepository(getServerClient());
    }

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

        String baseUrl = context.getResources().getString(R.string.backEndUrl);
        serverClient = new BackEndClient(baseUrl, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Destroy :(");
        instance = null;
    }

    public CognitoUser getCurrentUser() {
        return userPool.getCurrentUser();
    }

    public CognitoUserDetails getUserDetails() {
        return userDetails;
    }

    @Override
    public void sendEvent(Event event) {
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

    public void forgotPassword(final String userId) {
        userPool.getUser(userId).forgotPasswordInBackground(new ForgotPasswordHandler() {
            @Override
            public void onSuccess() {
                forgotPasswordContinuation = null;
                sendEvent(new ForgotPasswordSuccess(userId));
            }

            @Override
            public void getResetCode(ForgotPasswordContinuation continuation) {
                forgotPasswordContinuation = continuation;
                sendEvent(new ForgotPasswordStart(userId, continuation));
            }

            @Override
            public void onFailure(Exception exception) {
                sendEvent(new ForgotPasswordFail(userId, exception));
            }
        });
    }


    public void changePasswordForgotten(String newPassword, String verCode) {
        if (forgotPasswordContinuation != null) {
            forgotPasswordContinuation.setPassword(newPassword);
            forgotPasswordContinuation.setVerificationCode(verCode);
            forgotPasswordContinuation.continueTask();
        }
    }

    public void login() {
        AuthenticationHandler handler = getAuthenticationHandler(null, null);
        userPool.getCurrentUser().getSessionInBackground(handler);
    }

    public void login(final String email, final String password) {
        AuthenticationHandler handler = getAuthenticationHandler(email, password);
        userPool.getUser(email).getSessionInBackground(handler);
    }

    public void changePassword(String oldPassword, String newPassword) {
        userPool.getCurrentUser().changePasswordInBackground(oldPassword, newPassword, new GenericHandler() {
            @Override
            public void onSuccess() {
                sendEvent(new ChangePasswordSuccess());
            }

            @Override
            public void onFailure(Exception exception) {
                sendEvent(new ChangePasswordFail(exception));
            }
        });
    }

    public void logout() {
        if (getCurrentUser() != null) {
            getCurrentUser().signOut();
            sendEvent(new Logout());
        }
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
                newPasswordContinuation = null;
                sendEvent(new LogInSuccess(userSession, newDevice));
                loadUserDetails();
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

    public void loadUserDetails() {
        userPool.getCurrentUser().getDetailsInBackground(new GetDetailsHandler() {
            @Override
            public void onSuccess(CognitoUserDetails cognitoUserDetails) {
                MainService.this.userDetails = cognitoUserDetails;
                sendEvent(new UserDetailsSuccess(cognitoUserDetails));
            }

            @Override
            public void onFailure(Exception exception) {
                sendEvent(new UserDetailsFail(exception));
            }
        });
    }

    public NewPasswordContinuation getNewPasswordContinuation() {
        return newPasswordContinuation;
    }

    public ForgotPasswordContinuation getForgotPasswordContinuation() {
        return forgotPasswordContinuation;
    }

    private void setChallengeContinuation(ChallengeContinuation continuation) {
        if (continuation instanceof NewPasswordContinuation) {
            newPasswordContinuation = (NewPasswordContinuation) continuation;
        } else if (continuation instanceof ChooseMfaContinuation) {
            mfaOptionsContinuation = (ChooseMfaContinuation) continuation;
        }
    }

    private BackEndClient getServerClient() {
        serverClient.setToken(currSession.getIdToken().getJWTToken());
        return serverClient;
    }

}
