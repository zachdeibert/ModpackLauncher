package com.gitlab.zachdeibert.modpacklauncher.install.components.game;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import com.gitlab.zachdeibert.modpacklauncher.StreamUtils;
import com.gitlab.zachdeibert.modpacklauncher.install.Directory;
import com.gitlab.zachdeibert.modpacklauncher.install.Hash;
import com.gitlab.zachdeibert.modpacklauncher.install.InstallationComponent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AssetInstaller implements InstallationComponent {
    public static class Index {
        public Map<String, Resource> objects;
    }
    public static class Resource {
        public String hash;
        public int    size;
    }
    private class DownloadThread extends Thread {
        private class Entry<K, V> implements Map.Entry<K, V> {
            private final K key;
            private V       value;
            
            @Override
            public K getKey() {
                return key;
            }
            
            @Override
            public V getValue() {
                return value;
            }
            
            @Override
            public V setValue(final V value) {
                final V old = this.value;
                this.value = value;
                return old;
            }
            
            public Entry(final K key, final V value) {
                this.key = key;
                this.value = value;
            }
        }
        private final Map<String, Resource>       map;
        private final File                        objectDir;
        private final boolean                     old;
        private final Consumer<? super Exception> onError;
        
        private Entry<String, Resource> next() {
            final Entry<String, Resource> pair;
            synchronized( map ) {
                if ( map.size() > 0 ) {
                    final String key = map.keySet().iterator().next();
                    pair = new Entry<String, Resource>(key, map.get(key));
                    map.remove(key);
                    args.progress.stepForward();
                } else {
                    pair = null;
                }
            }
            return pair;
        }
        
        @Override
        public void run() {
            try {
                Entry<String, Resource> entry;
                while ( (entry = next()) != null ) {
                    final String hashed = String.format("%s/%s", entry.value.hash.substring(0, 2), entry.value.hash);
                    final File out = new File(objectDir, old ? entry.key : hashed);
                    installAsset(out, String.format("http://resources.download.minecraft.net/%s", hashed), entry.value.hash);
                    Thread.sleep(0);
                }
            } catch ( final RuntimeException ex ) {
                throw ex;
            } catch ( final InterruptedException ex ) {} catch ( final Exception ex ) {
                onError.accept(ex);
            }
        }
        
        public DownloadThread(final Map<String, Resource> map, final File objectDir, final boolean old, final Consumer<? super Exception> onError) {
            this.map = map;
            this.objectDir = objectDir;
            this.old = old;
            this.onError = onError;
        }
    }
    private static final int           DOWNLOAD_THREADS = 8;
    private final ConstructorArguments args;
    
    protected boolean useOldAssetFormat() {
        return args.system.version.matches("1(\\.[0-6](\\..+)?)?");
    }
    
    protected void installAssetIndex(final File file, final String url) throws IOException {
        file.getParentFile().mkdirs();
        StreamUtils.copyAndClose(StreamUtils.download(url), new FileOutputStream(file));
    }
    
    protected void installAsset(final File file, final String url, final String expectedHash) throws IOException, NoSuchAlgorithmException {
        file.getParentFile().mkdirs();
        StreamUtils.copyAndClose(StreamUtils.download(url), new FileOutputStream(file));
        final Hash gotHash = new Hash(file);
        if ( !gotHash.full.equalsIgnoreCase(expectedHash) ) {
            file.delete();
            throw new StreamCorruptedException("Hashes do not match!");
        }
    }
    
    @Override
    public void install() throws Exception {
        final File dir = Directory.GAME_DIR.getFile(args);
        args.progress.setSteps(1);
        final File indexesDir = Directory.INDEX_DIR.getFile(dir);
        final File objectDir = Directory.OBJECT_DIR.getFile(dir);
        final File indexFile = new File(indexesDir, args.system.version.concat(".json"));
        installAssetIndex(indexFile, String.format("https://s3.amazonaws.com/Minecraft.Download/indexes/%s.json", args.system.version));
        final Gson gson = new GsonBuilder().create();
        final Index index = gson.fromJson(new FileReader(indexFile), Index.class);
        args.progress.addSteps(index.objects.size());
        args.progress.stepForward();
        final boolean old = useOldAssetFormat();
        final Thread threads[] = new Thread[DOWNLOAD_THREADS];
        final RuntimeException rex = new RuntimeException();
        final Consumer<Exception> onError = (final Exception ex) -> {
            for ( int i = 0; i < DOWNLOAD_THREADS; i++ ) {
                threads[i].interrupt();
            }
            rex.initCause(ex);
        };
        final Map<String, Resource> syncObjects = Collections.synchronizedMap(index.objects);
        for ( int i = 0; i < DOWNLOAD_THREADS; i++ ) {
            threads[i] = new DownloadThread(syncObjects, objectDir, old, onError);
            threads[i].start();
        }
        for ( int i = 0; i < DOWNLOAD_THREADS; i++ ) {
            try {
                threads[i].join();
            } catch ( final InterruptedException ex ) {
                ex.printStackTrace();
            }
        }
        if ( rex.getCause() != null ) {
            throw rex;
        }
    }
    
    public AssetInstaller(final ConstructorArguments args) {
        this.args = args;
    }
}
