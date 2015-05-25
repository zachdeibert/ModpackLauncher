package com.gitlab.zachdeibert.modpacklauncher.install.components.game;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import com.gitlab.zachdeibert.modpacklauncher.StreamUtils;
import com.gitlab.zachdeibert.modpacklauncher.install.InstallationComponent;

public class ModInstaller implements InstallationComponent {
    private final ConstructorArguments args;
    private final List<String>         mods;
    private final File                 dir;
    
    private String filename(final String url) {
        final int index = url.lastIndexOf('/');
        if ( index >= 0 ) {
            return url.substring(index);
        }
        return url;
    }
    
    @Override
    public void install() throws Exception {
        args.progress.setSteps(mods.size());
        if ( !mods.isEmpty() ) {
            dir.mkdirs();
            for ( final String url : mods ) {
                StreamUtils.copyAndClose(StreamUtils.download(url), new FileOutputStream(new File(dir, filename(url))));
                args.progress.stepForward();
            }
        }
    }
    
    public ModInstaller(final ConstructorArguments args, final List<String> mods, final String dir) {
        this.args = args;
        this.mods = mods;
        this.dir = new File(args.system.installDir, dir);
    }
    
    public ModInstaller(final ConstructorArguments args) {
        this(args, args.install.loaderMods, "mods");
    }
}
