package main;

import gui.LoginFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Use system look and feel for better fonts on all platforms
        try {
            UIManager.setLookAndFeel(String.valueOf("javax.swing.plaf.nimbus.NimbusLookAndFeel"));
        } catch (Exception ignored) {}

        // Set global tooltip style
        UIManager.put("ToolTip.background", new java.awt.Color(40, 40, 60));
        UIManager.put("ToolTip.foreground", java.awt.Color.WHITE);

        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}
