package com.gitlab.zachdeibert.modpacklauncher;

import java.lang.reflect.Method;
import java.net.Proxy;
import java.util.Random;
import javax.swing.JOptionPane;
import com.mojang.authlib.Agent;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

public class Authenticator {
    private final YggdrasilAuthenticationService authService;
    private final UserAuthentication             userAuth;
    private final RuntimeConfiguration           runtime;
    
    private static char randHex(final Random rand) {
        int val = rand.nextInt(16);
        return (char) (val < 10 ? '0' + val : 'a' + val - 10);
    }
    
    private static void randHexStr(final Random rand, final StringBuilder str, final int size) {
        for ( int i = 0; i < size; i++ ) {
            str.append(randHex(rand));
        }
    }
    
    private static String randomUUID(final Random rand) {
        final StringBuilder UUID = new StringBuilder();
        randHexStr(rand, UUID, 8);
        UUID.append('-');
        randHexStr(rand, UUID, 4);
        UUID.append('-');
        randHexStr(rand, UUID, 4);
        UUID.append('-');
        randHexStr(rand, UUID, 4);
        UUID.append('-');
        randHexStr(rand, UUID, 8);
        return UUID.toString();
    }
    
    private boolean authenticate(final String pass) {
        userAuth.setUsername(runtime.name);
        userAuth.setPassword(pass);
        if ( userAuth.canLogIn() ) {
            try {
                userAuth.logIn();
            } catch ( final AuthenticationException ex ) {
                ex.printStackTrace();
                return false;
            }
            runtime.token = userAuth.getAuthenticatedToken();
            runtime.client = authService.getClientToken();
            runtime.UUID = userAuth.getSelectedProfile().getId().toString();
            runtime.authData = userAuth.saveForStorage();
            return true;
        } else {
            return false;
        }
    }
    
    private void invalidate() {
        userAuth.logOut();
        runtime.token = null;
        runtime.UUID = null;
        runtime.name = null;
        runtime.pass = 0;
        runtime.authData = userAuth.saveForStorage();
    }
    
    private boolean refresh() {
        if ( runtime.token == null || runtime.UUID == null || runtime.name == null || runtime.authData == null ) {
            return false;
        }
        try {
            userAuth.logIn();
            return true;
        } catch ( final AuthenticationException ex ) {
            ex.printStackTrace();
            return false;
        }
    }
    
    private boolean validate() {
        if ( runtime.token == null || runtime.UUID == null || runtime.name == null || runtime.authData == null ) {
            return false;
        }
        try {
            final Class<?> cls = userAuth.getClass();
            final Method checkTokenValidity = cls.getDeclaredMethod("checkTokenValidity");
            return (boolean) checkTokenValidity.invoke(userAuth);
        } catch ( final Exception ex ) {
            ex.printStackTrace();
            return false;
        }
    }
    
    public void logout() {
        if ( runtime.name == null || runtime.name.isEmpty() ) {
            JOptionPane.showMessageDialog(null, "You are not logged in.", "Logging Out User", JOptionPane.ERROR_MESSAGE);
            return;
        }
        invalidate();
    }
    
    public void login(final String username, final String password) {
        if ( username == null || username.isEmpty() || password == null || password.isEmpty() ) {
            JOptionPane.showMessageDialog(null, "Invalid Credentials.", "Authenticating User", JOptionPane.ERROR_MESSAGE);
            return;
        }
        runtime.name = username;
        runtime.pass = password.length();
        if ( !authenticate(password) ) {
            JOptionPane.showMessageDialog(null, "Invalid Credentials.", "Authenticating User", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }
    
    public Authenticator(final RuntimeConfiguration runtime) {
        if ( runtime.client == null || runtime.client.isEmpty() ) {
            runtime.client = randomUUID(new Random());
        }
        authService = new YggdrasilAuthenticationService(Proxy.NO_PROXY, runtime.client);
        userAuth = authService.createUserAuthentication(Agent.MINECRAFT);
        if ( runtime.authData != null ) {
            userAuth.loadFromStorage(runtime.authData);
        }
        this.runtime = runtime;
        if ( !validate() ) {
            refresh();
        }
    }
}
