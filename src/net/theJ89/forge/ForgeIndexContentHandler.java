package net.theJ89.forge;

public interface ForgeIndexContentHandler {
    /**
     * Called when we have started parsing an index of forge versions for a particular version of Minecraft.
     * @param name - The name of the Minecraft version (e.g. "1.9")
     * @param latest - Latest version of Forge for this version of Minecraft. Can be null.
     * @param recommended - Recommended version of Forge for this version of Minecraft. Can be null.
     * @throws Exception
     */
    public void startMinecraft( MinecraftVersion mv ) throws Exception;
    /**
     * Called when we have finished parsing an index of forge versions for a particular version of Minecraft.
     * @param name - The name of the Minecraft version (e.g. "1.9")
     * @param latest - Latest version of Forge for this version of Minecraft. Can be null.
     * @param recommended - Recommended version of Forge for this version of Minecraft. Can be null.
     * @throws Exception
     */
    public void endMinecraft( MinecraftVersion mv ) throws Exception;
    /**
     * Called after we have parsed basic information for a version of Forge, but before parsing its downloads.
     * @param name - The name of the Forge version (e.g. "11.15.1.1902")
     * @param time - The date + time the Forge version was released
     * @throws Exception
     */
    public void forge( ForgeVersion fv ) throws Exception;
    /**
     * Called after parsing a download for the current forge version, but before sending a HEAD request to find the file size.
     * The download can be a client, server, or universal .jar or .zip file.
     * Other types of downloads are ignored.
     * @param fd - Information about the Forge download.
     * @return boolean - true if we should send a HEAD request to get the file size. False otherwise.
     * @throws Exception
     */
    public boolean startDownload( ForgeDownload fd ) throws Exception;
    
    /**
     * Called after parsing a download for the current forge version and (optionally) sending a HEAD request to find the file size.
     * The download can be a client, server, or universal .jar or .zip file.
     * Other types of downloads are ignored.
     * @param fd - Information about the Forge download.
     * @throws Exception
     */
    public void endDownload( ForgeDownload fd ) throws Exception;
}
