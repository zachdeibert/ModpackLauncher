package com.gitlab.zachdeibert.modpacklauncher.install;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import com.gitlab.zachdeibert.modpacklauncher.InstallConfiguration;
import com.gitlab.zachdeibert.modpacklauncher.StreamUtils;
import com.gitlab.zachdeibert.modpacklauncher.SystemConfiguration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Installer {
    public static class Index {
        public Map<String, Resource> objects;
    }
    public static class Resource {
        public String hash;
        public int    size;
    }
    
    private static final String        ARCH = System.getProperty("sun.arch.data.model");
    private final SystemConfiguration  system;
    private final InstallConfiguration install;
    
    private static void delete(final File dir) {
        if ( dir.isDirectory() ) {
            for ( final File sub : dir.listFiles() ) {
                delete(sub);
            }
        }
        dir.delete();
    }
    
    private boolean useOldAssetFormat() {
        return system.version.matches("1(\\.[0-6](\\..+)?)?");
    }
    
    private int getSteps() {
        int steps = 1;
        steps += install.jarMods.size();
        steps += install.libs.size();
        steps += install.coreMods.size();
        steps += install.loaderMods.size();
        steps += install.configs.size();
        return steps;
    }
    
    private File getMCJar() {
        return new File(system.installDir, "minecraft.jar");
    }
    
    protected InputStream download(final String urlS) throws IOException {
        if ( urlS.indexOf(':') >= 0 ) {
            final URL url = new URL(urlS);
            final URLConnection conn = url.openConnection();
            return conn.getInputStream();
        } else {
            return ClassLoader.getSystemResourceAsStream(urlS);
        }
    }
    
    private String filename(final String url) {
        final int index = url.lastIndexOf('/');
        if ( index >= 0 ) {
            return url.substring(index);
        }
        return url;
    }
    
    public boolean isInstalled() {
        return getMCJar().exists();
    }
    
    public void uninstall() {
        if ( !isInstalled() ) {
            JOptionPane.showMessageDialog(null, "The modpack is not installed.", "Uninstalling Modpack", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ( JOptionPane.showConfirmDialog(null, String.format("Are you sure you want to delete this modpack forever (a long time)?\nAll files in %s will be deleted", system.installDir), "Uninstalling Modpack", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION ) {
            delete(system.installDir);
        }
    }
    
    protected void installJarMod(final JarOutputStream jarFile, final List<String> files, final String url) throws IOException {
        final InputStream in = download(url);
        final ZipInputStream zip = new ZipInputStream(in);
        ZipEntry entry;
        while ( (entry = zip.getNextEntry()) != null ) {
            final String name = entry.getName();
            if ( ! (files.contains(name) || name.matches("META-INF/MOJANG.*")) ) {
                files.add(name);
                jarFile.putNextEntry(entry);
                if ( !entry.isDirectory() ) {
                    StreamUtils.copy(zip, jarFile);
                }
            }
        }
        zip.close();
        in.close();
    }
    
    protected void extractNatives(final File nativeDir, final String fileName, final File file) throws FileNotFoundException, IOException {
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
    }
    
    protected void installLibrary(final File dir, final File nativeDir, final String artifact, final String fileName, final File file) throws IOException, FileNotFoundException {
        file.getParentFile().mkdirs();
        IOException ex = null;
        for ( final String repo : install.libRepos ) {
            final String url = String.format("%s%s%s", repo, repo.endsWith("/") ? "" : "/", fileName);
            try {
                StreamUtils.copyAndClose(download(url), new FileOutputStream(file));
                break;
            } catch ( final IOException e ) {
                ex = e;
            }
        }
        if ( !file.exists() && ex != null ) {
            throw ex;
        }
    }
    
    protected void installLibraryAndNatives(final File dir, final File nativeDir, final String artifact) throws IOException, FileNotFoundException {
        final String parts[] = artifact.split(":");
        final String fileName = String.format("%s/%s/%s/%2$s-%3$s%s.jar", parts[0].replace('.', '/'), parts[1], parts[2], parts.length < 4 ? "" : "-".concat(parts[3]));
        final File file = new File(dir, fileName);
        installLibrary(dir, nativeDir, artifact, fileName, file);
        if ( fileName.indexOf("native") >= 0 && ! ( (fileName.indexOf("64") >= 0 && !ARCH.equals("32")) || (fileName.indexOf("32") >= 0 && !ARCH.equals("64"))) ) {
            extractNatives(nativeDir, fileName, file);
        }
    }
    
    protected void installAssetIndex(final File file, final String url) throws IOException {
        file.getParentFile().mkdirs();
        StreamUtils.copyAndClose(download(url), new FileOutputStream(file));
    }
    
    protected void installAsset(final File file, final String url, final String expectedHash) throws IOException, NoSuchAlgorithmException {
        file.getParentFile().mkdirs();
        StreamUtils.copyAndClose(download(url), new FileOutputStream(file));
        final Hash gotHash = new Hash(file);
        if ( !gotHash.full.equals(expectedHash) ) {
            file.delete();
            throw new StreamCorruptedException("Hashes do not match!");
        }
    }
    
    protected void installAssets(final File dir) throws IOException, NoSuchAlgorithmException {
        final File indexesDir = new File(dir, "indexes");
        final File objectDir = new File(dir, "objects");
        final File indexFile = new File(indexesDir, system.version.concat(".json"));
        installAssetIndex(indexFile, String.format("https://s3.amazonaws.com/Minecraft.Download/indexes/%s.json", system.version));
        final Gson gson = new GsonBuilder().create();
        final Index index = gson.fromJson(new FileReader(indexFile), Index.class);
        final boolean old = useOldAssetFormat();
        for ( final String url : index.objects.keySet() ) {
            final Resource res = index.objects.get(url);
            final String hashed = String.format("%s/%s", res.hash.substring(0, 2), res.hash);
            final File out = new File(objectDir, old ? url : hashed);
            installAsset(out, String.format("http://resources.download.minecraft.net/%s", hashed), res.hash);
        }
    }
    
    protected void installMod(final File dir, final String url) throws IOException, FileNotFoundException {
        StreamUtils.copyAndClose(download(url), new FileOutputStream(new File(dir, filename(url))));
    }
    
    protected void installConfig(final File dir, final String url) throws IOException, FileNotFoundException {
        final InputStream in = download(url);
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

    protected int installJarMods(final JProgressBar bar, int step) throws FileNotFoundException, IOException {
        System.out.println("Installing Minecraft Jar");
        if ( !install.jarMods.isEmpty() ) {
            final File jar = getMCJar();
            jar.getParentFile().mkdirs();
            final FileOutputStream jarOut = new FileOutputStream(jar);
            final JarOutputStream jarFile = new JarOutputStream(jarOut);
            final List<String> files = new LinkedList<String>();
            Collections.reverse(install.jarMods);
            try {
                for ( final String url : install.jarMods ) {
                    installJarMod(jarFile, files, url);
                    bar.setValue(++step);
                    bar.repaint();
                }
            } finally {
                Collections.reverse(install.jarMods);
            }
            jarFile.close();
            jarOut.close();
        }
        return step;
    }

    protected int installNatives(final JProgressBar bar, int step) throws IOException, FileNotFoundException {
        System.out.println("Installing Libraries");
        if ( !install.libs.isEmpty() ) {
            final File dir = new File(system.installDir, "libraries");
            final File nativeDir = new File(system.installDir, "natives");
            dir.mkdirs();
            for ( final String artifact : install.libs ) {
                installLibraryAndNatives(dir, nativeDir, artifact);
                bar.setValue(++step);
                bar.repaint();
            }
        }
        return step;
    }

    protected int installAssets(final JProgressBar bar, int step) throws IOException, NoSuchAlgorithmException, FileNotFoundException {
        System.out.println("Installing Assets");
        installAssets(new File(system.installDir, "assets"));
        bar.setValue(++step);
        bar.repaint();
        System.out.println("Installing Core Mods");
        if ( !install.coreMods.isEmpty() ) {
            final File dir = new File(system.installDir, "coremods");
            dir.mkdirs();
            for ( final String url : install.coreMods ) {
                installMod(dir, url);
                bar.setValue(++step);
                bar.repaint();
            }
        }
        return step;
    }

    protected int installCoreMods(final JProgressBar bar, int step) throws IOException, FileNotFoundException {
        System.out.println("Installing Core Mods");
        if ( !install.coreMods.isEmpty() ) {
            final File dir = new File(system.installDir, "coremods");
            dir.mkdirs();
            for ( final String url : install.coreMods ) {
                installMod(dir, url);
                bar.setValue(++step);
                bar.repaint();
            }
        }
        return step;
    }

    protected int installMods(final JProgressBar bar, int step) throws IOException, FileNotFoundException {
        System.out.println("Installing Mods");
        if ( !install.loaderMods.isEmpty() ) {
            final File dir = new File(system.installDir, "mods");
            dir.mkdirs();
            for ( final String url : install.loaderMods ) {
                installMod(dir, url);
                bar.setValue(++step);
                bar.repaint();
            }
        }
        return step;
    }

    protected int installConfigs(final JProgressBar bar, int step) throws IOException, FileNotFoundException {
        System.out.println("Installing Configuration");
        if ( !install.configs.isEmpty() ) {
            final File dir = new File(system.installDir, "config");
            for ( final String url : install.configs ) {
                installConfig(dir, url);
                bar.setValue(++step);
                bar.repaint();
            }
        }
        return step;
    }
    
    public void install(final JProgressBar bar) {
        new Thread(() -> {
            if ( isInstalled() ) {
                JOptionPane.showMessageDialog(null, "The modpack is already installed.", "Installing Modpack", JOptionPane.ERROR_MESSAGE);
                bar.setValue(bar.getMaximum());
                return;
            }
            bar.setMaximum(0);
            bar.setMaximum(getSteps());
            bar.setValue(0);
            bar.repaint();
            int step = 0;
            try {
                step = installJarMods(bar, step);
                step = installNatives(bar, step);
                step = installAssets(bar, step);
                step = installCoreMods(bar, step);
                step = installMods(bar, step);
                step = installConfigs(bar, step);
                System.out.println("Done Installing!");
            } catch ( final Exception ex ) {
                ex.printStackTrace();
                Component top = bar;
                Component tmp;
                while ( (tmp = top.getParent()) != null ) {
                    top = tmp;
                }
                top.setVisible(false);
                JOptionPane.showMessageDialog(null, "Installation Failure.", "Installing Modpack", JOptionPane.ERROR_MESSAGE);
                top.setVisible(true);
                bar.setValue(bar.getMaximum());
            }
        }).start();
    }
    
    public Installer(final SystemConfiguration system, final InstallConfiguration install) {
        this.install = install;
        this.system = system;
    }
}
