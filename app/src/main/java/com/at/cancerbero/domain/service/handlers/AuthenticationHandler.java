package com.at.cancerbero.domain.service.handlers;

import android.util.Log;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChooseMfaContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.NewPasswordContinuation;
import com.at.cancerbero.domain.service.exceptions.AuthenticationContinuationRequired;

import java8.util.concurrent.CompletableFuture;

public class AuthenticationHandler implements com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler {

    protected final String TAG = getClass().getSimpleName();

    private final CognitoUserPool cognitoUserPool;

    private final String userId;
    private final String userPassword;

    private AuthenticationContinuations authenticationContinuations;

    private MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation;
    private NewPasswordContinuation newPasswordContinuation;
    private ChooseMfaContinuation mfaOptionsContinuation;
    private boolean continuing = false;

    private CompletableFuture<CognitoUserSession> loginStartFuture = new CompletableFuture<>();

    private CompletableFuture<CognitoUserSession> loginContinueFuture = new CompletableFuture<>();

    public AuthenticationHandler(CognitoUserPool cognitoUserPool, String userId, String userPassword) {
        this.cognitoUserPool = cognitoUserPool;
        this.userId = userId;
        this.userPassword = userPassword;
    }

    @Override
    public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
        if (!loginStartFuture.isDone()) {
            loginStartFuture.complete(userSession);
        } else if (!loginContinueFuture.isDone()) {
            loginContinueFuture.complete(userSession);
            continuing = false;
        } else {
            Log.e(TAG, "loginStartFuture and loginContinueFuture are done");
        }
    }

    @Override
    public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
        if (!loginStartFuture.isDone()) {
            AuthenticationDetails authenticationDetails = new AuthenticationDetails(userId, userPassword, null);
            authenticationContinuation.setAuthenticationDetails(authenticationDetails);
            authenticationContinuation.continueTask();
        } else {
            Log.e(TAG, "loginStartFuture is done");
        }
    }

    @Override
    public void getMFACode(MultiFactorAuthenticationContinuation continuation) {
        if (!loginStartFuture.isDone()) {
            multiFactorAuthenticationContinuation = continuation;
            authenticationContinuations = AuthenticationContinuations.MFA;
            loginStartFuture.completeExceptionally(new AuthenticationContinuationRequired(authenticationContinuations));
        } else {
            Log.e(TAG, "loginStartFuture is done");
        }
    }

    @Override
    public void authenticationChallenge(ChallengeContinuation continuation) {
        if (!loginStartFuture.isDone()) {
            if (continuation instanceof NewPasswordContinuation) {
                newPasswordContinuation = (NewPasswordContinuation) continuation;
                authenticationContinuations = AuthenticationContinuations.NewPassword;
                loginStartFuture.completeExceptionally(new AuthenticationContinuationRequired(authenticationContinuations));
            } else if (continuation instanceof ChooseMfaContinuation) {
                mfaOptionsContinuation = (ChooseMfaContinuation) continuation;
                authenticationContinuations = AuthenticationContinuations.ChooseMFA;
                loginStartFuture.completeExceptionally(new AuthenticationContinuationRequired(authenticationContinuations));
            } else {
                loginStartFuture.completeExceptionally(new IllegalStateException("Continuation not supported: " + continuation.getChallengeName()));
            }
        } else {
            Log.e(TAG, "loginStartFuture is done");
        }
    }

    @Override
    public void onFailure(Exception exception) {
        if (!loginStartFuture.isDone()) {
            loginStartFuture.completeExceptionally(exception);
            loginStartFuture = new CompletableFuture<>();
        } else if (!loginContinueFuture.isDone()) {
            loginContinueFuture.completeExceptionally(exception);
            loginContinueFuture = new CompletableFuture<>();
            continuing = false;
        } else {
            Log.e(TAG, "Unexpected status", exception);
        }
    }

    public CompletableFuture<CognitoUserSession> login() {
        if (userId == null) {
            cognitoUserPool.getCurrentUser().getSessionInBackground(this);
        } else {
            cognitoUserPool.getUser(userId).getSessionInBackground(this);
        }
        return loginStartFuture;
    }

    public CompletableFuture<CognitoUserSession> firstLogin(String newPassword, String name) {
        CompletableFuture<CognitoUserSession> result = new CompletableFuture<>();
        if (continuing) {
            result.completeExceptionally(new IllegalStateException("already continuing"));
        } else {
            if (!loginStartFuture.isDone()) {
                result.completeExceptionally(new IllegalStateException("loginStartFuture is not done"));
            } else if (loginContinueFuture.isDone()) {
                result.completeExceptionally(new IllegalStateException("loginContinueFuture is done"));
            } else if (newPasswordContinuation == null) {
                result.completeExceptionally(new IllegalStateException("newPasswordContinuation is null"));
            } else {
                newPasswordContinuation.setPassword(newPassword);
                newPasswordContinuation.setUserAttribute("given_name", name);
                newPasswordContinuation.continueTask();
                result = loginContinueFuture;
                continuing = true;
            }
        }
        return result;
    }

    public AuthenticationContinuations continuationRequired() {
        return authenticationContinuations;
    }

    public CompletableFuture<CognitoUserSession> attach() {
        if (!loginStartFuture.isDone()) {
            return loginStartFuture;
        } else if (continuing) {
            return loginContinueFuture;
        } else {
            return null;
        }
    }
}