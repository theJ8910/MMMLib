package com.mojang.launcher.updater.download.assets;

import java.util.Map;

public class AssetIndex {
    public class AssetObject {
        private String hash;
        private long size;
        
        //These don't appear to be used anywhere in the asset index .json files
        //private boolean reconstruct;
        //private String compressedHash;
        //private long compressedSize;
        
        public String getHash() {
            return this.hash;
        }
        public long getSize() {
            return this.size;
        }
        
        public void validate() {
            if( this.hash == null )
                throw new RuntimeException( "hash is null." );
        }
        
        public String getPath() {
            return this.hash.substring(0, 2) + "/" + this.hash;
        }
    }
    
    private Map<String,AssetObject> objects;
    private boolean virtual;
    
    /**
     * Returns the asset object map.
     * This maps an asset object's resource path to the asset object itself (containing the SHA-1 hash and the file's size in bytes).
     * @return
     */
    public Map<String,AssetObject> getObjects() {
        return this.objects;
    }
    
    /**
     * Returns whether or not the asset index is "virtual".
     * Minecraft 1.7.3+ uses a virtual file system of sorts. The asset index maps resource paths
     * to "objects" (stored by their SHA1 hash in the file system in the assets/objects/ folder).
     * Minecraft 1.7.2 and below uses a simpler system where the resources are stored in the file system under their resource paths;
     * for these versions, virtual will be set to true (indicating the asset should be downloaded to its resource path rather than the objects/ directory).
     * The "virtual" flag seems to be a misnomer, seeing as how it means the opposite of what you'd expect. 
     * @return true if the version does NOT use the virtual file system. False otherwise.
     */
    public boolean isVirtual() {
        return this.virtual;
    }
    
    public void validate() {
        if( this.objects == null )
            throw new RuntimeException( "objects is null." );
        for( AssetObject value : this.objects.values() )
            value.validate();
    }
}
