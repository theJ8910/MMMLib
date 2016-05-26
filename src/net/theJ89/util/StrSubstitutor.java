package net.theJ89.util;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple replacement for StrSubstitutor from Apache Lang 3.
 * 
 * This implementation isn't as feature rich as Apache Lang's StrSubstitutor,
 * but for what I was using it for this implemenation should suffice.
 */
public class StrSubstitutor {
    private static final Pattern RE_PLACEHOLDER = Pattern.compile( "\\$\\{(\\w+)\\}" );
    
    private Map< String, String > map;
    
    public StrSubstitutor( Map< String, String > map ) {
        this.map = map;
    }
    
    public String replace( final String format ) {
        Matcher matcher = RE_PLACEHOLDER.matcher( format );
        StringBuilder sb = new StringBuilder();
        int previousEnd = 0;
        while( matcher.find() ) {
            String key = matcher.group( 1 );
            String value = this.map.get( key );
            if( value == null )
                throw new NoSuchElementException();
            sb.append( format.substring( previousEnd, matcher.start() ) );
            sb.append( value );
            previousEnd = matcher.end();
        }
        sb.append( format.substring( previousEnd ) );
        return sb.toString();
    }
}
