package com.gitlab.zachdeibert.modpacklauncher.install.components.launcher;

import java.io.File;

abstract class Relauncher {
    public static void main(final String args[]) throws Throwable {
        Thread.sleep(1000);
        final File from = new File(args[0]);
        final File to = new File(args[1]);
        from.renameTo(to);
        final String cmd[] = new String[] {
            "java",
            "-jar",
            to.getAbsolutePath()
        };
        final Runtime rt = Runtime.getRuntime();
        rt.exec(cmd);
    }
}
