package com.gitlab.zachdeibert.modpacklauncher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public abstract class StreamUtils {
    private static class Buffer {
        public final int  length;
        public final byte bytes[];
        
        public Buffer(int size) {
            length = size;
            bytes = new byte[size];
        }
    }
    public static class OutputRedirThread extends Thread {
        private final InputStream  in;
        private final OutputStream out;
        
        @Override
        public void run() {
            try {
                final byte buffer[] = new byte[4096];
                for ( int len = in.read(buffer, 0, 4096); len > 0; len = in.read(buffer, 0, 4096) ) {
                    out.write(buffer, 0, len);
                }
            } catch ( final Exception ex ) {
                ex.printStackTrace();
            }
        }
        
        public OutputRedirThread(final InputStream in, final OutputStream out) {
            this.in = in;
            this.out = out;
        }
    }

    public static byte[] readAll(final InputStream stream) throws IOException {
        final List<Buffer> buffers = new LinkedList<Buffer>();
        final byte tmp[] = new byte[4096];
        for ( int len = stream.read(tmp, 0, 4096); len > 0; len = stream.read(tmp, 0, 4096) ) {
            final Buffer buffer = new Buffer(len);
            System.arraycopy(tmp, 0, buffer.bytes, 0, len);
            buffers.add(buffer);
        }
        int length = 0;
        for ( final Buffer buffer : buffers ) {
            length += buffer.length;
        }
        final byte bigBuffer[] = new byte[length];
        int i = 0;
        for ( final Buffer buffer : buffers ) {
            System.arraycopy(buffer.bytes, 0, bigBuffer, i, buffer.length);
            i += buffer.length;
        }
        return bigBuffer;
    }
    
    public static void copy(final InputStream in, final OutputStream out) throws IOException {
        final byte buffer[] = new byte[4096];
        for ( int len = in.read(buffer, 0, 4096); len > 0; len = in.read(buffer, 0, 4096) ) {
            out.write(buffer, 0, len);
        }
    }
    
    public static void copyAndClose(final InputStream in, final OutputStream out) throws IOException {
        copy(in, out);
        in.close();
        out.close();
    }
    
    public static InputStream download(final String urlS) throws IOException {
        if ( urlS.indexOf(':') >= 0 ) {
            final URL url = new URL(urlS);
            final URLConnection conn = url.openConnection();
            return conn.getInputStream();
        } else {
            return ClassLoader.getSystemResourceAsStream(urlS);
        }
    }
    
    public static void mergeJars(final ZipOutputStream out, final Predicate<? super ZipEntry> filter, final ZipInputStream... ins) throws IOException {
        final List<String> files = new LinkedList<String>();
        ZipEntry entry;
        for ( final ZipInputStream in : ins ) {
            while ( (entry = in.getNextEntry()) != null ) {
                final String name = entry.getName();
                if ( !files.contains(name) && filter.test(entry) ) {
                    out.putNextEntry(entry);
                    files.add(name);
                    if ( !entry.isDirectory() ) {
                        copy(in, out);
                    }
                }
            }
            if ( in instanceof JarInputStream ) {
                entry = new ZipEntry(JarFile.MANIFEST_NAME);
                if ( !files.contains(JarFile.MANIFEST_NAME) && filter.test(entry) ) {
                    out.putNextEntry(entry);
                    files.add(JarFile.MANIFEST_NAME);
                    ((JarInputStream) in).getManifest().write(out);
                }
            }
        }
    }
    
    public static void mergeJars(final ZipOutputStream out, final ZipInputStream... ins) throws IOException {
        mergeJars(out, new Predicate<ZipEntry>() {
            @Override
            public boolean test(final ZipEntry t) {
                return true;
            }
        }, ins);
    }
}
