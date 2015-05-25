package com.gitlab.zachdeibert.modpacklauncher.install;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.gitlab.zachdeibert.modpacklauncher.StreamUtils;

public class Hash {
    public final String small;
    public final String full;
    
    public Hash(final byte data[]) throws NoSuchAlgorithmException {
        final MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        sha1.reset();
        sha1.update(data);
        final byte hash[] = sha1.digest();
        final StringBuilder str = new StringBuilder();
        for ( final byte b : hash ) {
            str.append(String.format("%02d", b));
        }
        full = str.toString();
        small = String.format("%02d", hash[0]);
    }
    
    public Hash(final String data, final String encoding) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        this(data.getBytes(encoding));
    }
    
    public Hash(final String data) throws NoSuchAlgorithmException {
        this(data.getBytes());
    }
    
    public Hash(final InputStream data) throws IOException, NoSuchAlgorithmException {
        this(StreamUtils.readAll(data));
    }
    
    public Hash(final File data) throws IOException, NoSuchAlgorithmException {
        this(new FileInputStream(data));
    }
}
