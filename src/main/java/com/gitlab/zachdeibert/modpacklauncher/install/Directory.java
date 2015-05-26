package com.gitlab.zachdeibert.modpacklauncher.install;

import java.io.File;
import com.gitlab.zachdeibert.modpacklauncher.SystemConfiguration;
import com.gitlab.zachdeibert.modpacklauncher.install.InstallationComponent.ConstructorArguments;

public enum Directory {
    GAME_DIR("/"),
    ASSET_DIR("/assets/"),
    INDEX_DIR("/assets/indexes/"),
    OBJECT_DIR("/assets/objects/"),
    CONFIG_DIR("/config/"),
    COREMODS_DIR("/coremods/"),
    MODS_DIR("/mods/"),
    LIBRARY_DIR("/libraries/"),
    NATIVES_DIR("/natives/"),
    MC_JAR("/minecraft.jar"),
    RUNTIME_SER("/assets/runtime.ser");
    
    private static final String SELF_HASH = getHash();
    private final String path;
    
    private static String getHash() {
        try {
        final String selfName = Directory.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        if ( selfName.endsWith(".jar") ) {
            final Hash hash = new Hash(new File(selfName));
            return hash.full;
        }
        } catch ( final Exception ex ) {
            ex.printStackTrace();
        }
        return "dev";
    }
    
    private File getGameDir(final SystemConfiguration system) {
        return new File(system.installDir, SELF_HASH);
    }
    
    public File getFile(final File gameDir) {
        return new File(gameDir, path);
    }
    
    public File getFile(final SystemConfiguration system) {
        return getFile(getGameDir(system));
    }
    
    public File getFile(final ConstructorArguments args) {
        return getFile(args.system);
    }
    
    private Directory(final String path) {
        this.path = path;
    }
}
