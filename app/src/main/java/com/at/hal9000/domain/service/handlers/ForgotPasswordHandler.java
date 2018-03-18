package com.at.hal9000.domain.service.handlers;

import android.util.Log;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation;

import java8.util.concurrent.CompletableFuture;

public class ForgotPasswordHandler implements com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler {

    protected final String TAG = getClass().getSimpleName();

    private final CognitoUserPool cognitoUserPool;

    private final String userId;

    private CompletableFuture<Void> forgotPasswordStartFuture = new CompletableFuture<>();

    private CompletableFuture<Void> forgotPasswordSuccessFuture = new CompletableFuture<>();

    private ForgotPasswordContinuation forgotPasswordContinuation;

    public ForgotPasswordHandler(CognitoUserPool cognitoUserPool, String userId) {
        this.cognitoUserPool = cognitoUserPool;
        this.userId = userId;
    }

    @Override
    public void onSuccess() {
        if (!forgotPasswordSuccessFuture.isDone()) {
            forgotPasswordSuccessFuture.complete(null);
        } else {
            Log.e(TAG, "forgotPasswordSuccessFuture is done");
        }
    }

    @Override
    public void getResetCode(ForgotPasswordContinuation continuation) {
        if (!forgotPasswordStartFuture.isDone()) {
            forgotPasswordContinuation = continuation;
            forgotPasswordStartFuture.complete(null);
        } else {
            Log.e(TAG, "forgotPasswordSuccessFuture is done");
        }
    }

    @Override
    public void onFailure(Exception exception) {
        if (!forgotPasswordStartFuture.isDone()) {
            forgotPasswordStartFuture.completeExceptionally(exception);
            forgotPasswordStartFuture = new CompletableFuture<>();
        } else if (!forgotPasswordSuccessFuture.isDone()) {
            forgotPasswordSuccessFuture.completeExceptionally(exception);
            forgotPasswordSuccessFuture = new CompletableFuture<>();
        } else {
            Log.e(TAG, "Unexpected status", exception);
        }
    }

    public CompletableFuture<Void> changePassword(String newPassword, String verCode) {
        if (forgotPasswordContinuation != null) {
            forgotPasswordContinuation.setVerificationCode(verCode);
            forgotPasswordContinuation.setPassword(newPassword);
            forgotPasswordContinuation.continueTask();
        } else {
            forgotPasswordSuccessFuture.completeExceptionally(new IllegalStateException("Missing continuation"));
        }
        return forgotPasswordSuccessFuture;
    }

    public CompletableFuture<Void> forgotPassword() {
        cognitoUserPool.getUser(userId).forgotPasswordInBackground(this);
        return forgotPasswordStartFuture;
    }
}