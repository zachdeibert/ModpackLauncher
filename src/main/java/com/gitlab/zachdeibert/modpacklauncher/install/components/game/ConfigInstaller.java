package com.gitlab.zachdeibert.modpacklauncher.install.components.game;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import com.gitlab.zachdeibert.modpacklauncher.StreamUtils;
import com.gitlab.zachdeibert.modpacklauncher.install.Directory;
import com.gitlab.zachdeibert.modpacklauncher.install.InstallationComponent;

public class ConfigInstaller implements InstallationComponent {
    private final ConstructorArguments args;
    
    protected void installConfig(final File dir, final String url) throws IOException, FileNotFoundException {
        final InputStream in = StreamUtils.download(url);
        final ZipInputStream zis = new ZipInputStream(in);
        ZipEntry entry;
        while ( (entry = zis.getNextEntry()) != null ) {
            if ( !entry.isDirectory() ) {
                final File out = new File(dir, entry.getName());
                out.getParentFile().mkdirs();
                final FileOutputStream fos = new FileOutputStream(out);
                StreamUtils.copy(zis, fos);
                fos.close();
            }
        }
        zis.close();
        in.close();
    }
    
    @Override
    public void install() throws Exception {
        args.progress.setSteps(args.install.configs.size());
        if ( !args.install.configs.isEmpty() ) {
            final File dir = Directory.CONFIG_DIR.getFile(args);
            for ( final String url : args.install.configs ) {
                installConfig(dir, url);
                args.progress.stepForward();
            }
        }
    }
    
    public ConfigInstaller(final ConstructorArguments args) {
        this.args = args;
    }
}
