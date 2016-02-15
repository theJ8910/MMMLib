package com.mojang.authlib.yggdrasil.request;

@SuppressWarnings("unused")
public class SignoutRequest {
    private String username;
    private String password;
    
    public SignoutRequest( String username, String password ) {
        this.username = username;
        this.password = password;
    }
}
