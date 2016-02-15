package com.mojang.authlib.yggdrasil.request;

import java.util.UUID;

@SuppressWarnings("unused")
public class InvalidateRequest {
    private UUID clientToken;
    private UUID accessToken;
    
    public InvalidateRequest( UUID clientToken, UUID accessToken ) {
        this.clientToken = clientToken;
        this.accessToken = accessToken;
    }
}
