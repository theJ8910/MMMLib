package com.mojang.authlib.yggdrasil.response;

import java.util.UUID;

import com.mojang.authlib.yggdrasil.GameProfile;
import com.mojang.authlib.yggdrasil.User;

public class AuthenticationResponse implements Response {
    private UUID accessToken;
    private UUID clientToken;
    private GameProfile selectedProfile;
    private GameProfile[] availableProfiles;
    private User user;
    
    public UUID getAccessToken() {
        return this.accessToken;
    }
    
    public UUID getClientToken() {
        return this.clientToken;
    }
    
    public GameProfile getSelectedProfile() {
        return this.selectedProfile;
    }
    
    public GameProfile[] getAvailableProfiles() {
        return this.availableProfiles;
    }
    
    public User getUser() {
        return this.user;
    }
}