package com.at.cancerbero.domain.service;

import android.content.Context;
import android.support.annotation.NonNull;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.regions.Regions;
import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.domain.model.User;
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

    private CognitoUserPool cognitoUserPool;

    private CognitoUserSession cognitoUserSession;

    private final Map<String, AuthenticationHandler> authenticationHandlers = new HashMap<>();
    private final Map<String, ForgotPasswordHandler> forgotPasswordHandlers = new HashMap<>();

    private ReentrantLock authenticationLock = new ReentrantLock();

    private ReentrantLock forgotPasswordLock = new ReentrantLock();

    private ReentrantLock changePasswordLock = new ReentrantLock();

    private ReentrantLock getCurrentUserDetailsLock = new ReentrantLock();

    @Override
    public void start(Context context) {
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
            CompletableFuture<CognitoUserSession> result;

            if (authenticationHandlers.containsKey(userId)) {
                AuthenticationHandler handler = authenticationHandlers.get(userId);

                result = handler.attach();
                if (result == null) {
                    result = new CompletableFuture<>();
                    if (handler.continuationRequired() != null) {
                        result.completeExceptionally(new AuthenticationContinuationRequired(handler.continuationRequired()));
                    } else {
                        result.completeExceptionally(new IllegalStateException("Already login with: " + userId));
                    }
                }
            } else {
                AuthenticationHandler handler = new AuthenticationHandler(cognitoUserPool, userId, password);
                authenticationHandlers.put(userId, handler);

                result = handler.login();
            }

            return result.thenCompose(session -> {
                this.cognitoUserSession = session;
                return getCurrentUser();
            }).handle((u, t) -> {
                if (t != null) {
                    if (!(t.getCause() instanceof AuthenticationContinuationRequired)) {
                        authenticationHandlers.remove(userId);
                    }
                    throwRuntimeException(t);
                    return null;
                } else {
                    authenticationHandlers.remove(userId);
                    return u;
                }
            });
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
                    if (!(t.getCause() instanceof AuthenticationContinuationRequired)) {
                        authenticationHandlers.remove(userId);
                    }
                    throwRuntimeException(t);
                    return null;
                } else {
                    authenticationHandlers.remove(userId);
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
    public CompletableFuture<Void> changePassword(String oldPassword, String newPassword) {
        return changePasswordLock.get(() -> {
            CompletableFuture<Void> result = new CompletableFuture<>();
            cognitoUserPool.getCurrentUser().changePasswordInBackground(oldPassword, newPassword, new GenericHandler() {
                @Override
                public void onSuccess() {
                    result.complete(null);
                }

                @Override
                public void onFailure(Exception exception) {
                    result.completeExceptionally(exception);
                }
            });
            return result;
        });
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
}
