package net.theJ89.json;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * GSON type adapter that converts ISO 8601 formatted date/time strings to Java OffsetDateTime objects.
 */
public class ISO8601_OffsetDateTime_TypeAdapter extends TypeAdapter<OffsetDateTime> {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern( "yyyy-MM-dd'T'HH:mm:ssZ" );
    
    @Override
    public void write(JsonWriter out, OffsetDateTime value) throws IOException {
        out.value( value.format( FMT ) );
    }

    @Override
    public OffsetDateTime read(JsonReader in) throws IOException {
        return OffsetDateTime.parse( in.nextString(), FMT );
    }
}
