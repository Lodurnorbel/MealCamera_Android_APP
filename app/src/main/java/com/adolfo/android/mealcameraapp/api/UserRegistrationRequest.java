package com.adolfo.android.mealcameraapp.api;

public class UserRegistrationRequest {
    final String username;
    final String password;

    public UserRegistrationRequest(String username, String password){
        this.username = username;
        this.password = password;
    }
}
