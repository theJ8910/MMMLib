package com.mojang.authlib.yggdrasil.request;

import java.util.UUID;

@SuppressWarnings("unused")
public class ValidateRequest {
    private UUID clientToken;
    private UUID accessToken;
    
    public ValidateRequest( UUID clientToken, UUID accessToken ) {
        this.clientToken = clientToken;
        this.accessToken = accessToken;
    }
}
