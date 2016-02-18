package net.theJ89.MMMLib;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.commons.lang3.JavaVersion;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.launcher.updater.DateTypeAdapter;
import com.mojang.launcher.updater.LowerCaseEnumTypeAdapterFactory;

import net.minecraft.launcher.updater.CompleteMinecraftVersion;
import net.theJ89.util.OperatingSystem;
import net.theJ89.util.Platform;
import net.theJ89.util.Size;

public class MinecraftLauncher {
    //TODO: Actually make this launch a Minecraft instance
    public static void launch( String id, boolean client ) throws IOException {
        Path instanceDir = Paths.get( "instance" );
        
        JavaLauncher l = new JavaLauncher();
        
        l.setUseConcMarkSweepGC( true );
        l.setUseCMSIncrementalMode( true );
        l.setUseAdaptiveSizePolicy( true );
        
        //Set nursery and max heap sizes
        l.setNurserySize( 128 * Size.MEGABYTE );
        l.setMaxHeapSize( Platform.getBitness() == 32 ? 512 * Size.MEGABYTE : Size.GIGABYTE );
        
        //Manually set permgen if we're using something older than Java 1.8
        if( !Platform.getJavaVersion().atLeast( JavaVersion.JAVA_1_8 ) )
            l.setMetaspaceSize( 128 * Size.MEGABYTE );
        
        //Set class paths
        //The Minecraft instance .jar and any Java libraries it needs should be listed here.
        l.setClassPathsIL( "minecraft.jar", "bin/jinput.jar", "bin/lwjgl.jar", "bin/lwjgl_util.jar" );
        
        //Set native library dir
        l.setLibraryPathsIL( "bin/natives" );
        
        //Set classname
        l.setClassname( "net.minecraft.client.Minecraft" );
        
        //Add Minecraft arguments
        l.setArgumentsIL( "username", "password" );
        
        //Set working directory and environment if necessary
        if( Platform.getOS() == OperatingSystem.WINDOWS && client ) {
            l.setEnvironmentIL( "APPDATA", instanceDir.toString() );
            l.setWorkingDirectory( instanceDir.resolve( ".minecraft" ) );
        } else {
            l.setWorkingDirectory( instanceDir );
        }
        
        //Run minecraft
        l.launch();
    }
}
