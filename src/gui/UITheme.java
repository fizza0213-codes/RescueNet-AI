package gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * RescueNet AI — Professional UI Theme
 * Clean, government/emergency-grade light color system.
 */
public class UITheme {

    // ── Base palette ─────────────────────────────────────────
    public static final Color BG_DARK        = new Color(245, 247, 250);
    public static final Color BG_CARD        = new Color(255, 255, 255);
    public static final Color BG_PANEL       = new Color(235, 238, 245);
    public static final Color BG_INPUT       = new Color(255, 255, 255);
    public static final Color BG_NAV         = new Color(22,  36,  71);
    public static final Color BG_HEADER      = new Color(18,  30,  60);

    // ── Accent colors ─────────────────────────────────────────
    public static final Color ACCENT_RED     = new Color(194,  30,  30);
    public static final Color ACCENT_BLUE    = new Color( 22,  93, 190);
    public static final Color ACCENT_GREEN   = new Color( 25, 140,  70);
    public static final Color ACCENT_ORANGE  = new Color(214, 110,   0);
    public static final Color ACCENT_PURPLE  = new Color(100,  60, 190);
    public static final Color ACCENT_TEAL    = new Color( 14, 128, 140);

    // ── Text ─────────────────────────────────────────────────
    public static final Color TEXT_PRIMARY   = new Color( 25,  34,  56);
    public static final Color TEXT_SECONDARY = new Color( 90, 105, 135);
    public static final Color TEXT_WHITE     = new Color(240, 245, 255);

    // ── Borders ───────────────────────────────────────────────
    public static final Color BORDER_COLOR   = new Color(210, 215, 228);
    public static final Color DIVIDER        = new Color(225, 228, 238);

    // ── Severity row backgrounds (light) ─────────────────────
    public static final Color ROW_CRITICAL   = new Color(255, 228, 228);
    public static final Color ROW_SEVERE     = new Color(255, 242, 215);
    public static final Color ROW_MODERATE   = new Color(255, 252, 215);
    public static final Color ROW_MILD       = new Color(225, 252, 235);

    // ── Fonts ─────────────────────────────────────────────────
    public static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD,  22);
    public static final Font FONT_HEADER  = new Font("SansSerif", Font.BOLD,  15);
    public static final Font FONT_NORMAL  = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_BOLD    = new Font("SansSerif", Font.BOLD,  13);
    public static final Font FONT_MONO    = new Font("Monospaced", Font.PLAIN, 12);

    public static JPanel darkPanel() {
        JPanel p = new JPanel(); p.setBackground(BG_DARK); return p;
    }

    public static JLabel headerLabel(String text) {
        JLabel l = new JLabel(text); l.setFont(FONT_HEADER); l.setForeground(TEXT_PRIMARY); return l;
    }

    public static JLabel bodyLabel(String text) {
        JLabel l = new JLabel(text); l.setFont(FONT_NORMAL); l.setForeground(TEXT_SECONDARY); return l;
    }

    public static JTextField styledField() {
        JTextField f = new JTextField();
        f.setBackground(BG_INPUT); f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(TEXT_PRIMARY); f.setFont(FONT_NORMAL);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return f;
    }

    public static JPasswordField styledPassField() {
        JPasswordField f = new JPasswordField();
        f.setBackground(BG_INPUT); f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(TEXT_PRIMARY); f.setFont(FONT_NORMAL);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return f;
    }

    public static JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setBackground(BG_INPUT); c.setForeground(TEXT_PRIMARY); c.setFont(FONT_NORMAL);
        c.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        return c;
    }

    public static JTextArea styledTextArea() {
        JTextArea ta = new JTextArea();
        ta.setBackground(BG_INPUT); ta.setForeground(TEXT_PRIMARY);
        ta.setCaretColor(TEXT_PRIMARY); ta.setFont(FONT_NORMAL);
        ta.setLineWrap(true); ta.setWrapStyleWord(true);
        ta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return ta;
    }

    public static JButton primaryButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(FONT_BOLD); b.setFocusPainted(false);
        b.setBorderPainted(false); b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(bg.darker()); }
            public void mouseExited(java.awt.event.MouseEvent e)  { b.setBackground(bg); }
        });
        return b;
    }

    public static void styleTable(JTable table) {
        table.setBackground(BG_CARD); table.setForeground(TEXT_PRIMARY);
        table.setGridColor(DIVIDER); table.setFont(FONT_NORMAL);
        table.setRowHeight(30);
        table.setSelectionBackground(new Color(210, 225, 255));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowHorizontalLines(true); table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        JTableHeader h = table.getTableHeader();
        h.setBackground(BG_NAV); h.setForeground(TEXT_WHITE);
        h.setFont(FONT_BOLD); h.setPreferredSize(new Dimension(0, 36));
        h.setReorderingAllowed(false);
    }

    public static Border sectionBorder(String title, Color color) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(color, 1),
            "  " + title + "  ", TitledBorder.LEFT, TitledBorder.TOP, FONT_BOLD, color);
    }

    public static JPanel moduleHeader(String title, Color accent, Runnable onBack) {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(BG_HEADER);
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, accent));
        h.setPreferredSize(new Dimension(0, 54));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 9));
        left.setOpaque(false);
        JLabel accentBar = new JLabel(" ");
        accentBar.setOpaque(true); accentBar.setBackground(accent);
        accentBar.setPreferredSize(new Dimension(4, 34));
        JLabel lbl = new JLabel("  " + title);
        lbl.setFont(FONT_HEADER); lbl.setForeground(TEXT_WHITE);
        left.add(accentBar); left.add(lbl);

        JButton back = new JButton("← Back to Dashboard");
        back.setBackground(new Color(255,255,255,20));
        back.setForeground(new Color(180,200,230));
        back.setFont(FONT_SMALL); back.setFocusPainted(false);
        back.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80,100,150)),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.addActionListener(e -> onBack.run());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 10));
        right.setOpaque(false); right.add(back);

        h.add(left, BorderLayout.WEST); h.add(right, BorderLayout.EAST);
        return h;
    }

    public static Color severityRowColor(int level) {
        if (level >= 9) return ROW_CRITICAL;
        if (level >= 7) return ROW_SEVERE;
        if (level >= 4) return ROW_MODERATE;
        return ROW_MILD;
    }

    public static Color severityBadgeColor(int level) {
        if (level >= 9) return ACCENT_RED;
        if (level >= 7) return ACCENT_ORANGE;
        if (level >= 4) return new Color(160, 130, 0);
        return ACCENT_GREEN;
    }
}
