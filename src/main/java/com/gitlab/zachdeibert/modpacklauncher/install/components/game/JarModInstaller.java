package com.gitlab.zachdeibert.modpacklauncher.install.components.game;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import com.gitlab.zachdeibert.modpacklauncher.StreamUtils;
import com.gitlab.zachdeibert.modpacklauncher.install.Directory;
import com.gitlab.zachdeibert.modpacklauncher.install.InstallationComponent;

public class JarModInstaller implements InstallationComponent {
    private final ConstructorArguments args;
    
    protected void installJarMod(final JarOutputStream jarFile, final List<String> files, final String url) throws IOException {
        final InputStream in = StreamUtils.download(url);
        final ZipInputStream zip = new ZipInputStream(in);
        ZipEntry entry;
        while ( (entry = zip.getNextEntry()) != null ) {
            final String name = entry.getName();
            if ( ! (files.contains(name) || name.matches("META-INF/MOJANG.*")) ) {
                files.add(name);
                jarFile.putNextEntry(entry);
                if ( !entry.isDirectory() ) {
                    StreamUtils.copy(zip, jarFile);
                }
            }
        }
        zip.close();
        in.close();
    }
    
    @Override
    public void install() throws Exception {
        args.progress.setSteps(args.install.jarMods.size());
        if ( !args.install.jarMods.isEmpty() ) {
            final File jar = Directory.MC_JAR.getFile(args);
            jar.getParentFile().mkdirs();
            final FileOutputStream jarOut = new FileOutputStream(jar);
            final JarOutputStream jarFile = new JarOutputStream(jarOut);
            final List<String> files = new LinkedList<String>();
            Collections.reverse(args.install.jarMods);
            try {
                for ( final String url : args.install.jarMods ) {
                    installJarMod(jarFile, files, url);
                    args.progress.stepForward();
                }
            } finally {
                Collections.reverse(args.install.jarMods);
            }
            jarFile.close();
            jarOut.close();
        }
    }
    
    public JarModInstaller(final ConstructorArguments args) {
        this.args = args;
    }
}
