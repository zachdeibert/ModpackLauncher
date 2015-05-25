package com.gitlab.zachdeibert.modpacklauncher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

public class RuntimeConfiguration implements Serializable {
    private static final long  serialVersionUID = -5178571638738764377L;
    public String              memMax;
    public String              memMin;
    public String              token;
    public String              client;
    public String              UUID;
    public String              name;
    public int                 pass;
    public Map<String, Object> authData;
    
    public static void save(final File file, final RuntimeConfiguration config) throws IOException {
        final FileOutputStream fos = new FileOutputStream(file);
        final ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(config);
        oos.close();
        fos.close();
    }
    
    public static RuntimeConfiguration load(final File file) throws IOException {
        if ( !file.exists() ) {
            return new RuntimeConfiguration();
        }
        final FileInputStream fis = new FileInputStream(file);
        final ObjectInputStream ois = new ObjectInputStream(fis);
        RuntimeConfiguration config;
        try {
            config = (RuntimeConfiguration) ois.readObject();
        } catch ( final ClassNotFoundException ex ) {
            ois.close();
            fis.close();
            throw new IOException(ex);
        }
        ois.close();
        fis.close();
        return config;
    }
    
    public void copy(final RuntimeConfiguration other) {
        token = other.token;
        client = other.client;
        UUID = other.UUID;
        name = other.name;
        pass = other.pass;
    }
    
    @Override
    public String toString() {
        return String.format("RuntimeConfiguration [memMax=%s, memMin=%s, token=%s, client=%s, UUID=%s, name=%s, pass=%d, authData=%s]", memMax, memMin, token, client, UUID, name, pass, authData);
    }
    
    public RuntimeConfiguration() {
        memMax = "3G";
        memMin = "1G";
    }
}
