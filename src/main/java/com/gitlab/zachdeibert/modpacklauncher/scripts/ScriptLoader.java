package com.gitlab.zachdeibert.modpacklauncher.scripts;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.util.GroovyScriptEngine;
import java.io.File;
import java.util.List;
import com.gitlab.zachdeibert.modpacklauncher.InstallConfiguration;
import com.gitlab.zachdeibert.modpacklauncher.SystemConfiguration;

public class ScriptLoader extends GroovyScriptEngine implements Runnable {
    private static class Block {
        @SuppressWarnings("unused")
        public void call(final Closure<?> closure) {
            closure.setDelegate(this);
            closure.run();
        }
    }
    private class InfoBlock extends Block {
        @SuppressWarnings("unused")
        public void name(final String arg) {
            system.name = arg;
        }
        
        @SuppressWarnings("unused")
        public void mainClass(final String arg) {
            system.mainClass = arg;
        }
        
        @SuppressWarnings("unused")
        public void version(final String arg) {
            system.version = arg;
        }
        
        @SuppressWarnings("unused")
        public void installTo(final File arg) {
            system.installDir = arg;
        }
        
        public File subdir(final File arg0, final String arg1) {
            return new File(arg0, arg1);
        }
        
        @SuppressWarnings("unused")
        public File subdir(final String arg0, final String arg1) {
            return subdir(new File(arg0), arg1);
        }
        
        public File minecraftDir() {
            final String OS = System.getProperty("os.name").toLowerCase();
            if ( OS.indexOf("win") >= 0 ) {
                return new File(String.format("%s/.minecraft", System.getenv("appdata")));
            } else if ( OS.indexOf("mac") >= 0 || OS.indexOf("darwin") >= 0 ) {
                return new File(String.format("%s/Library/Application Support/minecraft", System.getenv("HOME")));
            } else {
                return new File(String.format("%s/.minecraft", System.getenv("HOME")));
            }
        }
        
        public File minecraftSubdir(final String arg) {
            return subdir(minecraftDir(), arg);
        }
        
        @SuppressWarnings("unused")
        public File minecraftSubdir() {
            return minecraftSubdir(String.format("modpacks/%s", system.name));
        }
        
        public File currentDir() {
            return new File(System.getProperty("user.dir"));
        }
        
        public File currentSubdir(final String arg) {
            return subdir(currentDir(), arg);
        }
        
        @SuppressWarnings("unused")
        public File currentSubdir() {
            return currentSubdir(system.name);
        }
        
        @SuppressWarnings("unused")
        public File dir(final String arg) {
            return new File(arg);
        }
    }
    private static class ModBlock extends Block {
        private final List<String> list;
        
        @SuppressWarnings("unused")
        public void install(final String arg) {
            list.add(arg);
        }
        
        public ModBlock(final List<String> list) {
            this.list = list;
        }
    }
    private class JarModBlock extends ModBlock {
        public String minecraft(final String arg) {
            return String.format("https://s3.amazonaws.com/Minecraft.Download/versions/%1$s/%1$s.jar", arg);
        }

        @SuppressWarnings("unused")
        public String minecraft() {
            return minecraft(system.version);
        }
        
        public String forge(final String arg0, final String arg1) {
            return String.format("http://files.minecraftforge.net/maven/net/minecraftforge/forge/%1$s-%2$s-%1$s/forge-%1$s-%2$s-%1$s-universal.jar", arg0, arg1);
        }

        @SuppressWarnings("unused")
        public String forge(final String arg) {
            return forge(system.version, arg);
        }
        
        public JarModBlock() {
            super(install.jarMods);
        }
    }
    private class LibraryBlock extends ModBlock {
        @SuppressWarnings("unused")
        public void addSource(final String arg) {
            install.libRepos.add(arg);
        }
        
        public LibraryBlock() {
            super(install.libs);
        }
    }
    private class CoreModsBlock extends ModBlock {
        public CoreModsBlock() {
            super(install.coreMods);
        }
    }
    private class ModsBlock extends ModBlock {
        public ModsBlock() {
            super(install.loaderMods);
        }
    }
    private class ConfigurationBlock extends Block {
        @SuppressWarnings("unused")
        public void from(final String arg) {
            install.configs.add(arg);
        }
    }
    private final Binding              binding;
    private final SystemConfiguration  system;
    private final InstallConfiguration install;
    
    @Override
    public void run() {
        try {
            run("modpack.info", binding);
        } catch ( final Exception ex ) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
    
    public ScriptLoader(final SystemConfiguration system, final InstallConfiguration install) {
        super(new ClassLoaderResourceConnector());
        this.system = system;
        this.install = install;
        binding = new Binding();
        binding.setVariable("info", new InfoBlock());
        binding.setVariable("jar", new JarModBlock());
        binding.setVariable("libraries", new LibraryBlock());
        binding.setVariable("coremods", new CoreModsBlock());
        binding.setVariable("mods", new ModsBlock());
        binding.setVariable("configuration", new ConfigurationBlock());
    }
}
