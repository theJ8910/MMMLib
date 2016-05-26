package com.mojang.launcher.updater;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class LowerCaseEnumTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create( Gson gson, TypeToken<T> type ) {
        
        //Even though TypeToken<T> is parameterized with T, type.getRawType() returns a Class<? super T>.
        //This means we have to do an unsafe downcast here from <? super T> to <T>...
        //I don't understand why; my best guess this must be due to some weakness of gson's TypeToken<T> implementation.
        //This seems to be how Mojang handles this situation, so I'll trust in their implementation.
        @SuppressWarnings("unchecked")
        Class<T> rawType = (Class<T>)type.getRawType();
        
        //This factory can only produce a type adapter for enum types.
        T[] constants = rawType.getEnumConstants();
        if( constants == null )
            return null;
        
        //Create a lookup table (lowercase_enum_constant_name -> enum constant)
        //Note: We've already determined this is an Enum in the step above.
        //So long as the implementer doesn't override the enum's toString() function,
        //this will always return the name of the enum constant as written in it's source file.
        final Map< String, T > LOOKUP = new HashMap< String, T >();
        for( T constant : constants )
            LOOKUP.put( constant.toString().toLowerCase( Locale.US ), constant );
        
        //Produce a type adapter that can handle translating this particular enum
        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                //An enum value can either be an enum constant or null
                if( value != null ) { out.value( value.toString().toLowerCase( Locale.US ) ); }
                else                { out.nullValue();                                        }
            }
            @Override
            public T read(JsonReader in) throws IOException {
                //null is a valid enum value. nextString() throws an IllegalStateException
                //if it reads a null, so we have to handle this situation explicitly here.
                if( in.peek() == JsonToken.NULL ) {
                    in.nextNull();
                    return null;
                }
                return LOOKUP.get( in.nextString().toLowerCase( Locale.US ) );
            }
        };
    }
}
