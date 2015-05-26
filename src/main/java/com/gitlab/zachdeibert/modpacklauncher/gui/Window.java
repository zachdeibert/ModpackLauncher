package com.gitlab.zachdeibert.modpacklauncher.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import com.gitlab.zachdeibert.modpacklauncher.Authenticator;
import com.gitlab.zachdeibert.modpacklauncher.InstallConfiguration;
import com.gitlab.zachdeibert.modpacklauncher.Launcher;
import com.gitlab.zachdeibert.modpacklauncher.RuntimeConfiguration;
import com.gitlab.zachdeibert.modpacklauncher.SystemConfiguration;
import com.gitlab.zachdeibert.modpacklauncher.install.InstallationComponent.ConstructorArguments;
import com.gitlab.zachdeibert.modpacklauncher.install.Installer;
import com.gitlab.zachdeibert.modpacklauncher.install.ProgressHandler;

public class Window extends JFrame {
    private static final long          serialVersionUID = -7683156597811406535L;
    private final JPanel               contentPanel;
    private final JPanel               promptsPanel;
    private final JPanel               loginPromptPanel;
    private final JPanel               memoryPromptPanel;
    private final JPanel               promptMarginPanel;
    private final JPanel               buttonPanel;
    private final JPanel               loginButtonPanel;
    private final JPanel               gameButtonPanel;
    private final JPanel               loadingPanel;
    private final JLabel               label;
    private final JLabel               loadingLabel;
    private final TextField            username;
    private final TextField            password;
    private final TextField            maxMemory;
    private final TextField            minMemory;
    private final JProgressBar         loadingBar;
    private final JButton              loginButton;
    private final JButton              logoutButton;
    private final JButton              installButton;
    private final JButton              uninstallButton;
    private final JButton              playButton;
    private final RuntimeConfiguration runtime;
    
    @Override
    public void setName(final String name) {
        final String title;
        if ( name == null || name.isEmpty() ) {
            title = "Modpack Installer";
        } else {
            title = String.format("%s Modpack Installer", name);
        }
        label.setText(title);
        final Dimension dim = username.getPreferredSize();
        dim.width = label.getPreferredSize().width / 2;
        username.setPreferredSize(dim);
        password.setPreferredSize(dim);
        maxMemory.setPreferredSize(dim);
        minMemory.setPreferredSize(dim);
        setTitle(title);
        pack();
        repaint();
    }
    
    public void onLoadingFinished() {
        username.setText(runtime.name);
        final char pass[] = new char[runtime.pass];
        Arrays.fill(pass, '*');
        password.setText(new String(pass));
        maxMemory.setText(runtime.memMax);
        minMemory.setText(runtime.memMin);
        setContentPane(contentPanel);
        pack();
        repaint();
    }
    
    public Window(final SystemConfiguration system, final InstallConfiguration install, final RuntimeConfiguration runtime) {
        this.runtime = runtime;
        final ConstructorArguments args = new ConstructorArguments();
        args.system = system;
        args.install = install;
        args.runtime = runtime;
        args.progress = ProgressHandler.NULL;
        final Installer instlr = new Installer(args);
        final Launcher launcher = new Launcher(system, runtime);
        // Initialization
        contentPanel = new JPanel();
        promptsPanel = new JPanel();
        loginPromptPanel = new JPanel();
        memoryPromptPanel = new JPanel();
        promptMarginPanel = new JPanel();
        buttonPanel = new JPanel();
        loginButtonPanel = new JPanel();
        gameButtonPanel = new JPanel();
        loadingPanel = new JPanel();
        label = new JLabel();
        loadingLabel = new JLabel();
        username = new TextField();
        maxMemory = new TextField();
        minMemory = new TextField();
        password = new TextField();
        loadingBar = new JProgressBar() {
            private static final long serialVersionUID = -2797584597183457506L;
            
            @Override
            public void setValue(final int n) {
                if ( n > getMaximum() ) {
                    onLoadingFinished();
                }
                super.setValue(n);
            }
        };
        loginButton = new JButton();
        logoutButton = new JButton();
        installButton = new JButton();
        uninstallButton = new JButton();
        playButton = new JButton();
        // Configuration
        {
            promptMarginPanel.setMaximumSize(new Dimension(1, 1));
        }
        {
            final Font old = label.getFont();
            label.setFont(old.deriveFont(3.0f * old.getSize2D()));
        }
        {
            final Font old = loadingLabel.getFont();
            loadingLabel.setText("Loading...");
            loadingLabel.setFont(old.deriveFont(4.0f * old.getSize2D()));
        }
        {
            final Dimension size = username.getPreferredSize();
            size.width = 120;
            username.setToolTipText("Username");
            username.setPlaceholder("Username");
            username.setPreferredSize(size);
        }
        {
            final Dimension size = maxMemory.getPreferredSize();
            size.width = 120;
            maxMemory.setToolTipText("Maximum Memory");
            maxMemory.setPlaceholder("Maximum Memory");
            maxMemory.setPreferredSize(size);
            maxMemory.addCaretListener((CaretEvent) -> runtime.memMax = maxMemory.getText());
        }
        {
            final Dimension size = minMemory.getPreferredSize();
            size.width = 120;
            minMemory.setToolTipText("Minimum Memory");
            minMemory.setPlaceholder("Minimum Memory");
            minMemory.setPreferredSize(size);
            minMemory.addCaretListener((CaretEvent) -> runtime.memMin = minMemory.getText());
        }
        {
            final Dimension size = password.getPreferredSize();
            size.width = 120;
            password.setToolTipText("Password");
            password.setPlaceholder("Password");
            password.setObfuscated(true);
            password.setPreferredSize(size);
        }
        {
            loadingBar.setIndeterminate(true);
            loadingBar.setPreferredSize(new Dimension(120, 32));
        }
        {
            loginButton.setText("Login");
            loginButton.addActionListener((ActionEvent) -> new Authenticator(runtime).login(username.getText(), password.getText()));
        }
        {
            logoutButton.setText("Logout");
            logoutButton.addActionListener((ActionEvent) -> new Authenticator(runtime).logout());
        }
        {
            installButton.setText("Install");
            installButton.addActionListener((ActionEvent) -> {
                loadingLabel.setText("Installing...");
                loadingBar.setIndeterminate(false);
                setContentPane(loadingPanel);
                pack();
                repaint();
                instlr.install(loadingBar);
            });
        }
        {
            uninstallButton.setText("Uninstall");
            uninstallButton.addActionListener((ActionEvent) -> instlr.uninstall());
        }
        {
            playButton.setText("Play");
            playButton.addActionListener((ActionEvent) -> launcher.launch(this));
        }
        // Formatting
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(label, BorderLayout.PAGE_START);
        contentPanel.add(promptsPanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.PAGE_END);
        promptsPanel.setLayout(new BorderLayout());
        promptsPanel.add(loginPromptPanel, BorderLayout.LINE_START);
        promptsPanel.add(promptMarginPanel, BorderLayout.CENTER);
        promptsPanel.add(memoryPromptPanel, BorderLayout.LINE_END);
        loginPromptPanel.setLayout(new BorderLayout());
        loginPromptPanel.add(username, BorderLayout.PAGE_START);
        loginPromptPanel.add(password, BorderLayout.PAGE_END);
        memoryPromptPanel.setLayout(new BorderLayout());
        memoryPromptPanel.add(maxMemory, BorderLayout.PAGE_START);
        memoryPromptPanel.add(minMemory, BorderLayout.PAGE_END);
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(loginButtonPanel, BorderLayout.PAGE_START);
        buttonPanel.add(gameButtonPanel, BorderLayout.PAGE_END);
        loginButtonPanel.setLayout(new BorderLayout());
        loginButtonPanel.add(loginButton, BorderLayout.LINE_START);
        loginButtonPanel.add(logoutButton, BorderLayout.LINE_END);
        gameButtonPanel.setLayout(new BorderLayout());
        gameButtonPanel.add(installButton, BorderLayout.LINE_START);
        gameButtonPanel.add(playButton, BorderLayout.CENTER);
        gameButtonPanel.add(uninstallButton, BorderLayout.LINE_END);
        loadingPanel.setLayout(new BorderLayout());
        loadingPanel.add(loadingLabel, BorderLayout.PAGE_START);
        loadingPanel.add(loadingBar, BorderLayout.PAGE_END);
        // Window Configuration
        setContentPane(loadingPanel);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setName(null);
        pack();
    }
}
