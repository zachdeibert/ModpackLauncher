package com.gitlab.zachdeibert.modpacklauncher.install.components.launcher;

import java.io.IOException;

public class GroovyInstaller extends DepsInstaller {
    @Override
    public boolean isInstalled() {
        try {
            Class.forName("groovy.util.GroovyScriptEngine");
            return true;
        } catch ( final Exception ex ) {
            return false;
        }
    }
    
    @Override
    public void install() throws IOException {
        addRepos("http://repo1.maven.org/maven2/");
        getArtifacts("org.codehaus.groovy:groovy-all:2.3.9");
        restart();
    }
}
