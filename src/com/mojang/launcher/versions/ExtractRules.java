package com.mojang.launcher.versions;

import java.util.ArrayList;
import java.util.List;

public class ExtractRules {
    private List<String> exclude;
    
    public ExtractRules() {
        this.exclude = new ArrayList<String>();
    }
    
    public List<String> getExclude() {
        return this.exclude;
    }
    
    public boolean shouldExtract( String path ) {
        if( this.exclude != null ) {
            for( String pathPrefix : this.exclude )
                if( path.startsWith( pathPrefix ) )
                    return false;
        }
        return true;
    }
}
