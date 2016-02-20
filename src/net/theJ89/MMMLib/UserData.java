package net.theJ89.MMMLib;

import java.util.UUID;

import com.mojang.authlib.UserType;

public class UserData {
    private UUID     clientToken;
    private UUID     accessToken;
    private UUID     uuid;
    private String   username;
    private int      userid;
    private UserType usertype;
    
    public UserData() {
        this.clientToken = UUID.randomUUID();
        this.accessToken = null;
        this.uuid        = null;
        this.username    = null;
        this.userid      = 0;
        this.usertype    = UserType.LEGACY;
    }
    public UserData( UUID clientToken, UUID accessToken, UUID uuid, String username, int userid, UserType usertype ) {
        this.clientToken = clientToken;
        this.accessToken = accessToken;
        this.uuid        = uuid;
        this.username    = username;
        this.userid      = userid;
        this.usertype    = usertype; 
    }
    public void setUsername( String username ) {
        this.username = username;
    }
    public String getUsername() {
        return this.username;
    }
    public void setUserID( int userid ) {
        this.userid = userid;
    }
    public int getUserID() {
        return this.userid;
    }
    public void setUUID( UUID uuid ) {
        this.uuid = uuid;
    }
    public UUID getUUID() {
        return this.uuid;
    }
    public void setClientToken( UUID clientToken ) {
        this.clientToken = clientToken;
    }
    public UUID getClientToken() {
        return this.clientToken;
    }
    public void setAccessToken( UUID accessToken ) {
        this.accessToken = accessToken;
    }
    public UUID getAccessToken() {
        return this.accessToken;
    }
    public void setUserType( UserType usertype ) {
        this.usertype = usertype;
    }
    public UserType getUserType() {
        return this.usertype;
    }
}
