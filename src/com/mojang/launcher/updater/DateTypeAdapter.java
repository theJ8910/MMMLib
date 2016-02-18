package com.mojang.launcher.updater;

import java.io.IOException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

//Another difference from Mojang's version: Using TypeAdapter<Date> instead of implementing Serializer / Deserializer.
public class DateTypeAdapter extends TypeAdapter<Date> {
    private final DateFormat ISO_8601;
    
    public DateTypeAdapter() {
        //Note: Mojang uses ISO 8601 formatted dates in their .json files.
        //The launcher also seems to support default US format date strings (e.g. "Jun 30, 2009 7:03:47 AM"),
        //but I can't see these used anywhere in the .json files, so unless I'm mistaken, I'm dropping support for these.
        //Mojang uses "Z" in their format strings, which is intended for RFC 822 date strings.
        //Apparantly this works, but only after doing some ugly hacks to clean up the date string.
        //I use "X" here instead. This was introduced in Java 7 and is parses ISO 8601 date strings correctly.
        //Most likely Mojang uses "Z" for compatibility reasons.
        ISO_8601  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
    }

    @Override
    public void write(JsonWriter out, Date value) throws IOException {
        //Note: DateFormat is not threadsafe
        synchronized( ISO_8601 ) {
            out.value( ISO_8601.format( value ) );
        }
    }

    @Override
    public Date read(JsonReader in) throws IOException {
        //Note: DateFormat is not threadsafe
        synchronized( ISO_8601 ) {
            //Handle null dates
            if( in.peek() == JsonToken.NULL ) {
                in.nextNull();
                return null;
            }
            
            //Try to parse the date string using the ISO_8601 format, or throw a JsonParseException.
            try                       { return ISO_8601.parse( in.nextString() );             }
            catch( ParseException e ) { throw new JsonParseException( "Cannot parse date." ); }
        }
    }
}
