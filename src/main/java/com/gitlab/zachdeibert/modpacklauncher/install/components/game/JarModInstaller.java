package com.gitlab.zachdeibert.modpacklauncher.install.components.game;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import com.gitlab.zachdeibert.modpacklauncher.StreamUtils;
import com.gitlab.zachdeibert.modpacklauncher.install.Directory;
import com.gitlab.zachdeibert.modpacklauncher.install.InstallationComponent;

public class JarModInstaller implements InstallationComponent {
    private static class Streams implements Closeable {
        public final InputStream is;
        public final JarInputStream jis;
        
        @Override
        public void close() throws IOException {
            is.close();
            jis.close();
        }
        
        public Streams(final String url) throws IOException {
            this.is = StreamUtils.download(url);
            this.jis = new JarInputStream(is);
        }
    }
    private final ConstructorArguments args;
    
    @Override
    public void install() throws Exception {
        args.progress.setSteps(args.install.jarMods.size());
        if ( !args.install.jarMods.isEmpty() ) {
            final File jar = Directory.MC_JAR.getFile(args);
            jar.getParentFile().mkdirs();
            final FileOutputStream jarOut = new FileOutputStream(jar);
            final JarOutputStream jarFile = new JarOutputStream(jarOut);
            Collections.reverse(args.install.jarMods);
            try {
                final Streams streams[] = new Streams[args.install.jarMods.size()];
                final JarInputStream jiss[] = new JarInputStream[streams.length];
                for ( int i = 0; i < streams.length; i++ ) {
                    streams[i] = new Streams(args.install.jarMods.get(i));
                    jiss[i] = streams[i].jis;
                }
                StreamUtils.mergeJars(jarFile, new Predicate<ZipEntry>() {
                    @Override
                    public boolean test(final ZipEntry t) {
                        return !t.getName().matches("META-INF/MOJANG.*");
                    }
                }, jiss);
            } finally {
                Collections.reverse(args.install.jarMods);
                jarFile.close();
                jarOut.close();
            }
        }
    }
    
    public JarModInstaller(final ConstructorArguments args) {
        this.args = args;
    }
}
