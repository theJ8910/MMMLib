package com.mojang.util;

import java.io.IOException;
import java.util.UUID;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class UUIDTypeAdapter extends TypeAdapter< UUID > {

    @Override
    public void write(JsonWriter out, UUID value) throws IOException {
        out.value( fromUUID( value ) );
    }

    @Override
    public UUID read(JsonReader in) throws IOException {
        return UUID.fromString( in.nextString().replaceFirst( "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5") );
    }
    
    /**
     * Converts a UUID to a string format and strips the dashes from it.
     * @param uuid - The UUID to convert to a string.
     * @return
     */
    public static String fromUUID( UUID uuid ) {
        return uuid.toString().replace( "-", "" );
    }

}
