package com.gitlab.zachdeibert.modpacklauncher.scripts;

import groovy.util.ResourceConnector;
import groovy.util.ResourceException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class ClassLoaderResourceConnector implements ResourceConnector {
    private static final URL src = ClassLoaderResourceConnector.class.getProtectionDomain().getCodeSource().getLocation();
    private final ClassLoader cl;
    
    @Override
    public URLConnection getResourceConnection(final String name) throws ResourceException {
        try {
            return new URLConnection(new URL(src, name)) {
                private InputStream stream;
                
                @Override
                public void connect() throws IOException {
                    stream = cl.getResourceAsStream(name);
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    if ( stream == null ) {
                        connect();
                    }
                    return stream;
                }
            };
        } catch ( final MalformedURLException ex ) {
            throw new ResourceException(ex);
        }
    }
    
    public ClassLoaderResourceConnector(final ClassLoader cl) {
        this.cl = cl;
    }
    
    public ClassLoaderResourceConnector() {
        this(ClassLoader.getSystemClassLoader());
    }
}
