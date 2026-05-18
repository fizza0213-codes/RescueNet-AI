package main;

import gui.LoginFrame;
import realtime.RealTimeDataEngine;

import javax.swing.*;
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}

        UIManager.put("ToolTip.background", new java.awt.Color(40, 40, 60));
        UIManager.put("ToolTip.foreground", java.awt.Color.WHITE);
        UIManager.put("ToolTip.font",       new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 11));

        // ── Global uncaught exception handler ─────────────────────────────────
        Thread.setDefaultUncaughtExceptionHandler((t, ex) -> {
            ex.printStackTrace();
            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(null,
                            "An unexpected error occurred:\n" + ex.getMessage()
                                    + "\n\nPlease check the console for details.",
                            "RescueNet AI — Error", JOptionPane.ERROR_MESSAGE));
        });

        // ── Shutdown hook: stop real-time engine ──────────────────────────────
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[RescueNet] Shutdown hook — stopping real-time engine...");
            RealTimeDataEngine.getInstance().stop();
        }));

        // ── Launch ────────────────────────────────────────────────────────────
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}
