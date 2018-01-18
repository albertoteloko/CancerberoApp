package com.at.cancerbero.domain.service;

import android.content.Context;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.domain.data.repository.BackEndClient;
import com.at.cancerbero.domain.data.repository.InstallationRepository;
import com.at.cancerbero.domain.data.repository.NodesRepository;
import com.at.cancerbero.domain.model.Installation;
import com.at.cancerbero.domain.model.Node;
import com.at.cancerbero.domain.model.User;
import com.at.cancerbero.domain.service.converters.InstallationConverter;
import com.at.cancerbero.domain.service.converters.NodeConverter;

import java.util.Set;
import java.util.UUID;

import java8.util.concurrent.CompletableFuture;

public class InstallationServiceRemote implements InstallationService {

    private static final boolean IGNORE_HOST_VERIFICATION = false;

    protected final String TAG = getClass().getSimpleName();

    private final NodeConverter nodeConverter = new NodeConverter();
    private final InstallationConverter installationConverter = new InstallationConverter(nodeConverter);

    private final SecurityService securityService;

    private String baseUrl;


    public InstallationServiceRemote(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Override
    public void start(Context context) {
        baseUrl = context.getResources().getString(R.string.backEndUrl);
    }

    @Override
    public void stop() {
        baseUrl = null;
    }

    @Override
    public CompletableFuture<Set<Installation>> loadInstallations() {
        return securityService.getCurrentUser()
                .thenApplyAsync(user -> installationConverter.convert(getInstallationRepository(user).loadInstallations(), getNodesRepository(user)));
    }

    @Override
    public CompletableFuture<Installation> loadInstallation(UUID installationId) {
        return null;
    }

    @Override
    public CompletableFuture<Node> loadNode(String nodeId) {
        return null;
    }

    private InstallationRepository getInstallationRepository(User user) {
        return new InstallationRepository(getServerClient(user));
    }

    private NodesRepository getNodesRepository(User user) {
        return new NodesRepository(getServerClient(user));
    }

    private BackEndClient getServerClient(User user) {
        BackEndClient serverClient = new BackEndClient(baseUrl, IGNORE_HOST_VERIFICATION);
        serverClient.setToken(user.getToken());
        return serverClient;
    }
//
//    @Override
//    public boolean isLogged() {
//        return Optional.ofNullable(cognitoUserSession).map(CognitoUserSession::isValid).orElse(false);
//    }
//
//    @Override
//    public CompletableFuture<User> getCurrentUser() {
//        return getCurrentUserDetailsLock.get(this::getCurrentUserInt);
//    }
//
//    @Override
//    public CompletableFuture<User> login() {
//        return login(null, null);
//    }
//
//    @Override
//    public CompletableFuture<User> login(String userId, String password) {
//        return authenticationLock.get(() -> {
//            CompletableFuture<User> result;
//
//            if (authenticationHandlers.containsKey(userId)) {
//                AuthenticationHandler handler = authenticationHandlers.get(userId);
//
//                result = new CompletableFuture<>();
//                if (handler.continuationRequired() != null) {
//                    result.completeExceptionally(new AuthenticationContinuationRequired(handler.continuationRequired()));
//                } else {
//                    result.completeExceptionally(new IllegalStateException("Already login with: " + userId));
//
//                }
//            } else {
//                AuthenticationHandler handler = new AuthenticationHandler(cognitoUserPool, userId, password);
//                authenticationHandlers.put(userId, handler);
//
//                result = handler.login().thenCompose(session -> {
//                    this.cognitoUserSession = session;
//                    return getCurrentUser();
//                });
//            }
//
//            return result.handle((u, t) -> {
//                if (t != null) {
//                    if (!(t.getCause() instanceof AuthenticationContinuationRequired)) {
//                        authenticationHandlers.remove(userId);
//                    }
//                    throwRuntimeException(t);
//                    return null;
//                } else {
//                    authenticationHandlers.remove(userId);
//                    return u;
//                }
//            });
//        });
//    }
//
//    @Override
//    public CompletableFuture<User> firstLogin(String userId, String newPassword, String name) {
//        return authenticationLock.get(() -> {
//            CompletableFuture<User> result = new CompletableFuture<>();
//
//            if (!authenticationHandlers.containsKey(userId)) {
//                result.completeExceptionally(new IllegalStateException("User not login with: " + userId));
//            } else {
//                AuthenticationHandler handler = authenticationHandlers.get(userId);
//
//                AuthenticationContinuations authenticationContinuations = handler.continuationRequired();
//                if (authenticationContinuations != null) {
//                    if (authenticationContinuations == AuthenticationContinuations.NewPassword) {
//                        result = handler.firstLogin(newPassword, name).thenCompose(session -> {
//                            this.cognitoUserSession = session;
//                            return getCurrentUser();
//                        });
//                    } else {
//                        result.completeExceptionally(new AuthenticationContinuationRequired(authenticationContinuations));
//                    }
//                } else {
//                    result.completeExceptionally(new IllegalStateException("Login does not require continuation"));
//                }
//            }
//
//
//            return result.handle((u, t) -> {
//                if (t != null) {
//                    if (!(t.getCause() instanceof AuthenticationContinuationRequired)) {
//                        authenticationHandlers.remove(userId);
//                    }
//                    throwRuntimeException(t);
//                    return null;
//                } else {
//                    authenticationHandlers.remove(userId);
//                    return u;
//                }
//            });
//        });
//    }
//
//    @Override
//    public CompletableFuture<Boolean> logout() {
//        return authenticationLock.get(() -> {
//            CompletableFuture<Boolean> result = new CompletableFuture<>();
//            if (isLogged()) {
//                cognitoUserPool.getCurrentUser().signOut();
//                result.complete(true);
//            } else {
//                result.complete(false);
//            }
//            return result;
//        });
//    }
//
//    @Override
//    public CompletableFuture<Void> forgotPassword(String userId) {
//        return forgotPasswordLock.get(() -> {
//            CompletableFuture<Void> result;
//
//            if (forgotPasswordHandlers.containsKey(userId)) {
//                result = new CompletableFuture<>();
//                result.completeExceptionally(new IllegalStateException("Already restoring forgot password to: " + userId));
//            } else {
//                ForgotPasswordHandler handler = new ForgotPasswordHandler(cognitoUserPool, userId);
//                forgotPasswordHandlers.put(userId, handler);
//                result = handler.forgotPassword().handle((v, t) -> {
//                    if (t != null) {
//                        forgotPasswordHandlers.remove(userId);
//                        throwRuntimeException(t);
//                    }
//                    return null;
//                });
//            }
//            return result;
//        });
//    }
//
//
//    @Override
//    public CompletableFuture<Void> changePasswordForgotten(String userId, String newPassword, String verCode) {
//        return forgotPasswordLock.get(() -> {
//            CompletableFuture<Void> result;
//
//            if (!forgotPasswordHandlers.containsKey(userId)) {
//                result = new CompletableFuture<>();
//                result.completeExceptionally(new IllegalStateException("Not restoring forgot password to: " + userId));
//            } else {
//                ForgotPasswordHandler handler = forgotPasswordHandlers.get(userId);
//                result = handler.changePassword(newPassword, verCode).handle((v, t) -> {
//                    if (t != null) {
//                        throwRuntimeException(t);
//                    } else {
//                        forgotPasswordHandlers.remove(userId);
//                    }
//                    return null;
//                });
//            }
//            return result;
//        });
//    }
//
//
//    private void throwRuntimeException(Throwable t) throws RuntimeException {
//        if (t instanceof RuntimeException) {
//            throw (RuntimeException) t;
//        } else {
//            throw new RuntimeException(t);
//        }
//    }
//
//    @Override
//    public CompletableFuture<Void> changePassword(String oldPassword, String newPassword) {
//        return changePasswordLock.get(() -> {
//            CompletableFuture<Void> result = new CompletableFuture<>();
//            cognitoUserPool.getCurrentUser().changePasswordInBackground(oldPassword, newPassword, new GenericHandler() {
//                @Override
//                public void onSuccess() {
//                    result.complete(null);
//                }
//
//                @Override
//                public void onFailure(Exception exception) {
//                    result.completeExceptionally(exception);
//                }
//            });
//            return result;
//        });
//    }
//
//
//    private User toUser(CognitoUserDetails cognitoUserDetails) {
//        String userId = cognitoUserSession.getUsername();
//        String name = cognitoUserDetails.getAttributes().getAttributes().get("given_name");
//        String token = cognitoUserSession.getIdToken().getJWTToken();
//        Set<String> groups = new HashSet<>();
//        return new User(userId, name, token, groups);
//    }
//
//    @NonNull
//    private CompletableFuture<User> getCurrentUserInt() {
//        CompletableFuture<User> result = new CompletableFuture<>();
//        if (isLogged()) {
//            cognitoUserPool.getCurrentUser().getDetailsInBackground(new GetDetailsHandler() {
//                @Override
//                public void onSuccess(CognitoUserDetails cognitoUserDetails) {
//                    result.complete(toUser(cognitoUserDetails));
//                }
//
//                @Override
//                public void onFailure(Exception exception) {
//                    result.completeExceptionally(exception);
//                }
//            });
//        } else {
//            result.completeExceptionally(new IllegalStateException("Not logged"));
//        }
//        return result;
//    }
}
