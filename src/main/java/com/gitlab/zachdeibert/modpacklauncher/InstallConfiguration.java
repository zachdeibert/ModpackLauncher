package com.gitlab.zachdeibert.modpacklauncher;

import java.util.ArrayList;
import java.util.List;

public class InstallConfiguration {
    public final List<String> jarMods;
    public final List<String> coreMods;
    public final List<String> loaderMods;
    public final List<String> configs;
    public final List<String> libRepos;
    public final List<String> libs;
    
    @Override
    public String toString() {
        return String.format("InstallConfiguration [jarMods=%s, coreMods=%s, loaderMods=%s, configs=%s, libRepos=%s, libs=%s]", jarMods, coreMods, loaderMods, configs, libRepos, libs);
    }

    public InstallConfiguration() {
        jarMods = new ArrayList<String>();
        coreMods = new ArrayList<String>();
        loaderMods = new ArrayList<String>();
        configs = new ArrayList<String>();
        libRepos = new ArrayList<String>();
        libs = new ArrayList<String>();
    }
}
