package com.at.cancerbero.domain.service;

import android.content.Context;

import com.at.cancerbero.domain.model.domain.User;
import com.at.cancerbero.service.events.ForgotPasswordStart;
import com.at.cancerbero.service.events.ForgotPasswordSuccess;

import java8.util.Optional;
import java8.util.concurrent.CompletableFuture;

public interface SecurityService {

    void start(Context context);

    void stop();

    boolean isLogged();

    CompletableFuture<User> getCurrentUser();

    CompletableFuture<User> login();

    CompletableFuture<User> login(String email, String password);

    CompletableFuture<User> firstLogin(String newPassword, String name);

    CompletableFuture<Boolean> logout();

    CompletableFuture<Void> forgotPassword(String userId);

    CompletableFuture<Void> changePasswordForgotten(String userId, String newPassword, String verCode);

    CompletableFuture<Boolean> changePassword(String oldPassword, String newPassword);


}
