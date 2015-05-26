package com.gitlab.zachdeibert.modpacklauncher;

import java.awt.Component;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;
import com.gitlab.zachdeibert.modpacklauncher.install.Directory;
import com.gitlab.zachdeibert.modpacklauncher.install.Installer;

public class Launcher {
    private final SystemConfiguration  system;
    private final RuntimeConfiguration runtime;
    
    public List<String> getJars(final File dir) {
        final List<String> list = new LinkedList<String>();
        for ( final File file : dir.listFiles() ) {
            if ( file.isDirectory() ) {
                list.addAll(getJars(file));
            } else {
                final String name = file.getPath();
                if ( name.endsWith(".jar") ) {
                    list.add(name);
                }
            }
        }
        return list;
    }
    
    public void launch(final Component comp) {
        final Installer instlr = new Installer(system, null);
        if ( !instlr.isInstalled() ) {
            JOptionPane.showMessageDialog(null, "The modpack is not installed.", "Launching Modpack", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ( runtime.name == null || runtime.name.isEmpty() ) {
            JOptionPane.showMessageDialog(null, "You must login before playing.", "Launching Modpack", JOptionPane.ERROR_MESSAGE);
            return;
        }
        final List<String> jars = getJars(Directory.LIBRARY_DIR.getFile(system));
        jars.add(Directory.MC_JAR.getFile(system).getAbsolutePath());
        final List<String> cmd = new LinkedList<String>();
        cmd.add("java");
        cmd.add("-cp");
        cmd.add(String.join(":", jars));
        cmd.add("-Djava.library.path=".concat(Directory.NATIVES_DIR.getFile(system).getAbsolutePath()));
        cmd.add("-Dfml.ignoreInvalidMinecraftCertificates=true");
        cmd.add("-Xmx".concat(runtime.memMax));
        cmd.add("-Xms".concat(runtime.memMin));
        cmd.add(system.mainClass);
        cmd.add("--username");
        cmd.add(runtime.name);
        cmd.add("--uuid");
        cmd.add(runtime.UUID);
        cmd.add("--accessToken");
        cmd.add(runtime.token);
        cmd.add("--userProperties");
        cmd.add("{}");
        cmd.add("--version");
        cmd.add(system.version);
        cmd.add("--assetsDir");
        cmd.add(Directory.ASSET_DIR.getFile(system).getAbsolutePath());
        cmd.add("--assetIndex");
        cmd.add(system.version);
        cmd.add("--tweakClass");
        cmd.add("cpw.mods.fml.common.launcher.FMLTweaker");
        System.out.println("Launching Minecraft:");
        System.out.println(cmd);
        final Runtime rt = Runtime.getRuntime();
        comp.setVisible(false);
        try {
            final Process proc = rt.exec(cmd.toArray(new String[0]), null, Directory.GAME_DIR.getFile(system));
            new StreamUtils.OutputRedirThread(proc.getInputStream(), System.out).start();
            new StreamUtils.OutputRedirThread(proc.getErrorStream(), System.err).start();
            int exit = proc.waitFor();
            (exit == 0 ? System.out : System.err).printf("Minecraft exited with code %d\n", exit);
            if ( exit != 0 ) {
                System.exit(exit);
            }
        } catch ( final Exception ex ) {
            ex.printStackTrace();
            System.exit(1);
        }
        comp.setVisible(true);
    }
    
    public Launcher(final SystemConfiguration system, final RuntimeConfiguration runtime) {
        this.system = system;
        this.runtime = runtime;
    }
}
