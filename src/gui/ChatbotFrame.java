package gui;

import chatbot.AIChatbot;
import database.ChatbotDAO;
import models.User;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.UUID;

/**
 * RescueNet AI Chatbot — Disaster Management Assistant.
 * Clean chat-bubble style UI, quick-topic buttons, chat history.
 */
public class ChatbotFrame extends JFrame {

    private final User       currentUser;
    private final ChatbotDAO chatDAO    = new ChatbotDAO();
    private final String     sessionId  = UUID.randomUUID().toString().substring(0, 8);

    private JTextPane   chatPane;
    private JTextField  txtInput;
    private JLabel      lblStatus;
    private StyledDocument doc;

    // Text styles
    private Style styleUser, styleBot, styleMeta, styleDivider;

    public ChatbotFrame(User user) {
        this.currentUser = user;
        setTitle("RescueNet AI — Disaster Assistant");
        setSize(860, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(600, 500));
        buildUI();
        showWelcome();
        setVisible(true);
    }

    private void buildUI() {
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(),     BorderLayout.NORTH);
        add(buildChatArea(),   BorderLayout.CENTER);
        add(buildInputPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        // Top bar
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UITheme.BG_HEADER);
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UITheme.ACCENT_GREEN));
        top.setPreferredSize(new Dimension(0, 54));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 9));
        left.setOpaque(false);
        JLabel accentBar = new JLabel(" ");
        accentBar.setOpaque(true);
        accentBar.setBackground(UITheme.ACCENT_GREEN);
        accentBar.setPreferredSize(new Dimension(4, 34));
        JLabel title = new JLabel("  🤖  Disaster Management Assistant");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.TEXT_WHITE);
        left.add(accentBar); left.add(title);

        boolean apiReady = AIChatbot.isApiConfigured();
        lblStatus = new JLabel(apiReady ? "● AI Connected" : "● Offline Mode");
        lblStatus.setFont(UITheme.FONT_SMALL);
        lblStatus.setForeground(apiReady ? UITheme.ACCENT_GREEN : UITheme.ACCENT_ORANGE);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 18));
        right.setOpaque(false);

        JButton btnBack = new JButton("← Back");
        btnBack.setBackground(new Color(255,255,255,20));
        btnBack.setForeground(new Color(180,200,230));
        btnBack.setFont(UITheme.FONT_SMALL); btnBack.setFocusPainted(false);
        btnBack.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80,100,150)),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> dispose());

        right.add(lblStatus); right.add(btnBack);
        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);

        // Quick-topic buttons
        JPanel quick = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 8));
        quick.setBackground(UITheme.BG_PANEL);
        quick.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR));

        JLabel ql = new JLabel("  Quick topics: ");
        ql.setFont(UITheme.FONT_SMALL);
        ql.setForeground(UITheme.TEXT_SECONDARY);
        quick.add(ql);

        String[][] topics = {
            {"🌊 Flood",        "What should I do during a flood?"},
            {"🏚️ Earthquake",   "Earthquake safety tips"},
            {"🔥 Fire",         "Fire emergency procedures"},
            {"🩺 First Aid",    "Basic first aid guidance"},
            {"🏠 Shelter",      "How to find emergency shelter?"},
            {"📞 Contacts",     "Emergency contact numbers Pakistan"},
            {"🎒 Kit",          "What to put in an emergency kit?"},
            {"🚶 Evacuation",   "How to evacuate safely?"}
        };
        for (String[] t : topics) {
            JButton b = new JButton(t[0]);
            b.setBackground(UITheme.BG_CARD);
            b.setForeground(UITheme.ACCENT_BLUE);
            b.setFont(UITheme.FONT_SMALL);
            b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
            final String q = t[1];
            b.addActionListener(e -> sendMessage(q));
            b.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { b.setBackground(UITheme.BG_PANEL); }
                public void mouseExited(MouseEvent e)  { b.setBackground(UITheme.BG_CARD); }
            });
            quick.add(b);
        }

        wrapper.add(top, BorderLayout.NORTH);
        wrapper.add(quick, BorderLayout.SOUTH);
        return wrapper;
    }

    private JScrollPane buildChatArea() {
        chatPane = new JTextPane();
        chatPane.setEditable(false);
        chatPane.setBackground(new Color(249, 250, 252));
        chatPane.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        doc = chatPane.getStyledDocument();

        // Define styles
        styleUser = doc.addStyle("user", null);
        StyleConstants.setForeground(styleUser, new Color(22, 93, 190));
        StyleConstants.setFontFamily(styleUser, "SansSerif");
        StyleConstants.setFontSize(styleUser, 13);
        StyleConstants.setBold(styleUser, true);

        styleBot = doc.addStyle("bot", null);
        StyleConstants.setForeground(styleBot, new Color(25, 34, 56));
        StyleConstants.setFontFamily(styleBot, "SansSerif");
        StyleConstants.setFontSize(styleBot, 13);

        styleMeta = doc.addStyle("meta", null);
        StyleConstants.setForeground(styleMeta, new Color(130, 145, 170));
        StyleConstants.setFontFamily(styleMeta, "SansSerif");
        StyleConstants.setFontSize(styleMeta, 11);

        styleDivider = doc.addStyle("div", null);
        StyleConstants.setForeground(styleDivider, new Color(210, 215, 228));
        StyleConstants.setFontFamily(styleDivider, "SansSerif");
        StyleConstants.setFontSize(styleDivider, 11);

        JScrollPane sp = new JScrollPane(chatPane);
        sp.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, UITheme.BORDER_COLOR));
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private JPanel buildInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(UITheme.BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        txtInput = UITheme.styledField();
        txtInput.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        txtInput.addActionListener(e -> sendMessage(txtInput.getText()));

        JButton btnSend = UITheme.primaryButton("Send", UITheme.ACCENT_BLUE);
        btnSend.setPreferredSize(new Dimension(90, 40));
        btnSend.addActionListener(e -> sendMessage(txtInput.getText()));

        JButton btnClear = UITheme.primaryButton("Clear", UITheme.TEXT_SECONDARY);
        btnClear.setPreferredSize(new Dimension(80, 40));
        btnClear.addActionListener(e -> {
            try { doc.remove(0, doc.getLength()); } catch (Exception ignored) {}
            showWelcome();
        });

        JButton btnHistory = UITheme.primaryButton("History", UITheme.ACCENT_TEAL != null ? new Color(14, 128, 140) : UITheme.ACCENT_BLUE);
        btnHistory.setPreferredSize(new Dimension(90, 40));
        btnHistory.addActionListener(e -> showHistory());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btns.setOpaque(false);
        btns.add(btnHistory); btns.add(btnClear); btns.add(btnSend);

        panel.add(txtInput, BorderLayout.CENTER);
        panel.add(btns, BorderLayout.EAST);
        return panel;
    }

    // ── Chat logic ───────────────────────────────────────────

    private void showWelcome() {
        appendStyled("RescueNet AI — Disaster Management Assistant\n", styleUser);
        appendStyled("Serving Pakistan's emergency response community\n", styleMeta);
        appendStyled("────────────────────────────────────────────────────\n\n", styleDivider);
        appendStyled("🤖 Assistant:  ", styleUser);
        appendStyled("Welcome, " + currentUser.getFullName() + "!\n\n" +
            "I can provide guidance on:\n" +
            "  • Flood safety and evacuation procedures\n" +
            "  • Earthquake response and aftershock safety\n" +
            "  • Fire emergency protocols\n" +
            "  • Basic first aid and medical guidance\n" +
            "  • Emergency shelter locations\n" +
            "  • Emergency kit preparation\n\n" +
            "Use the quick-topic buttons above or type your question below.\n\n", styleBot);
        appendStyled("────────────────────────────────────────────────────\n\n", styleDivider);
    }

    private void sendMessage(String message) {
        if (message == null || message.trim().isEmpty()) return;
        txtInput.setText("");

        // User message
        appendStyled("You:  ", styleUser);
        appendStyled(message.trim() + "\n\n", styleBot);

        // Thinking indicator
        appendStyled("🤖 Assistant is responding...\n", styleMeta);
        int thinkPos = -1;
        try { thinkPos = doc.getLength() - "🤖 Assistant is responding...\n".length(); }
        catch (Exception ignored) {}

        final String msg    = message.trim();
        final int    tPos   = thinkPos;

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            protected String doInBackground() { return AIChatbot.getResponse(msg); }
            protected void done() {
                try {
                    String response = get();
                    // Remove thinking line
                    if (tPos >= 0) {
                        try {
                            int len = "🤖 Assistant is responding...\n".length();
                            int rem = Math.min(len, doc.getLength() - tPos);
                            if (rem > 0) doc.remove(tPos, rem);
                        } catch (Exception ignored) {}
                    }
                    appendStyled("🤖 Assistant:  ", styleUser);
                    appendStyled(response + "\n\n", styleBot);
                    appendStyled("────────────────────────────────────────────────────\n\n", styleDivider);

                    // Save to history
                    String type = AIChatbot.getRuleBasedResponse(msg) != null ? "RULE_BASED" : "AI_API";
                    chatDAO.saveChat(
                        currentUser.getUserId() > 0 ? currentUser.getUserId() : null,
                        sessionId, msg, response, type);

                    // Auto-scroll
                    chatPane.setCaretPosition(doc.getLength());
                } catch (Exception e) {
                    appendStyled("⚠ Unable to get response. Please try again.\n\n", styleMeta);
                }
            }
        };
        worker.execute();
    }

    private void appendStyled(String text, Style style) {
        try {
            doc.insertString(doc.getLength(), text, style);
            chatPane.setCaretPosition(doc.getLength());
        } catch (Exception ignored) {}
    }

    private void showHistory() {
        JDialog d = new JDialog(this, "Chat History", true);
        d.setSize(720, 540);
        d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(UITheme.BG_DARK);
        d.setLayout(new BorderLayout(0, 0));

        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(UITheme.BG_HEADER);
        hdr.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(14,128,140)));
        hdr.setPreferredSize(new Dimension(0, 48));
        JLabel hl = new JLabel("  📜  Chat History");
        hl.setFont(UITheme.FONT_HEADER); hl.setForeground(UITheme.TEXT_WHITE);
        hdr.add(hl, BorderLayout.WEST);
        d.add(hdr, BorderLayout.NORTH);

        JTextArea area = new JTextArea("Loading...");
        area.setBackground(UITheme.BG_CARD);
        area.setForeground(UITheme.TEXT_PRIMARY);
        area.setFont(UITheme.FONT_MONO);
        area.setEditable(false);
        area.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        area.setLineWrap(true); area.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        d.add(sp, BorderLayout.CENTER);

        JButton close = UITheme.primaryButton("Close", UITheme.TEXT_SECONDARY);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        bp.setBackground(UITheme.BG_DARK);
        bp.add(close);
        close.addActionListener(e -> d.dispose());
        d.add(bp, BorderLayout.SOUTH);

        SwingWorker<String, Void> loader = new SwingWorker<>() {
            protected String doInBackground() throws Exception {
                StringBuilder sb = new StringBuilder();
                var list = chatDAO.getChatHistory(
                    currentUser.getUserId() > 0 ? currentUser.getUserId() : null, 20);
                if (list.isEmpty()) {
                    return "No chat history available.\nStart a conversation to build history.";
                }
                for (String[] row : list) {
                    sb.append("Date: ").append(row[3]).append("  [").append(row[2]).append("]\n");
                    sb.append("You:       ").append(row[0]).append("\n");
                    sb.append("Assistant: ").append(row[1]).append("\n");
                    sb.append("─────────────────────────────────────────────\n\n");
                }
                return sb.toString();
            }
            protected void done() {
                try { area.setText(get()); area.setCaretPosition(0); }
                catch (Exception ex) { area.setText("Error loading history: " + ex.getMessage()); }
            }
        };
        loader.execute();
        d.setVisible(true);
    }
}
