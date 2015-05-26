package com.gitlab.zachdeibert.modpacklauncher.install.components.game;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
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
        if ( !gotHash.full.equals(expectedHash) ) {
            file.delete();
            throw new StreamCorruptedException("Hashes do not match!");
        }
    }
    
    protected void installAssets(final File dir) throws IOException, NoSuchAlgorithmException {
        final File indexesDir = Directory.INDEX_DIR.getFile(dir);
        final File objectDir = Directory.OBJECT_DIR.getFile(dir);
        final File indexFile = new File(indexesDir, args.system.version.concat(".json"));
        installAssetIndex(indexFile, String.format("https://s3.amazonaws.com/Minecraft.Download/indexes/%s.json", args.system.version));
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
    
    @Override
    public void install() throws Exception {
        args.progress.setSteps(1);
        installAssets(Directory.ASSET_DIR.getFile(args));
        args.progress.stepForward();
    }
    
    public AssetInstaller(final ConstructorArguments args) {
        this.args = args;
    }
}
