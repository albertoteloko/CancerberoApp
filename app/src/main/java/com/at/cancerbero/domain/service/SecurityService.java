package com.at.cancerbero.domain.service;

import android.content.Context;

import com.at.cancerbero.app.MainAppService;
import com.at.cancerbero.domain.model.User;

import java8.util.concurrent.CompletableFuture;

public interface SecurityService {

    void start(MainAppService mainAppService);

    void stop();

    boolean isLogged();

    CompletableFuture<User> getCurrentUser();

    CompletableFuture<User> login();

    CompletableFuture<User> login(String email, String password);

    CompletableFuture<User> firstLogin(String userId, String newPassword, String name);

    CompletableFuture<Boolean> logout();

    CompletableFuture<Void> forgotPassword(String userId);

    CompletableFuture<Void> changePasswordForgotten(String userId, String newPassword, String verCode);

    CompletableFuture<Void> changePassword(String oldPassword, String newPassword);


}
