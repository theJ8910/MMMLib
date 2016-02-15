package com.mojang.util;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.authlib.yggdrasil.response.AuthenticationResponse;
import com.mojang.authlib.yggdrasil.response.ErrorResponse;
import com.mojang.authlib.yggdrasil.response.Response;

public class ResponseDeserializer implements JsonDeserializer<Response> {
    @Override
    public Response deserialize( JsonElement json, Type typeOfT, JsonDeserializationContext context ) throws JsonParseException {
        //Cast element JSON object
        JsonObject json_obj = json.getAsJsonObject();
        
        //If the object contains "error", this is an ErrorResponse
        if( json_obj.has( "error" ) )
            return context.deserialize( json, ErrorResponse.class );
        
        //If the object contains "accessToken", this is an AuthenticationResponse
        if( json_obj.has( "accessToken" ) )
            return context.deserialize( json, AuthenticationResponse.class );
        
        //We were given an object but couldn't determine its type.
        throw new JsonParseException( "Couldn't determine response type." );
    }
}
