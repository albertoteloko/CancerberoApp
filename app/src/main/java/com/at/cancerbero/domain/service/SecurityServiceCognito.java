package com.at.cancerbero.domain.service;

import android.content.Context;
import android.support.annotation.NonNull;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.regions.Regions;
import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.domain.model.domain.User;
import com.at.cancerbero.domain.service.exceptions.AuthenticationContinuationRequired;
import com.at.cancerbero.domain.service.handlers.AuthenticationContinuations;
import com.at.cancerbero.domain.service.handlers.AuthenticationHandler;
import com.at.cancerbero.domain.service.handlers.ForgotPasswordHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java8.util.Optional;
import java8.util.concurrent.CompletableFuture;

public class SecurityServiceCognito implements SecurityService {

    protected final String TAG = getClass().getSimpleName();

    private Context context;

    private CognitoUserPool cognitoUserPool;

    private CognitoUserSession cognitoUserSession;

    private final Map<String, AuthenticationHandler> authenticationHandlers = new HashMap<>();
    private final Map<String, ForgotPasswordHandler> forgotPasswordHandlers = new HashMap<>();

    private ReentrantLock authenticationLock = new ReentrantLock();

    private ReentrantLock forgotPasswordLock = new ReentrantLock();

    private ReentrantLock getCurrentUserDetailsLock = new ReentrantLock();

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
        authenticationHandlers.clear();
        forgotPasswordHandlers.clear();
    }

    @Override
    public boolean isLogged() {
        return Optional.ofNullable(cognitoUserSession).map(CognitoUserSession::isValid).orElse(false);
    }

    @Override
    public CompletableFuture<User> getCurrentUser() {
        return getCurrentUserDetailsLock.get(this::getCurrentUserInt);
    }

    @Override
    public CompletableFuture<User> login() {
        return login(null, null);
    }

    @Override
    public CompletableFuture<User> login(String userId, String password) {
        return authenticationLock.get(() -> {
            CompletableFuture<User> result;

            if (authenticationHandlers.containsKey(userId)) {
                AuthenticationHandler handler = authenticationHandlers.get(userId);

                result = new CompletableFuture<>();
                if (handler.continuationRequired() != null) {
                    result.completeExceptionally(new AuthenticationContinuationRequired(handler.continuationRequired()));
                } else {
                    result.completeExceptionally(new IllegalStateException("Already login with: " + userId));

                }
            } else {
                AuthenticationHandler handler = new AuthenticationHandler(cognitoUserPool, userId, password);
                authenticationHandlers.put(userId, handler);

                result = handler.login().thenCompose(session -> {
                    this.cognitoUserSession = session;
                    return getCurrentUser();
                }).handle((u, t) -> {
                    if (t != null) {
                        if (!(t instanceof AuthenticationContinuationRequired)) {
                            forgotPasswordHandlers.remove(userId);
                        }
                        throwRuntimeException(t);
                        return null;
                    } else {
                        return u;
                    }
                });
            }
            return result;
        });
    }

    @Override
    public CompletableFuture<User> firstLogin(String userId, String newPassword, String name) {
        return authenticationLock.get(() -> {
            CompletableFuture<User> result = new CompletableFuture<>();

            if (!authenticationHandlers.containsKey(userId)) {
                result.completeExceptionally(new IllegalStateException("User not login with: " + userId));
            } else {
                AuthenticationHandler handler = authenticationHandlers.get(userId);

                AuthenticationContinuations authenticationContinuations = handler.continuationRequired();
                if (authenticationContinuations != null) {
                    if (authenticationContinuations == AuthenticationContinuations.NewPassword) {
                        result = handler.firstLogin(newPassword, name).thenCompose(session -> {
                            this.cognitoUserSession = session;
                            return getCurrentUser();
                        });
                    } else {
                        result.completeExceptionally(new AuthenticationContinuationRequired(authenticationContinuations));
                    }
                } else {
                    result.completeExceptionally(new IllegalStateException("Login does not require continuation"));
                }
            }


            return result.handle((u, t) -> {
                if (t != null) {
                    if (!(t instanceof AuthenticationContinuationRequired)) {
                        forgotPasswordHandlers.remove(userId);
                    }
                    throwRuntimeException(t);
                    return null;
                } else {
                    forgotPasswordHandlers.remove(userId);
                    return u;
                }
            });
        });
    }

    @Override
    public CompletableFuture<Boolean> logout() {
        return authenticationLock.get(() -> {
            CompletableFuture<Boolean> result = new CompletableFuture<>();
            if (isLogged()) {
                cognitoUserPool.getCurrentUser().signOut();
                result.complete(true);
            } else {
                result.complete(false);
            }
            return result;
        });
    }

    @Override
    public CompletableFuture<Void> forgotPassword(String userId) {
        return forgotPasswordLock.get(() -> {
            CompletableFuture<Void> result;

            if (forgotPasswordHandlers.containsKey(userId)) {
                result = new CompletableFuture<>();
                result.completeExceptionally(new IllegalStateException("Already restoring forgot password to: " + userId));
            } else {
                ForgotPasswordHandler handler = new ForgotPasswordHandler(cognitoUserPool, userId);
                forgotPasswordHandlers.put(userId, handler);
                result = handler.forgotPassword().handle((v, t) -> {
                    if (t != null) {
                        forgotPasswordHandlers.remove(userId);
                        throwRuntimeException(t);
                    }
                    return null;
                });
            }
            return result;
        });
    }


    @Override
    public CompletableFuture<Void> changePasswordForgotten(String userId, String newPassword, String verCode) {
        return forgotPasswordLock.get(() -> {
            CompletableFuture<Void> result;

            if (!forgotPasswordHandlers.containsKey(userId)) {
                result = new CompletableFuture<>();
                result.completeExceptionally(new IllegalStateException("Not restoring forgot password to: " + userId));
            } else {
                ForgotPasswordHandler handler = forgotPasswordHandlers.get(userId);
                result = handler.changePassword(newPassword, verCode).handle((v, t) -> {
                    if (t != null) {
                        throwRuntimeException(t);
                    } else {
                        forgotPasswordHandlers.remove(userId);
                    }
                    return null;
                });
            }
            return result;
        });
    }


    private void throwRuntimeException(Throwable t) throws RuntimeException {
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        } else {
            throw new RuntimeException(t);
        }
    }

    @Override
    public CompletableFuture<Boolean> changePassword(String oldPassword, String newPassword) {
        return null;
    }


    private User toUser(CognitoUserDetails cognitoUserDetails) {
        String userId = cognitoUserSession.getUsername();
        String name = cognitoUserDetails.getAttributes().getAttributes().get("given_name");
        String token = cognitoUserSession.getIdToken().getJWTToken();
        Set<String> groups = new HashSet<>();
        return new User(userId, name, token, groups);
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

//    private final AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
//        @Override
//        public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
//            authenticationLock.run(() -> {
//                SecurityServiceCognito.this.cognitoUserSession = userSession;
//
//                if (loginFuture != null) {
//                    getCurrentUserInt().handle((u, t) -> {
//                        if (t != null) {
//                            loginFuture.completeExceptionally(t);
//                        } else {
//                            loginFuture.complete(u);
//                        }
//                        cleanContinuation();
//                        loginFuture = null;
//                        return null;
//                    });
//                } else {
//                    Log.e(TAG, "onSuccess without login future");
//                }
//            });
//        }
//
//        @Override
//        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
//            authenticationLock.run(() -> {
//                if (loginFuture != null) {
//                    AuthenticationDetails authenticationDetails = new AuthenticationDetails(currentUserId, currentUsePassword, null);
//                    authenticationContinuation.setAuthenticationDetails(authenticationDetails);
//                    authenticationContinuation.continueTask();
//                } else {
//                    Log.e(TAG, "getAuthenticationDetails without login future");
//                }
//            });
//        }
//
//        @Override
//        public void getMFACode(MultiFactorAuthenticationContinuation continuation) {
//            authenticationLock.run(() -> {
//                if (loginFuture != null) {
//                    multiFactorAuthenticationContinuation = continuation;
//                    loginFuture.completeExceptionally(new MFAAuthenticationRequired("MFA required"));
//                } else {
//                    Log.e(TAG, "MFA Continuation without login future");
//                }
//            });
//        }
//
//        @Override
//        public void authenticationChallenge(ChallengeContinuation continuation) {
//            authenticationLock.run(() -> {
//                if (loginFuture != null) {
//                    if (continuation instanceof NewPasswordContinuation) {
//                        newPasswordContinuation = (NewPasswordContinuation) continuation;
//                        onFailureInt(new NewPasswordRequired("New Password required"));
//                    } else if (continuation instanceof ChooseMfaContinuation) {
//                        mfaOptionsContinuation = (ChooseMfaContinuation) continuation;
//                        onFailureInt(new ChooseMfaContinuationRequired("Choose MFA required"));
//                    } else {
//                        Log.e(TAG, "Authentication Challenge not handle " + continuation);
//                    }
//                } else {
//                    Log.e(TAG, "Authentication Challenge without login future");
//                }
//            });
//        }
//
//        @Override
//        public void onFailure(Exception exception) {
//            authenticationLock.run(() -> onFailureInt(exception));
//        }
//    };


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
//    public void login(final String userId) {
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
//            public void getAuthenticationDetails(AuthenticationContinuations authenticationContinuations, String userId) {
//                AuthenticationDetails authenticationDetails = new AuthenticationDetails(email, password, null);
//                authenticationContinuations.setAuthenticationDetails(authenticationDetails);
//                authenticationContinuations.continueTask();
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
