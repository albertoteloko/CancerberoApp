package com.at.cancerbero.domain.service;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
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
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.regions.Regions;
import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.app.MainAppService;
import com.at.cancerbero.domain.model.domain.User;
import com.at.cancerbero.domain.service.exceptions.ChooseMfaContinuationRequired;
import com.at.cancerbero.domain.service.exceptions.MFAAuthenticationRequired;
import com.at.cancerbero.domain.service.exceptions.NewPasswordRequired;
import com.at.cancerbero.service.events.ForgotPasswordStart;
import com.at.cancerbero.service.events.ForgotPasswordSuccess;
import com.at.cancerbero.service.events.UserDetailsFail;
import com.at.cancerbero.service.events.UserDetailsSuccess;

import java8.util.Optional;
import java8.util.concurrent.CompletableFuture;

public class SecurityServiceCognito implements SecurityService, AuthenticationHandler {


    protected final String TAG = getClass().getSimpleName();

    private Context context;

    private CognitoUserPool cognitoUserPool;
    private CognitoDevice cognitoDevice;

    private CognitoUserSession cognitoUserSession;
    private CognitoUserDetails cognitoUserDetails;

    private MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation;
    private ForgotPasswordContinuation forgotPasswordContinuation;
    private NewPasswordContinuation newPasswordContinuation;
    private ChooseMfaContinuation mfaOptionsContinuation;

    private Optional<User> currentUser;

    private CompletableFuture<User> loginFuture;
    private String currentUserId;
    private String currentUsePassword;

    private ReentrantLock lock;

    @Override
    public void start(Context context) {
        this.context = context;

        String userPoolId = context.getResources().getString(R.string.userPoolId);
        String clientId = context.getResources().getString(R.string.clientId);
        String clientSecret = context.getResources().getString(R.string.clientSecret);
        Regions regions = Regions.fromName(context.getResources().getString(R.string.region));

        // Create a user pool with default ClientConfiguration
        cognitoUserPool = new CognitoUserPool(context, userPoolId, clientId, clientSecret, regions);
    }

    @Override
    public void stop() {
        cognitoUserPool = null;
    }

    @Override
    public boolean isLogged() {
        return Optional.ofNullable(cognitoUserSession).map(CognitoUserSession::isValid).orElse(false);
    }

    @Override
    public CompletableFuture<User> getCurrentUser() {
        return lock.get(this::getCurrentUserInt);
    }

    @Override
    public CompletableFuture<User> login() {
        return login(null, null);
    }

    @Override
    public CompletableFuture<User> login(String email, String password) {
        return lock.get(() -> {
            CompletableFuture<User> result = new CompletableFuture<>();
            if (loginFuture == null) {
                this.currentUserId = null;
                this.currentUsePassword = null;

                cognitoUserPool.getCurrentUser().getSessionInBackground(this);
                loginFuture = result;
            } else {
                result.completeExceptionally(new IllegalStateException("Already being logging in"));
            }
            return result;
        });
    }

    @Override
    public CompletableFuture<User> firstLogin(String newPassword, String name) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> logout() {
        return lock.get(() -> {
            CompletableFuture<Boolean> result = new CompletableFuture<>();
            if (isLogged()) {
                cognitoUserPool.getCurrentUser().signOut();
                clean();
                result.complete(true);
            } else {
                result.complete(false);
            }
            return result;
        });
    }

    @Override
    public CompletableFuture<ForgotPasswordStart> forgotPassword(String userId) {
        return null;
    }

    @Override
    public CompletableFuture<ForgotPasswordSuccess> changePasswordForgotten(String newPassword, String verCode) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> changePassword(String oldPassword, String newPassword) {
        return null;
    }


    @Override
    public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
        lock.run(() -> {
            this.cognitoUserSession = userSession;
            this.cognitoDevice = newDevice;

            if (loginFuture != null) {
                getCurrentUserInt().handle((u, t) -> {
                    cleanContinuation();
                    if (t != null) {
                        loginFuture.completeExceptionally(t);
                    } else {
                        loginFuture.complete(u);
                    }
                    return null;
                });
            } else {
                Log.e(TAG, "onSuccess without login future");
            }
        });
    }

    @Override
    public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
        lock.run(() -> {
            if (loginFuture != null) {
                AuthenticationDetails authenticationDetails = new AuthenticationDetails(currentUserId, currentUsePassword, null);
                authenticationContinuation.setAuthenticationDetails(authenticationDetails);
                authenticationContinuation.continueTask();
            } else {
                Log.e(TAG, "getAuthenticationDetails without login future");
            }
        });
    }

    @Override
    public void getMFACode(MultiFactorAuthenticationContinuation continuation) {
        lock.run(() -> {
            if (loginFuture != null) {
                multiFactorAuthenticationContinuation = continuation;
                loginFuture.completeExceptionally(new MFAAuthenticationRequired("MFA required"));
            } else {
                Log.e(TAG, "MFA Continuation without login future");
            }
        });
    }

    @Override
    public void authenticationChallenge(ChallengeContinuation continuation) {
        lock.run(() -> {
            if (loginFuture != null) {
                if (continuation instanceof NewPasswordContinuation) {
                    newPasswordContinuation = (NewPasswordContinuation) continuation;
                    loginFuture.completeExceptionally(new NewPasswordRequired("New Password required"));
                } else if (continuation instanceof ChooseMfaContinuation) {
                    mfaOptionsContinuation = (ChooseMfaContinuation) continuation;
                    loginFuture.completeExceptionally(new ChooseMfaContinuationRequired("Choose MFA required"));
                } else {
                    Log.e(TAG, "Authentication Challenge not handle " + continuation);
                }
            } else {
                Log.e(TAG, "Authentication Challenge without login future");
            }
        });
    }

    @Override
    public void onFailure(Exception exception) {
        lock.run(() -> {
            if (loginFuture != null) {
                loginFuture.completeExceptionally(exception);
            } else {
                Log.e(TAG, "Exception without login future", exception);
            }
        });
    }

    private void clean() {

    }

    private void cleanContinuation() {
        loginFuture = null;
        newPasswordContinuation = null;
        mfaOptionsContinuation = null;
        multiFactorAuthenticationContinuation = null;
    }

    private User toUser(CognitoUserDetails cognitoUserDetails) {
        return null;
    }

    @NonNull
    private CompletableFuture<User> getCurrentUserInt() {
        CompletableFuture<User> result = new CompletableFuture<>();
        if (isLogged()) {
            cognitoUserPool.getCurrentUser().getDetailsInBackground(new GetDetailsHandler() {
                @Override
                public void onSuccess(CognitoUserDetails cognitoUserDetails) {
                    result.complete(toUser(cognitoUserDetails));
                }

                @Override
                public void onFailure(Exception exception) {
                    result.completeExceptionally(exception);
                }
            });
        } else {
            result.completeExceptionally(new IllegalStateException("Not logged"));
        }
        return result;
    }


    //    private Context context;
//
//
//    private CognitoUserPool cognitoUserPool;
//    private CognitoDevice cognitoDevice;
//
//    private CognitoUserSession cognitoUserSession;
//    private CognitoUserDetails cognitoUserDetails;
//
//    private MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation;
//    private ForgotPasswordContinuation forgotPasswordContinuation;
//    private NewPasswordContinuation newPasswordContinuation;
//    private ChooseMfaContinuationRequired mfaOptionsContinuation;
//
//    private Optional<User> currentUser;
//
//    public boolean isLogged() {
//        return currentUser.isPresent();
//    }
//
//    public Optional<User> getCurrentUser() {
//        return currentUser;
//    }
//
//
//    public void start(Context context) {
//        String userPoolId = context.getResources().getString(R.string.userPoolId);
//        String clientId = context.getResources().getString(R.string.clientId);
//        String clientSecret = context.getResources().getString(R.string.clientSecret);
//        Regions cognitoRegion = Regions.fromName(context.getResources().getString(R.string.region));
//
//        // Create a user pool with default ClientConfiguration
//        cognitoUserPool = new CognitoUserPool(context, userPoolId, clientId, clientSecret, cognitoRegion);
//    }
//
//    public void stop() {
//        cognitoUserPool = null;
//    }
//
////    public CognitoUser getCurrentUser() {
//////        return cognitoUserPool.getCurrentUser();
//////    }
//
//    public CognitoUserDetails getUserDetails() {
//        return cognitoUserDetails;
//    }
//
//
//    public void forgotPassword(final String userId) {
//        cognitoUserPool.getUser(userId).forgotPasswordInBackground(new ForgotPasswordHandler() {
//            @Override
//            public void onSuccess() {
//                forgotPasswordContinuation = null;
//                sendEvent(new ForgotPasswordSuccess(userId));
//            }
//
//            @Override
//            public void getResetCode(ForgotPasswordContinuation continuation) {
//                forgotPasswordContinuation = continuation;
//                sendEvent(new ForgotPasswordStart(userId, continuation));
//            }
//
//            @Override
//            public void onFailure(Exception exception) {
//                sendEvent(new ForgotPasswordFail(userId, exception));
//            }
//        });
//    }
//
//
//    public void changePasswordForgotten(String newPassword, String verCode) {
//        if (forgotPasswordContinuation != null) {
//            forgotPasswordContinuation.setPassword(newPassword);
//            forgotPasswordContinuation.setVerificationCode(verCode);
//            forgotPasswordContinuation.continueTask();
//        }
//    }
//
//    public CompletableFuture<User> login() {
//        CompletableFuture<User> completableFuture = new CompletableFuture<>();
//        AuthenticationHandler handler = getAuthenticationHandler(null, null, completableFuture);
//        cognitoUserPool.getCurrentUser().getSessionInBackground(handler);
//        return completableFuture;
//    }
//
//    public void login(final String email, final String password) {
//        CompletableFuture<User> completableFuture = new CompletableFuture<>();
//        completableFuture.completeAsync(() -> new User("", "", new HashSet<>()));
//        AuthenticationHandler handler = getAuthenticationHandler(email, password);
//        cognitoUserPool.getUser(email).getSessionInBackground(handler);
//    }
//
//    public void changePassword(String oldPassword, String newPassword) {
//        cognitoUserPool.getCurrentUser().changePasswordInBackground(oldPassword, newPassword, new GenericHandler() {
//            @Override
//            public void onSuccess() {
//                sendEvent(new ChangePasswordSuccess());
//            }
//
//            @Override
//            public void onFailure(Exception exception) {
//                sendEvent(new ChangePasswordFail(exception));
//            }
//        });
//    }
//
//    private void sendEvent(Event changePasswordSuccess) {
//
//    }
//
//    public void logout() {
//        if (getCurrentUser() != null) {
//            getCurrentUser().signOut();
//            sendEvent(new Logout());
//        }
//    }
//
//    public void continueWithFirstTimeSignIn(String newPassword, Map<String, String> newAttributes) {
//        if (newPasswordContinuation != null) {
//            newPasswordContinuation.setPassword(newPassword);
//            if (newAttributes != null) {
//                for (Map.Entry<String, String> attr : newAttributes.entrySet()) {
//                    Log.d(TAG, String.format(" -- Adding attribute: %s, %s", attr.getKey(), attr.getValue()));
//                    newPasswordContinuation.setUserAttribute(attr.getKey(), attr.getValue());
//                }
//            }
//            newPasswordContinuation.continueTask();
//        } else {
//            throw new IllegalStateException("There is no continue with sign in");
//        }
//    }
//
//    @NonNull
//    private AuthenticationHandler getAuthenticationHandler(final String email, final String password, final CompletableFuture<User> completableFuture) {
//        return new AuthenticationHandler() {
//            @Override
//            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
//                SecurityServiceCognito.this.cognitoUserSession = userSession;
//                SecurityServiceCognito.this.cognitoDevice = newDevice;
//                newPasswordContinuation = null;
//                sendEvent(new LogInSuccess(userSession, newDevice));
//                loadUserDetails();
//            }
//
//            @Override
//            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
//                AuthenticationDetails authenticationDetails = new AuthenticationDetails(email, password, null);
//                authenticationContinuation.setAuthenticationDetails(authenticationDetails);
//                authenticationContinuation.continueTask();
//            }
//
//            @Override
//            public void getMFACode(MultiFactorAuthenticationContinuation continuation) {
//                sendEvent(new MultiFactorAuthentication(continuation));
//            }
//
//            @Override
//            public void authenticationChallenge(ChallengeContinuation continuation) {
//                setChallengeContinuation(continuation);
//
//                sendEvent(new AuthenticationChallenge(continuation));
//            }
//
//            @Override
//            public void onFailure(Exception exception) {
//                completableFuture.completeExceptionally(exception);
//            }
//        };
//    }
//
//    public void loadUserDetails() {
//        cognitoUserPool.getCurrentUser().getDetailsInBackground(new GetDetailsHandler() {
//            @Override
//            public void onSuccess(CognitoUserDetails cognitoUserDetails) {
//                SecurityServiceCognito.this.cognitoUserDetails = cognitoUserDetails;
//                sendEvent(new UserDetailsSuccess(cognitoUserDetails));
//            }
//
//            @Override
//            public void onFailure(Exception exception) {
//                sendEvent(new UserDetailsFail(exception));
//            }
//        });
//    }
//
//    public NewPasswordContinuation getNewPasswordContinuation() {
//        return newPasswordContinuation;
//    }
//
//    public ForgotPasswordContinuation getForgotPasswordContinuation() {
//        return forgotPasswordContinuation;
//    }
//
//    private void setChallengeContinuation(ChallengeContinuation continuation) {
//        if (continuation instanceof NewPasswordContinuation) {
//            newPasswordContinuation = (NewPasswordContinuation) continuation;
//        } else if (continuation instanceof ChooseMfaContinuationRequired) {
//            mfaOptionsContinuation = (ChooseMfaContinuationRequired) continuation;
//        }
//    }
}
