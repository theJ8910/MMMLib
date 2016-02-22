package net.theJ89.MMMLib;

public class MinecraftConstants {
    //Note: The following directories (versions, libraries, natives) are relative to the instance directory:
    //Where Minecraft version executables and info are stored
    public static final String VERSIONS_DIRECTORY = "versions";
    
    //Where Java libraries Minecraft needs are stored
    public static final String LIBRARIES_DIRECTORY = "libraries";
    
    //Directory we can find the natives in
    public static final String NATIVES_DIRECTORY = "natives";
    
    //Directory we can find the assets in
    public static final String ASSETS_DIRECTORY = "assets";
    
    //Virtual assets root (inside of ASSETS_DIRECTORY) for versions that need it
    public static final String ASSETS_VIRTUAL_DIRECTORY = "virtual";
    
    //Directory we can find asset indices in (inside of ASSETS_DIRECTORY)
    public static final String ASSETS_INDICES_DIRECTORY = "indexes";
    
    //Directory we can find asset objects in (inside of the ASSETS_DIRECTORY).
    public static final String ASSETS_OBJECTS_DIRECTORY = "objects";
    
    private MinecraftConstants() {
        throw new Error();
    }
}
