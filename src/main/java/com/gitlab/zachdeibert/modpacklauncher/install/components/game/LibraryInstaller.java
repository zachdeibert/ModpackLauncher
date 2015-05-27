package com.gitlab.zachdeibert.modpacklauncher.install.components.game;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import com.gitlab.zachdeibert.modpacklauncher.StreamUtils;
import com.gitlab.zachdeibert.modpacklauncher.install.Directory;
import com.gitlab.zachdeibert.modpacklauncher.install.InstallationComponent;
import com.gitlab.zachdeibert.modpacklauncher.install.Maven;

public class LibraryInstaller implements InstallationComponent {
    private static final String        ARCH = System.getProperty("sun.arch.data.model");
    private final ConstructorArguments args;
    private final List<Thread>         threads;
    
    protected void extractNatives(final File nativeDir, final String fileName, final File file) {
        try {
            final FileInputStream fis = new FileInputStream(file);
            final ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry entry;
            while ( (entry = zis.getNextEntry()) != null ) {
                final String name = entry.getName();
                if ( !entry.isDirectory() && name.indexOf("META-INF") < 0 ) {
                    final File out = new File(nativeDir, name);
                    out.getParentFile().mkdirs();
                    final FileOutputStream fos = new FileOutputStream(out);
                    StreamUtils.copy(zis, fos);
                    fos.close();
                }
            }
            zis.close();
            fis.close();
        } catch ( final Exception ex ) {
            throw new RuntimeException(ex);
        }
    }
    
    protected void installLibrary(final File dir, final File nativeDir, final String artifact, final String fileName, final File file, final Maven mvn) throws IOException, FileNotFoundException {
        file.getParentFile().mkdirs();
        StreamUtils.copyAndClose(mvn.download(artifact), new FileOutputStream(file));
    }
    
    protected void installLibraryAndNatives(final File dir, final File nativeDir, final String artifact, final Maven mvn) throws IOException, FileNotFoundException {
        final String parts[] = artifact.split(":");
        final String fileName = String.format("%s/%s/%s/%2$s-%3$s%s.jar", parts[0].replace('.', '/'), parts[1], parts[2], parts.length < 4 ? "" : "-".concat(parts[3]));
        final File file = new File(dir, fileName);
        installLibrary(dir, nativeDir, artifact, fileName, file, mvn);
        if ( fileName.indexOf("native") >= 0 && ! ( (fileName.indexOf("64") >= 0 && !ARCH.equals("32")) || (fileName.indexOf("32") >= 0 && !ARCH.equals("64"))) ) {
            final Thread thread = new Thread(() -> extractNatives(nativeDir, fileName, file));
            synchronized ( threads ) {
                threads.add(thread);
                thread.start();
            }
        }
    }
    
    @Override
    public void install() throws Exception {
        args.progress.setSteps(args.install.libs.size());
        if ( !args.install.libs.isEmpty() ) {
            final File dir = Directory.LIBRARY_DIR.getFile(args);
            final File nativeDir = Directory.NATIVES_DIR.getFile(args);
            dir.mkdirs();
            final Maven mvn = new Maven();
            mvn.addRepos(args.install.libRepos);
            for ( final String artifact : args.install.libs ) {
                installLibraryAndNatives(dir, nativeDir, artifact, mvn);
                args.progress.stepForward();
            }
        }
        synchronized( threads ) {
            for ( final Thread thread : threads ) {
                thread.join();
            }
        }
    }
    
    public LibraryInstaller(final ConstructorArguments args) {
        this.args = args;
        threads = Collections.synchronizedList(new LinkedList<Thread>());
    }
}
