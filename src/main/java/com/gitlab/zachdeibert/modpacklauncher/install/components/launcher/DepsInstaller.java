package com.gitlab.zachdeibert.modpacklauncher.install.components.launcher;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import com.gitlab.zachdeibert.modpacklauncher.InstallConfiguration;
import com.gitlab.zachdeibert.modpacklauncher.StreamUtils;
import com.gitlab.zachdeibert.modpacklauncher.gui.Window;
import com.gitlab.zachdeibert.modpacklauncher.install.LauncherInstallationComponent;
import com.gitlab.zachdeibert.modpacklauncher.install.Maven;

public class DepsInstaller implements LauncherInstallationComponent {
    private static class Streams implements Closeable {
        public InputStream    is;
        public JarInputStream jis;
        
        @Override
        public void close() throws IOException {
            is.close();
            jis.close();
        }
    }
    private static final String        DEPS[] = new String[] {
        "com.google.code.findbugs:jsr305:2.0.1",
        "com.google.code.gson:gson:2.2.4",
        "com.google.guava:guava:17.0",
        "com.mojang:authlib:1.5.21",
        "commons-codec:commons-codec:1.9",
        "commons-io:commons-io:2.4",
        "org.apache.commons:commons-lang3:3.3.2",
        "org.apache.logging.log4j:log4j-api:2.0-beta9",
        "org.apache.logging.log4j:log4j-core:2.0-beta9"
                                              };
    private final Maven                mvn;
    private final InstallConfiguration config;
    private final Window               win;
    
    protected void addRepos(final String... repos) {
        mvn.addRepos(repos);
    }
    
    protected Streams download(final String artifact) throws IOException {
        final Streams str = new Streams();
        str.is = mvn.download(artifact);
        str.jis = new JarInputStream(str.is);
        return str;
    }
    
    protected File getSelf() throws IOException {
        return new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
    }
    
    protected void getArtifacts(final String... artifacts) throws IOException {
        final Streams streams[] = new Streams[artifacts.length];
        final JarInputStream jiss[] = new JarInputStream[artifacts.length + 1];
        for ( int i = 0; i < artifacts.length; i++ ) {
            streams[i] = download(artifacts[i]);
            jiss[i + 1] = streams[i].jis;
        }
        final File self = getSelf();
        final File backup = new File(self.getAbsolutePath().concat("~"));
        final FileInputStream fis = new FileInputStream(self);
        final JarInputStream jis = new JarInputStream(fis);
        final FileOutputStream fos = new FileOutputStream(backup);
        final JarOutputStream jos = new JarOutputStream(fos);
        jiss[0] = jis;
        StreamUtils.mergeJars(jos, jiss);
        jos.close();
        fos.close();
        jis.close();
        fis.close();
        for ( final Streams stream : streams ) {
            stream.close();
        }
    }
    
    protected void restart() throws IOException {
        win.setVisible(false);
        final File thisJar = getSelf();
        final String self = thisJar.getAbsolutePath();
        final File relauncher = new File(self.concat("~~"));
        final InputStream is = ClassLoader.getSystemResourceAsStream("com/gitlab/zachdeibert/modpacklauncher/install/components/launcher/Relauncher.class");
        final FileOutputStream os = new FileOutputStream(relauncher);
        final JarOutputStream jos = new JarOutputStream(os);
        final Manifest man = new Manifest();
        final Attributes attr = man.getMainAttributes();
        attr.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attr.put(Attributes.Name.MAIN_CLASS, "com.gitlab.zachdeibert.modpacklauncher.install.components.launcher.Relauncher");
        final JarEntry manifest = new JarEntry(JarFile.MANIFEST_NAME);
        jos.putNextEntry(manifest);
        man.write(jos);
        final JarEntry main = new JarEntry("com/gitlab/zachdeibert/modpacklauncher/install/components/launcher/Relauncher.class");
        jos.putNextEntry(main);
        StreamUtils.copyAndClose(is, jos);
        os.close();
        final String cmd[] = new String[] {
            "java",
            "-jar",
            self.concat("~~"),
            self.concat("~"),
            self
        };
        Runtime.getRuntime().exec(cmd);
        System.exit(0);
    }
    
    @Override
    public boolean isInstalled() {
        try {
            Class.forName("com.mojang.authlib.AuthenticationService");
            return true;
        } catch ( final Exception ex ) {
            return false;
        }
    }
    
    @Override
    public void install() throws Exception {
        addRepos(config.libRepos.toArray(new String[0]));
        getArtifacts(DEPS);
        restart();
    }
    
    private DepsInstaller(final InstallConfiguration config, final Window win, final byte b) {
        mvn = new Maven();
        this.config = config;
        this.win = win;
    }
    
    protected DepsInstaller(final Window win) {
        this(null, win);
    }
    
    public DepsInstaller(final InstallConfiguration config, final Window win) {
        this(config, win, (byte) 0);
    }
}
