MMMLib
=========

1. [Overview](#TOC-Overview)
2. [Building MMMLib](#TOC-BuildingMMMLib)

Overview
--------
<a name="TOC-Overview"></a>
MMMLib stands for Minecraft Mod Manager Library.

This is the back end of my Mod Manager, which handles all the heavy lifting the Mod Manager performs.

This includes, but is not limited to:
* Downloading and Installing Minecraft and Forge
* Managing Minecraft instances
* Managing installed packages in instances (adding, removing, upgrading, etc).
* Downloading packages from upstream
* Handling User Logins (Authenticating with Mojang)
* Launching Minecraft

Parts of MMMLib's source code are taken from or based off of Mojang's vanilla [Minecraft Launcher](http://www.minecraft.net/), the [Simple Forge Installer](https://github.com/MinecraftForge/Installer), and [Apache Commons](https://commons.apache.org/) IO + Lang.
Mojang Launcher classes are in the com.mojang and net.minecraft packages; I claim no ownership for these files.

Building MMMLib
---------------
<a name="TOC-BuildingMMMLib"></a>
MMMLib has a few dependencies that need to be satisfied before building:
* [Jsoup](https://jsoup.org/download) (1.9.1+)
* [Gson](https://github.com/google/gson) (2.6.1+)
* [SQLite JDBC](https://github.com/xerial/sqlite-jdbc) (3.8.11.2+)
* [XZ for Java](http://tukaani.org/xz/java.html) (1.5+)

Besides these libraries, MMMLib has no special build considerations.