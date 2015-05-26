package com.gitlab.zachdeibert.modpacklauncher.install;

import com.gitlab.zachdeibert.modpacklauncher.InstallConfiguration;
import com.gitlab.zachdeibert.modpacklauncher.RuntimeConfiguration;
import com.gitlab.zachdeibert.modpacklauncher.SystemConfiguration;

public interface InstallationComponent {
    public static class ConstructorArguments {
        public InstallConfiguration install;
        public RuntimeConfiguration runtime;
        public SystemConfiguration  system;
        public ProgressHandler      progress;
    }
    
    void install() throws Exception;
}
