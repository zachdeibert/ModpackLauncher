package com.gitlab.zachdeibert.modpacklauncher.install;

import java.awt.Component;
import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import com.gitlab.zachdeibert.modpacklauncher.install.InstallationComponent.ConstructorArguments;
import com.gitlab.zachdeibert.modpacklauncher.install.components.game.AssetInstaller;
import com.gitlab.zachdeibert.modpacklauncher.install.components.game.ConfigInstaller;
import com.gitlab.zachdeibert.modpacklauncher.install.components.game.CoreModInstaller;
import com.gitlab.zachdeibert.modpacklauncher.install.components.game.JarModInstaller;
import com.gitlab.zachdeibert.modpacklauncher.install.components.game.ModInstaller;
import com.gitlab.zachdeibert.modpacklauncher.install.components.game.NativesInstaller;

public class Installer {
    private static class ThrowableArray extends Throwable {
        private static final long serialVersionUID = 6363719998838049100L;
        
        public Throwable exceptions[];

        @Override
        public void printStackTrace(final PrintStream s) {
            for ( final Throwable ex : exceptions ) {
                if ( ex != null ) {
                    ex.printStackTrace(s);
                }
            }
        }

        @Override
        public void printStackTrace(final PrintWriter s) {
            for ( final Throwable ex : exceptions ) {
                if ( ex != null ) {
                    ex.printStackTrace(s);
                }
            }
        }
    }
    
    private final ConstructorArguments args;
    
    private static void delete(final File dir) {
        if ( dir.isDirectory() ) {
            for ( final File sub : dir.listFiles() ) {
                delete(sub);
            }
        }
        dir.delete();
    }
    
    private ConstructorArguments getArgs(final JProgressBar bar) {
        final ConstructorArguments args = (ConstructorArguments) this.args.clone();
        args.progress = new ProgressHandler() {
            private int total = 0;
            private int current = 0;
            
            @Override
            public void stepForward(final int steps) {
                current += steps;
                bar.setValue(bar.getValue() + steps);
            }

            @Override
            public int getSteps() {
                return total;
            }

            @Override
            public void setSteps(final int steps) {
                bar.setMaximum(bar.getMaximum() - total + steps);
                total = steps;
            }

            @Override
            public void addSteps(final int steps) {
                total += steps;
                bar.setMaximum(bar.getMaximum() + steps);;
            }

            @Override
            public void finishEarly() {
                bar.setValue(bar.getValue() - current + total);
            }
        };
        return args;
    }
    
    public boolean isInstalled() {
        return Directory.MC_JAR.getFile(args).exists();
    }
    
    public void uninstall() {
        if ( !isInstalled() ) {
            JOptionPane.showMessageDialog(null, "The modpack is not installed.", "Uninstalling Modpack", JOptionPane.ERROR_MESSAGE);
            return;
        }
        final File dir = Directory.GAME_DIR.getFile(args);
        if ( JOptionPane.showConfirmDialog(null, String.format("Are you sure you want to delete this modpack forever (a long time)?\nAll files in %s will be deleted", dir.getAbsolutePath()), "Uninstalling Modpack", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION ) {
            delete(dir);
        }
    }
    
    public void install(final JProgressBar bar) {
        new Thread(() -> {
            if ( isInstalled() ) {
                JOptionPane.showMessageDialog(null, "The modpack is already installed.", "Installing Modpack", JOptionPane.ERROR_MESSAGE);
                bar.setValue(bar.getMaximum());
                return;
            }
            bar.setMaximum(0);
            bar.setValue(0);
            bar.repaint();
            try {
                final InstallationComponent components[] = new InstallationComponent[] {
                    new JarModInstaller(getArgs(bar)),
                    new NativesInstaller(getArgs(bar)),
                    new AssetInstaller(getArgs(bar)),
                    new CoreModInstaller(getArgs(bar)),
                    new ModInstaller(getArgs(bar)),
                    new ConfigInstaller(getArgs(bar))
                };
                final Throwable exs[] = new Throwable[components.length];
                final Thread threads[] = new Thread[components.length];
                for ( int i = 0; i < components.length; i++ ) {
                    final int j = i;
                    threads[i] = new Thread(() -> {
                        try {
                            components[j].install();
                        } catch ( final Throwable ex ) {
                            exs[j] = ex;
                        }
                    });
                    threads[i].start();
                }
                boolean needsThrowing = false;
                for ( int i = 0; i < components.length; i++ ) {
                    threads[i].join();
                    if ( exs[i] != null ) {
                        needsThrowing = true;
                    }
                }
                if ( needsThrowing ) {
                    final ThrowableArray array = new ThrowableArray();
                    array.exceptions = exs;
                    throw array;
                }
            } catch ( final Throwable ex ) {
                ex.printStackTrace();
                Component top = bar;
                Component tmp;
                while ( (tmp = top.getParent()) != null ) {
                    top = tmp;
                }
                top.setVisible(false);
                JOptionPane.showMessageDialog(null, "Installation Failure.", "Installing Modpack", JOptionPane.ERROR_MESSAGE);
                top.setVisible(true);
            } finally {
                bar.setValue(bar.getMaximum() + 1);
            }
        }).start();
    }
    
    public Installer(final ConstructorArguments args) {
        this.args = args;
    }
}
