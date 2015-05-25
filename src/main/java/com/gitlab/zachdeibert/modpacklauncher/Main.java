package com.gitlab.zachdeibert.modpacklauncher;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import com.gitlab.zachdeibert.modpacklauncher.gui.Window;
import com.gitlab.zachdeibert.modpacklauncher.install.LauncherInstallationComponent;
import com.gitlab.zachdeibert.modpacklauncher.install.components.launcher.DepsInstaller;
import com.gitlab.zachdeibert.modpacklauncher.install.components.launcher.GroovyInstaller;
import com.gitlab.zachdeibert.modpacklauncher.scripts.ScriptLoader;

final class Main extends Thread {
    private final SystemConfiguration system;
    private final InstallConfiguration install;
    private final RuntimeConfiguration runtime;
    private Window win;
    private File rtc;
    
    @Override
    public void run() {
        try {
            RuntimeConfiguration.save(rtc, runtime);
        } catch ( final IOException ex ) {
            ex.printStackTrace();
        }
    }
    
    public static void main(final String[] args) throws Throwable {
        final LauncherInstallationComponent groovy = new GroovyInstaller();
        if ( !groovy.isInstalled() ) {
            groovy.install();
        }
        final Main m = new Main();
        try {
            EventQueue.invokeAndWait(() -> {
                m.win = new Window(m.system, m.install, m.runtime);
                m.win.setVisible(true);
            });
        } catch ( final Exception ex ) {
            ex.printStackTrace();
        }
        final ScriptLoader loader = new ScriptLoader(m.system, m.install);
        loader.run();
        final LauncherInstallationComponent deps = new DepsInstaller(m.install);
        if ( !deps.isInstalled() ) {
            deps.install();
        }
        m.rtc = new File(m.system.installDir, "runtime.ser");
        m.runtime.copy(RuntimeConfiguration.load(m.rtc));
        m.win.setName(m.system.name);
        System.out.println("Loaded configuration:");
        System.out.println(m.system);
        System.out.println(m.install);
        System.out.println(m.runtime);
        Runtime.getRuntime().addShutdownHook(m);
        m.win.onLoadingFinished();
    }
    
    private Main() {
        system = new SystemConfiguration();
        install = new InstallConfiguration();
        runtime = new RuntimeConfiguration();
    }
}
