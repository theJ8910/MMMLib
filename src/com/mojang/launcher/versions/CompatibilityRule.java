package com.mojang.launcher.versions;

import net.theJ89.util.OperatingSystem;
import net.theJ89.util.Target;

public class CompatibilityRule {
    //Indicates whether the target is compatible (ALLOW) or incompatible (DISALLOW).
    public static enum Action {
        ALLOW,
        DISALLOW
    }
    
    public class OSRestriction {
        private OperatingSystem name;
        
        //Note: these are regular expressions
        private String          version;
        private String          arch;
        
        public OperatingSystem getName() {
            return this.name;
        }
        
        public String getVersion() {
            return this.version;
        }
        
        public String getArch() {
            return this.arch;
        }
    }
    
    private Action action;
    private OSRestriction os;
    
    public Action getAction() {
        return this.action;
    }
    
    public OSRestriction getOS() {
        return this.os;
    }
    
    public boolean appliesTo( Target target ) {
        return this.os == null                                                               ||
               ( this.os.name    == null || target.getOS()     .equals(  this.os.name    ) ) &&
               ( this.os.version == null || target.getVersion().matches( this.os.version ) ) &&
               ( this.os.arch    == null || target.getArch()   .matches( this.os.arch    ) );
    }
}
