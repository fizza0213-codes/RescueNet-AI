package alerts;

import gui.UITheme;
import realtime.RealTimeDataEngine.AlertEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * RescueNet AI — Alert System
 *
 * Components:
 *  1. AlertManager  — receives AlertEvent, stores history, dispatches UI
 *  2. AlertBadge    — animated bell icon for nav bar
 *  3. AlertPanel    — slide-in toast notifications (bottom-right corner)
 *  4. AlertCenter   — full alert history window
 *  5. NotificationSimulator — simulates SMS/Email sending
 */
public class AlertManager {

    private static AlertManager instance;
    public static synchronized AlertManager getInstance() {
        if (instance == null) instance = new AlertManager();
        return instance;
    }

    // ── State ─────────────────────────────────────────────────────────────────
    private final List<AlertEvent> history = new ArrayList<>();
    private int unreadCount = 0;
    private final List<Runnable> badgeListeners = new ArrayList<>();
    private JFrame parentFrame;

    // ── Toast queue ───────────────────────────────────────────────────────────
    private final List<AlertToast> activeToasts = new ArrayList<>();
    private static final int MAX_TOASTS = 4;

    private AlertManager() {}

    // ═══════════════════════════════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════════════════════════════

    public void setParentFrame(JFrame f) { this.parentFrame = f; }

    /** Called by RealTimeDataEngine listener */
    public void receiveAlert(AlertEvent event) {
        history.add(0, event);  // prepend newest
        if (history.size() > 200) history.remove(history.size() - 1);
        unreadCount++;
        badgeListeners.forEach(Runnable::run);

        // Show toast on EDT
        SwingUtilities.invokeLater(() -> showToast(event));

        // Simulate SMS/Email for CRITICAL
        if (event.severity == AlertEvent.Severity.CRITICAL) {
            NotificationSimulator.sendSMS(event);
            NotificationSimulator.sendEmail(event);
        }
    }

    public void addBadgeListener(Runnable r) { badgeListeners.add(r); }
    public int  getUnreadCount()             { return unreadCount; }
    public void markAllRead()                { unreadCount = 0; badgeListeners.forEach(Runnable::run); }
    public List<AlertEvent> getHistory()     { return Collections.unmodifiableList(history); }

    /** Manually fire a custom alert */
    public void fireAlert(String message, AlertEvent.Severity severity) {
        AlertEvent ev = new AlertEvent(message, severity,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                "System", "Manual");
        receiveAlert(ev);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // TOAST NOTIFICATIONS
    // ═══════════════════════════════════════════════════════════════════════

    private void showToast(AlertEvent event) {
        if (parentFrame == null || !parentFrame.isVisible()) return;
        if (activeToasts.size() >= MAX_TOASTS) {
            AlertToast oldest = activeToasts.remove(0);
            oldest.dismiss();
        }
        AlertToast toast = new AlertToast(event, this::removeToast);
        activeToasts.add(toast);
        repositionToasts();
    }

    private void removeToast(AlertToast t) {
        activeToasts.remove(t);
        repositionToasts();
    }

    private void repositionToasts() {
        if (parentFrame == null) return;
        Rectangle pBounds = parentFrame.getBounds();
        int baseX = pBounds.x + pBounds.width  - AlertToast.WIDTH  - 20;
        int baseY = pBounds.y + pBounds.height  - AlertToast.HEIGHT - 50;
        for (int i = 0; i < activeToasts.size(); i++) {
            AlertToast t = activeToasts.get(i);
            t.setLocation(baseX, baseY - i * (AlertToast.HEIGHT + 8));
            t.setVisible(true);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ALERT CENTER WINDOW
    // ═══════════════════════════════════════════════════════════════════════

    public void openAlertCenter() {
        markAllRead();
        new AlertCenterFrame(history);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ALERT CENTER FRAME
    // ═══════════════════════════════════════════════════════════════════════

    public static class AlertCenterFrame extends JFrame {
        public AlertCenterFrame(List<AlertEvent> history) {
            setTitle("RescueNet AI — Alert Center");
            setSize(680, 550);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            getContentPane().setBackground(UITheme.BG_DARK);
            setLayout(new BorderLayout());

            // Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(UITheme.BG_HEADER);
            header.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, UITheme.ACCENT_RED));
            header.setPreferredSize(new Dimension(0, 50));
            JLabel title = new JLabel("  🔔  Alert Center  — Emergency Notifications");
            title.setFont(UITheme.FONT_HEADER);
            title.setForeground(Color.WHITE);
            header.add(title, BorderLayout.WEST);

            // Clear button
            JButton btnClear = UITheme.primaryButton("Clear All", new Color(120, 30, 30));
            JPanel hRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 9));
            hRight.setOpaque(false);
            hRight.add(btnClear);
            header.add(hRight, BorderLayout.EAST);

            // Alert list
            JPanel listPanel = new JPanel();
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            listPanel.setBackground(UITheme.BG_DARK);
            listPanel.setBorder(new EmptyBorder(10, 12, 10, 12));

            if (history.isEmpty()) {
                JLabel empty = new JLabel("No alerts yet. System is monitoring...");
                empty.setFont(UITheme.FONT_NORMAL);
                empty.setForeground(UITheme.TEXT_SECONDARY);
                empty.setAlignmentX(Component.LEFT_ALIGNMENT);
                listPanel.add(empty);
            } else {
                for (AlertEvent ev : history) {
                    listPanel.add(buildAlertRow(ev));
                    listPanel.add(Box.createVerticalStrut(5));
                }
            }

            JScrollPane scroll = new JScrollPane(listPanel);
            scroll.setBorder(null);
            scroll.getVerticalScrollBar().setUnitIncrement(16);

            btnClear.addActionListener(e -> {
                listPanel.removeAll();
                JLabel empty = new JLabel("All alerts cleared.");
                empty.setFont(UITheme.FONT_NORMAL);
                empty.setForeground(UITheme.TEXT_SECONDARY);
                listPanel.add(empty);
                listPanel.revalidate();
                listPanel.repaint();
            });

            add(header, BorderLayout.NORTH);
            add(scroll, BorderLayout.CENTER);
            setVisible(true);
        }

        private JPanel buildAlertRow(AlertEvent ev) {
            Color accent = switch (ev.severity) {
                case CRITICAL -> UITheme.ACCENT_RED;
                case WARNING  -> UITheme.ACCENT_ORANGE;
                default       -> UITheme.ACCENT_BLUE;
            };
            String icon = switch (ev.severity) {
                case CRITICAL -> "🔴";
                case WARNING  -> "🟡";
                default       -> "🔵";
            };

            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setBackground(UITheme.BG_CARD);
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                    new EmptyBorder(10, 12, 10, 12)));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel msg = new JLabel(icon + "  " + ev.message);
            msg.setFont(UITheme.FONT_NORMAL);
            msg.setForeground(UITheme.TEXT_PRIMARY);

            JLabel meta = new JLabel(ev.timestamp + "  |  " + ev.city + "  |  " + ev.disasterType);
            meta.setFont(UITheme.FONT_SMALL);
            meta.setForeground(UITheme.TEXT_SECONDARY);

            JLabel sev = new JLabel("  " + ev.severity.name() + "  ");
            sev.setFont(UITheme.FONT_SMALL);
            sev.setForeground(Color.WHITE);
            sev.setBackground(accent);
            sev.setOpaque(true);
            sev.setBorder(new EmptyBorder(2, 6, 2, 6));

            JPanel left = new JPanel(new BorderLayout(0, 3));
            left.setOpaque(false);
            left.add(msg, BorderLayout.NORTH);
            left.add(meta, BorderLayout.SOUTH);

            row.add(left, BorderLayout.CENTER);
            row.add(sev, BorderLayout.EAST);
            return row;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // TOAST WINDOW
    // ═══════════════════════════════════════════════════════════════════════

    public static class AlertToast extends JWindow {
        static final int WIDTH  = 380;
        static final int HEIGHT = 80;

        private final javax.swing.Timer dismissTimer;

        public AlertToast(AlertEvent event, java.util.function.Consumer<AlertToast> onDismiss) {
            setSize(WIDTH, HEIGHT);
            setAlwaysOnTop(true);

            Color accent = switch (event.severity) {
                case CRITICAL -> UITheme.ACCENT_RED;
                case WARNING  -> UITheme.ACCENT_ORANGE;
                default       -> UITheme.ACCENT_BLUE;
            };

            JPanel panel = new JPanel(new BorderLayout(8, 0));
            panel.setBackground(new Color(22, 36, 71));
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(accent, 2),
                    new EmptyBorder(10, 12, 10, 12)));

            // Left accent bar
            JPanel bar = new JPanel();
            bar.setBackground(accent);
            bar.setPreferredSize(new Dimension(5, HEIGHT));

            // Text
            String icon = switch (event.severity) {
                case CRITICAL -> "🔴 ";
                case WARNING  -> "🟡 ";
                default       -> "🔵 ";
            };
            JLabel msg = new JLabel("<html><b>" + icon + event.severity.name() + "</b>  "
                    + event.timestamp + "<br><small>" + event.message + "</small></html>");
            msg.setFont(UITheme.FONT_SMALL);
            msg.setForeground(Color.WHITE);

            // Close button
            JButton close = new JButton("✕");
            close.setFont(new Font("SansSerif", Font.BOLD, 11));
            close.setForeground(new Color(180, 200, 230));
            close.setBackground(new Color(22, 36, 71));
            close.setBorderPainted(false);
            close.setFocusPainted(false);
            close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            close.addActionListener(e -> dismiss());

            panel.add(bar,   BorderLayout.WEST);
            panel.add(msg,   BorderLayout.CENTER);
            panel.add(close, BorderLayout.EAST);
            setContentPane(panel);

            // Auto-dismiss after 6 seconds
            dismissTimer = new javax.swing.Timer(6000, e -> {
                dismiss();
                onDismiss.accept(this);
            });
            dismissTimer.setRepeats(false);
            dismissTimer.start();

            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    dismiss();
                    onDismiss.accept(AlertToast.this);
                }
            });
        }

        public void dismiss() {
            dismissTimer.stop();
            setVisible(false);
            dispose();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // NOTIFICATION SIMULATOR
    // ═══════════════════════════════════════════════════════════════════════

    public static class NotificationSimulator {
        private static final List<String> smsLog   = new ArrayList<>();
        private static final List<String> emailLog = new ArrayList<>();

        public static void sendSMS(AlertEvent event) {
            String msg = "[RescueNet SMS] " + event.timestamp
                    + " CRITICAL: " + event.message
                    + " — Location: " + event.city
                    + ". Call NDMA: 051-9205436";
            smsLog.add(msg);
            System.out.println("📱 SMS SENT: " + msg);
        }

        public static void sendEmail(AlertEvent event) {
            String email = """
                    To: admin@rescuenet.pk, ndma@ndma.gov.pk
                    From: alerts@rescuenet.pk
                    Subject: [CRITICAL ALERT] %s — %s
                    
                    Dear Relief Coordinator,
                    
                    A CRITICAL emergency alert has been raised:
                    
                    Message  : %s
                    Location : %s
                    Type     : %s
                    Time     : %s
                    
                    Please take immediate action.
                    
                    — RescueNet AI Automated Alert System
                    """.formatted(event.city, event.disasterType,
                    event.message, event.city, event.disasterType, event.timestamp);
            emailLog.add(email);
            System.out.println("📧 EMAIL SENT:\n" + email);
        }

        public static List<String> getSmsLog()   { return Collections.unmodifiableList(smsLog); }
        public static List<String> getEmailLog() { return Collections.unmodifiableList(emailLog); }

        /** Show a summary dialog */
        public static void showLogDialog(Component parent) {
            JTextArea area = new JTextArea();
            area.setEditable(false);
            area.setFont(new Font("Monospaced", Font.PLAIN, 11));
            StringBuilder sb = new StringBuilder("=== SMS LOG (" + smsLog.size() + " sent) ===\n\n");
            smsLog.forEach(s -> sb.append(s).append("\n\n"));
            sb.append("\n=== EMAIL LOG (").append(emailLog.size()).append(" sent) ===\n\n");
            emailLog.forEach(e -> sb.append(e).append("\n---\n"));
            area.setText(sb.toString());
            JScrollPane sp = new JScrollPane(area);
            sp.setPreferredSize(new Dimension(620, 420));
            JOptionPane.showMessageDialog(parent, sp, "Notification Log", JOptionPane.INFORMATION_MESSAGE);
        }
    }

}