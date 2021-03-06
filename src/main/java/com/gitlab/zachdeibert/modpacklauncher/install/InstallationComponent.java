package com.gitlab.zachdeibert.modpacklauncher.install;

import com.gitlab.zachdeibert.modpacklauncher.InstallConfiguration;
import com.gitlab.zachdeibert.modpacklauncher.RuntimeConfiguration;
import com.gitlab.zachdeibert.modpacklauncher.SystemConfiguration;

public interface InstallationComponent {
    public static class ConstructorArguments implements Cloneable {
        public InstallConfiguration install;
        public RuntimeConfiguration runtime;
        public SystemConfiguration  system;
        public ProgressHandler      progress;
        
        @Override
        public Object clone() {
            try {
                return super.clone();
            } catch ( final CloneNotSupportedException ex ) {
                ex.printStackTrace();
                return new ConstructorArguments();
            }
        }
    }
    
    void install() throws Exception;
}
