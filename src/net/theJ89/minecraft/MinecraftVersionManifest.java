package net.theJ89.minecraft;

import java.util.List;

import net.minecraft.launcher.updater.PartialMinecraftVersion;

public class MinecraftVersionManifest {
    private MinecraftLatestVersions         latest;
    private List< PartialMinecraftVersion > versions;
    
    public MinecraftLatestVersions getLatestVersions() {
        return this.latest;
    }
    
    public List< PartialMinecraftVersion > getVersions() {
        return this.versions;
    }
}
