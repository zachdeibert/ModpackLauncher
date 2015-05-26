package com.gitlab.zachdeibert.modpacklauncher.install.components.game;

import com.gitlab.zachdeibert.modpacklauncher.install.Directory;


public class CoreModInstaller extends ModInstaller {
    public CoreModInstaller(final ConstructorArguments args) {
        super(args, args.install.coreMods, Directory.COREMODS_DIR);
    }
}
