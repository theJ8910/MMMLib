package com.mojang.authlib.yggdrasil.request;

import java.util.UUID;

import com.mojang.authlib.Agent;

@SuppressWarnings("unused")
public class AuthenticationRequest {
    private Agent     agent = Agent.MINECRAFT;
    private String    username;
    private String    password;
    private UUID      clientToken;
    private boolean   requestUser = true;
    
    public AuthenticationRequest( String username, String password, UUID clientToken ) {
        this.username    = username;
        this.password    = password;
        this.clientToken = clientToken;
    }
}
