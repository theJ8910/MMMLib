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
* Handling User Logins (Authenticating with Mojang)
* Launching Minecraft
* Managing Minecraft instances
* Managing installed packages in instances (adding, removing, upgrading, etc).
* Downloading packages from upstream

Parts of MMMLib's source code are taken or based off of Mojang's vanilla Minecraft Launcher.
These files are in the com.mojang and net.minecraft packages; I claim no ownership for these files.

Building MMMLib
---------------
<a name="TOC-BuildingMMMLib"></a>
MMMLib has a few dependencies that need to be satisfied before building:
* [Apache Commons Lang](https://commons.apache.org/proper/commons-lang/download_lang.cgi) (3.4+)
* [Gson](https://github.com/google/gson) (2.6.1+)
* [SQLite JDBC](https://github.com/xerial/sqlite-jdbc) (3.8.11.2+)

Besides these libraries, MMMLib has no special build considerations.