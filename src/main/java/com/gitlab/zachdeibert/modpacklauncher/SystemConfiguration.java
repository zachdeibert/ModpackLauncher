package com.gitlab.zachdeibert.modpacklauncher;

import java.io.File;

public class SystemConfiguration {
    public String name;
    public String mainClass;
    public String version;
    public File   installDir;
    
    @Override
    public String toString() {
        return String.format("SystemConfiguration [name=%s, mainClass=%s, version=%s, installDir=%s]", name, mainClass, version, installDir);
    }
}
