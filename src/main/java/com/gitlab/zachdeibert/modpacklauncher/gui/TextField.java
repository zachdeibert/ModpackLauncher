package com.gitlab.zachdeibert.modpacklauncher.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Arrays;
import javax.swing.JTextField;

public class TextField extends JTextField {
    private static final long serialVersionUID = -8483680006816933243L;
    private static final char OBFUSCATED_CHAR = '*';
    private String            placeholder;
    private Color             color;
    private boolean           obfuscated;
    
    public String getPlaceholder() {
        return placeholder;
    }
    
    public void setPlaceholder(final String text) {
        placeholder = text;
    }
    
    public Color getPlaceholderColor() {
        return color;
    }
    
    public void setPlaceholderColor(final Color color) {
        this.color = color;
    }
    
    public boolean getObfuscated() {
        return obfuscated;
    }
    
    public void setObfuscated(final boolean obfuscated) {
        this.obfuscated = obfuscated;
    }
    
    @Override
    public void paintComponent(final Graphics g) {
        final Color old = getForeground();
        final String text = getText();
        final boolean empty = text.isEmpty() && placeholder != null && !placeholder.isEmpty();
        if ( empty ) {
            setForeground(color);
            setText(placeholder);
            setCaretPosition(0);
        } else if ( obfuscated ) {
            final char obf[] = new char[text.length()];
            Arrays.fill(obf, OBFUSCATED_CHAR);
            setText(new String(obf));
        }
        super.paintComponent(g);
        if ( empty ) {
            setForeground(old);
            setText("");
        } else if ( obfuscated ) {
            setText(text);
        }
    }
    
    public TextField() {
        color = getForeground().brighter().brighter();
    }
}
