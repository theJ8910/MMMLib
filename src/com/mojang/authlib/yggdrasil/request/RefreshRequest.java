package com.mojang.authlib.yggdrasil.request;

import java.util.UUID;

import com.mojang.authlib.yggdrasil.GameProfile;

@SuppressWarnings("unused")
public class RefreshRequest {
    private UUID clientToken;
    private UUID accessToken;
    private GameProfile selectedProfile;
    private boolean requestUser = true;
    
    public RefreshRequest( UUID clientToken, UUID accessToken ) {
        this( clientToken, accessToken, null );
    }
    
    public RefreshRequest( UUID clientToken, UUID accessToken, GameProfile selectedProfile ) {
        this.clientToken = clientToken;
        this.accessToken = accessToken;
        this.selectedProfile = selectedProfile;
    }
}
