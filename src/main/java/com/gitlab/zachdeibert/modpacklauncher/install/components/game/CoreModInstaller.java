package com.gitlab.zachdeibert.modpacklauncher.install.components.game;


public class CoreModInstaller extends ModInstaller {
    public CoreModInstaller(final ConstructorArguments args) {
        super(args, args.install.coreMods, "coremods");
    }
}
