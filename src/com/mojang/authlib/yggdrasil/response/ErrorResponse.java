package com.mojang.authlib.yggdrasil.response;

public class ErrorResponse implements Response {
    private String error;
    private String errorMessage;
    private String cause;
    
    public void setError( String error ) {
        this.error = error;
    }
    
    public String getError() {
        return this.error;
    }
    
    public void setErrorMessage( String errorMessage ) {
        this.errorMessage = errorMessage;
    }
    
    public String getErrorMessage() {
        return this.errorMessage;
    }
    
    public void setCause( String cause ) {
        this.cause = cause;
    }
    
    public String getCause() {
        return this.cause;
    }
}
