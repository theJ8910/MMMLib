package com.mojang.authlib.properties;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class PropertyMap {
    private Map<String, List<Property>> map;
    
    public PropertyMap() {
        this.map = new HashMap< String, List< Property > >();
    }
    
    /**
     * Sets the list at the given key.
     * The list may not be null.
     * @param key
     * @param values
     */
    public void set( String key, List<Property> values ) {
        if( values == null )
            throw new NullPointerException();
        if( values.isEmpty() )
            throw new IllegalArgumentException();
        
        this.map.put( key, values );
    }
    
    /**
     * Returns the list at the given key.
     * @param key
     * @return
     */
    public List<Property> get( String key ) {
        return this.map.get( key );
    }
    
    /**
     * Removes the entire list at key.
     * @param key
     * @return The previous list
     */
    public List< Property > remove( String key ) {
        return this.map.remove( key );
    }
    
    /**
     * Adds value to the list at key.
     * If there is no list at key, creates one.
     * @param key
     * @param value
     * @return Always true
     */
    public boolean put( String key, Property value ) {
        List< Property > list = this.map.get(key);
        if( list == null ) {
            list = new LinkedList< Property >();
            this.map.put( key, list );
        }
        
        return list.add( value );
    }
    
    /**
     * Returns the value at the given index in the list at the given key.
     * @param key
     * @param index
     * @return
     */
    public Property get( String key, int index ) {
        List< Property > list = this.map.get(key);
        if( list == null )
            return null;
        return list.get( index );
    }
    
    /**
     * Removes the value at the given index from the list at the given key.
     * If the list becomes empty after removing the value, removes the list as well.
     * @param key
     * @param index
     * @return The removed value.
     */
    public Property remove( String key, int index ) {
        List< Property > list = this.map.get( key );
        if( list == null )
            return null;
        Property removed = list.remove( index );
        if( list.isEmpty() )
            this.map.remove( key );
        return removed;
    }
    /**
     * Removes the given value from the list at the given key.
     * If there is no list at the given key, returns false.
     * @param key
     * @param value
     * @return true if the value was removed, false otherwise.
     */
    public boolean remove( String key, Property value ) {
        List< Property > list = this.map.get( key );
        if( list == null )
            return false;
        return list.remove( value );
    }
    
    /**
     * Returns a view of all the keys stored in the PropertyMap.
     * @return
     */
    public Set<String> keys() {
        return this.map.keySet();
    }
    
    /**
     * Returns a view of all the values stored in the PropertyMap.
     * @return
     */
    public Iterable<Property> values() {
        final Iterator<List<Property>> li = PropertyMap.this.map.values().iterator();
        final Iterator<Property>       vi = li.hasNext() ? li.next().iterator() : Collections.emptyIterator();
        
        return new Iterable<Property>() {
            @Override
            public Iterator<Property> iterator() {
                return new Iterator<Property>() {
                    private Iterator<List<Property>> listIterator  = li;
                    private Iterator<Property>       valueIterator = vi;
                    
                    @Override
                    public boolean hasNext() {
                        return valueIterator.hasNext() || listIterator.hasNext();
                    }
                    @Override
                    public Property next() {
                        boolean hasNoValues = !valueIterator.hasNext();
                        if( hasNoValues && !listIterator.hasNext() )
                            throw new NoSuchElementException();
                        
                        if( hasNoValues )
                            valueIterator = listIterator.next().iterator();
                        
                        return valueIterator.next();
                    }
                };
            }
        };
    }
    
    public static class Serializer implements JsonSerializer<PropertyMap>, JsonDeserializer<PropertyMap> {

        @Override
        public PropertyMap deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            PropertyMap result = new PropertyMap();
            //Old object-of-lists representation?
            //i.e. {k1:[v1,v2,v3], k2:[v4,v5,v6]}
            if( json instanceof JsonObject ) {
                JsonObject obj = (JsonObject)json;
                for( Entry<String,JsonElement> entry : obj.entrySet() ) {
                    String key = entry.getKey();
                    JsonElement element = entry.getValue();
                    if( element instanceof JsonArray ) {
                        JsonArray array = (JsonArray)element;
                        for( JsonElement element2 : array )
                            result.put( key, new Property( key, element2.getAsString() ) );
                    }
                }
            //More recent array-based representation supporting signatures for properties.
            //i.e. [{"name": k1, "value": v1, "signature": s }, ...]
            //Honestly, I feel like this is more redundant and less efficient than the old form.
            //The key gets printed multiple times, and you have to serialize the names of the fields as well.
            } else if( json instanceof JsonArray ) {
                JsonArray arr = (JsonArray)json;
                for( JsonElement elem : arr ) {
                    if( elem instanceof JsonObject ) {
                        JsonObject obj      = (JsonObject)elem;
                        String     name     = obj.get( "name"  ).getAsString();
                        Property   property = new Property( name, obj.get( "value" ).getAsString() );
                        if( obj.has( "signature" ) )
                            property.setSignature( obj.get( "signature" ).getAsString() );
                        
                        result.put( name, property );
                    }
                }
            }
            return result;
        }

        @Override
        public JsonElement serialize(PropertyMap src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray result = new JsonArray();
            for( Property property : src.values() ) {
                JsonObject obj = new JsonObject();
                
                obj.addProperty( "name", property.getName() );
                obj.addProperty( "value", property.getValue() );
                
                String signature = property.getSignature();
                if( signature != null )
                    obj.addProperty( "signature", signature );
                
                result.add( obj );
            }
            return result;
        }
    }
    
    public static class LegacySerializer implements JsonSerializer<PropertyMap> {
        @Override
        public JsonElement serialize( PropertyMap src, Type typeOfSrc, JsonSerializationContext context ) {
            JsonObject result = new JsonObject();
            for( String key : src.keys() ) {
                JsonArray values = new JsonArray();
                for( Property property : src.get( key ) )
                    values.add( property.getValue() );
                result.add( key, values );
            }
            return result;
        }
    }
}
