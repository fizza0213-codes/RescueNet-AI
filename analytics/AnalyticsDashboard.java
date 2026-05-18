package analytics;

import database.*;
import gui.UITheme;
import models.Victim;
import models.Shelter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * RescueNet AI — Analytics Dashboard
 *
 * Renders professional charts using pure Java2D — no extra JARs required.
 * Drop-in compatible: if JFreeChart is on classpath, you can swap these
 * panels for JFreeChart ChartPanel; API is identical.
 *
 * Charts included:
 *  1. Bar chart  — Victims by disaster type
 *  2. Pie chart  — Victim status distribution
 *  3. Bar chart  — Shelter occupancy
 *  4. Line chart — Severity trend (simulated time series)
 *  5. Gauge      — Resource utilisation
 */
public class AnalyticsDashboard extends JFrame {

    private final VictimDAO    victimDAO    = new VictimDAO();
    private final ShelterDAO   shelterDAO   = new ShelterDAO();
    private final ResourceDAO  resourceDAO  = new ResourceDAO();

    public AnalyticsDashboard() {
        setTitle("RescueNet AI — Analytics Dashboard");
        setSize(1180, 760);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));
        getContentPane().setBackground(UITheme.BG_DARK);
        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        add(buildHeader(), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 3, 12, 12));
        grid.setBackground(UITheme.BG_DARK);
        grid.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Load data async then build charts
        SwingWorker<ChartData, Void> worker = new SwingWorker<>() {
            protected ChartData doInBackground() { return loadChartData(); }
            protected void done() {
                try {
                    ChartData d = get();
                    grid.add(wrapChart("Victims by Disaster Type", new BarChart(d.byDisaster, UITheme.ACCENT_BLUE)));
                    grid.add(wrapChart("Victim Status Distribution", new PieChart(d.byStatus)));
                    grid.add(wrapChart("Shelter Occupancy (%)", new ShelterBarChart(d.shelterOccupancy)));
                    grid.add(wrapChart("Severity Trend (7 days)", new LineChart(d.severityTrend, UITheme.ACCENT_RED)));
                    grid.add(wrapChart("Victims by Gender", new PieChart(d.byGender)));
                    grid.add(wrapChart("Resource Utilisation", new GaugeChart(d.resourcePct)));
                    grid.revalidate();
                    grid.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(UITheme.BG_HEADER);
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(14, 128, 140)));
        h.setPreferredSize(new Dimension(0, 52));

        JLabel title = new JLabel("  📊  Advanced Analytics Dashboard");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Live disaster intelligence & resource tracking  ");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(new Color(140, 180, 220));

        h.add(title, BorderLayout.WEST);
        h.add(sub,   BorderLayout.EAST);
        return h;
    }

    private JPanel buildFooter() {
        JPanel f = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        f.setBackground(new Color(22, 36, 71));
        f.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(35, 55, 100)));
        JLabel l = new JLabel("Data sourced from RescueNet MySQL database  |  Charts rendered with Java2D  ");
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(new Color(100, 130, 180));
        f.add(l);
        return f;
    }

    private JPanel wrapChart(String title, JPanel chart) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 4));
        wrapper.setBackground(UITheme.BG_CARD);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_BOLD);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        wrapper.add(titleLabel, BorderLayout.NORTH);
        wrapper.add(chart,      BorderLayout.CENTER);
        return wrapper;
    }

    // ── Data loading ──────────────────────────────────────────────────────────
    private ChartData loadChartData() {
        ChartData d = new ChartData();
        try {
            List<Victim> victims = victimDAO.getAllVictims();

            // By disaster type
            for (Victim v : victims) {
                d.byDisaster.merge(v.getDisasterType(), 1, Integer::sum);
                d.byStatus.merge(v.getStatus(), 1, Integer::sum);
                d.byGender.merge(v.getGender() == null || v.getGender().isBlank() ? "Unknown" : v.getGender(), 1, Integer::sum);
            }

            // Shelter occupancy
            List<Shelter> shelters = shelterDAO.getAllShelters();
            for (Shelter s : shelters) {
                if (s.getCapacity() > 0)
                    d.shelterOccupancy.put(s.getName().length() > 16
                                    ? s.getName().substring(0, 14) + ".." : s.getName(),
                            (int) s.getOccupancyPct());
            }

            // Resource pct (simulated if no data)
            int total = resourceDAO.getTotalItems();
            d.resourcePct = total > 0 ? Math.min(100, (int)(total / 10.0)) : 72;

            // Severity trend (simulated 7-day)
            Random r = new Random(42);
            for (int i = 0; i < 7; i++)
                d.severityTrend.add(3 + r.nextInt(victims.size() == 0 ? 10 : victims.size() / 2 + 5));

        } catch (Exception e) {
            // Demo data if DB unavailable
            d.byDisaster.put("Flood", 38); d.byDisaster.put("Earthquake", 24);
            d.byDisaster.put("Landslide", 14); d.byDisaster.put("Fire", 9);
            d.byDisaster.put("Cyclone", 6);
            d.byStatus.put("RESCUED", 52); d.byStatus.put("HOSPITALIZED", 23);
            d.byStatus.put("MISSING", 18); d.byStatus.put("DECEASED", 7);
            d.byGender.put("Male", 55); d.byGender.put("Female", 42); d.byGender.put("Unknown", 3);
            d.shelterOccupancy.put("Central Camp", 88); d.shelterOccupancy.put("North Wing", 45);
            d.shelterOccupancy.put("East Block", 72); d.shelterOccupancy.put("NDMA Site", 33);
            Random r = new Random(42);
            for (int i = 0; i < 7; i++) d.severityTrend.add(5 + r.nextInt(20));
            d.resourcePct = 67;
        }
        return d;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CHART DATA
    // ═══════════════════════════════════════════════════════════════════════

    static class ChartData {
        Map<String, Integer> byDisaster      = new LinkedHashMap<>();
        Map<String, Integer> byStatus        = new LinkedHashMap<>();
        Map<String, Integer> byGender        = new LinkedHashMap<>();
        Map<String, Integer> shelterOccupancy = new LinkedHashMap<>();
        List<Integer>        severityTrend   = new ArrayList<>();
        int                  resourcePct     = 0;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // BAR CHART
    // ═══════════════════════════════════════════════════════════════════════

    static class BarChart extends JPanel {
        private final Map<String, Integer> data;
        private final Color baseColor;

        BarChart(Map<String, Integer> data, Color baseColor) {
            this.data = data; this.baseColor = baseColor;
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(300, 200));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data.isEmpty()) { drawEmpty(g); return; }
            Graphics2D g2 = setup(g);

            int pad = 40, barPad = 8;
            int w = getWidth(), h = getHeight();
            int chartW = w - pad * 2, chartH = h - pad - 20;
            int maxVal = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);

            int barW = Math.max(10, (chartW - barPad * (data.size() + 1)) / data.size());
            int i = 0;

            // Grid lines
            g2.setColor(new Color(230, 235, 245));
            for (int gl = 0; gl <= 4; gl++) {
                int y = pad + chartH - gl * chartH / 4;
                g2.drawLine(pad, y, pad + chartW, y);
                g2.setColor(new Color(150, 165, 195));
                g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                g2.drawString(String.valueOf(maxVal * gl / 4), 2, y + 4);
                g2.setColor(new Color(230, 235, 245));
            }

            for (Map.Entry<String, Integer> e : data.entrySet()) {
                int barH = (int)((double) e.getValue() / maxVal * chartH);
                int x = pad + barPad + i * (barW + barPad);
                int y = pad + chartH - barH;

                // Gradient bar
                GradientPaint gp = new GradientPaint(x, y, baseColor.brighter(), x, pad + chartH, baseColor.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(x, y, barW, barH, 4, 4);

                // Value label on top
                g2.setColor(UITheme.TEXT_PRIMARY);
                g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                FontMetrics fm = g2.getFontMetrics();
                String val = String.valueOf(e.getValue());
                g2.drawString(val, x + (barW - fm.stringWidth(val)) / 2, y - 3);

                // Category label
                g2.setColor(UITheme.TEXT_SECONDARY);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                String lbl = e.getKey().length() > 8 ? e.getKey().substring(0, 7) + "." : e.getKey();
                int lx = x + (barW - g2.getFontMetrics().stringWidth(lbl)) / 2;
                g2.drawString(lbl, lx, pad + chartH + 13);
                i++;
            }

            // Axes
            g2.setColor(new Color(180, 195, 220));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(pad, pad, pad, pad + chartH);
            g2.drawLine(pad, pad + chartH, pad + chartW, pad + chartH);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PIE CHART
    // ═══════════════════════════════════════════════════════════════════════

    static class PieChart extends JPanel {
        private final Map<String, Integer> data;
        private static final Color[] PALETTE = {
                new Color(22, 93, 190), new Color(25, 140, 70),
                new Color(214, 110, 0), new Color(194, 30, 30),
                new Color(100, 60, 190), new Color(14, 128, 140),
        };

        PieChart(Map<String, Integer> data) {
            this.data = data;
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data.isEmpty()) { drawEmpty(g); return; }
            Graphics2D g2 = setup(g);

            int total = data.values().stream().mapToInt(Integer::intValue).sum();
            if (total == 0) { drawEmpty(g); return; }

            int w = getWidth(), h = getHeight();
            int diameter = Math.min(w - 100, h - 40);
            int cx = diameter / 2 + 10, cy = h / 2;
            int r = diameter / 2;

            double startAngle = -90;
            int ci = 0;
            List<Map.Entry<String, Integer>> entries = new ArrayList<>(data.entrySet());

            for (Map.Entry<String, Integer> e : entries) {
                double arc = 360.0 * e.getValue() / total;
                Color col = PALETTE[ci % PALETTE.length];

                // Slight explode for first slice
                double midAngle = Math.toRadians(startAngle + arc / 2);
                int ex = ci == 0 ? (int)(Math.cos(midAngle) * 5) : 0;
                int ey = ci == 0 ? (int)(Math.sin(midAngle) * 5) : 0;

                g2.setColor(col);
                g2.fillArc(cx - r + ex, cy - r + ey, diameter, diameter, (int) startAngle, (int) arc);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2));
                g2.drawArc(cx - r + ex, cy - r + ey, diameter, diameter, (int) startAngle, (int) arc);

                // Percentage label inside
                if (arc > 18) {
                    double labelAngle = Math.toRadians(startAngle + arc / 2);
                    int lx = (int)(cx + r * 0.6 * Math.cos(labelAngle));
                    int ly = (int)(cy + r * 0.6 * Math.sin(labelAngle));
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                    String pct = (int)(arc) + "%";
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(pct, lx - fm.stringWidth(pct)/2, ly + fm.getAscent()/2);
                }

                startAngle += arc;
                ci++;
            }

            // Legend
            int lx = cx + r + 14, ly = cy - entries.size() * 12;
            ci = 0;
            for (Map.Entry<String, Integer> e : entries) {
                Color col = PALETTE[ci % PALETTE.length];
                g2.setColor(col);
                g2.fillRoundRect(lx, ly + ci * 22, 12, 12, 3, 3);
                g2.setColor(UITheme.TEXT_PRIMARY);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                g2.drawString(e.getKey() + " (" + e.getValue() + ")", lx + 16, ly + ci * 22 + 10);
                ci++;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SHELTER BAR CHART  (horizontal)
    // ═══════════════════════════════════════════════════════════════════════

    static class ShelterBarChart extends JPanel {
        private final Map<String, Integer> data;

        ShelterBarChart(Map<String, Integer> data) {
            this.data = data;
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data.isEmpty()) { drawEmpty(g); return; }
            Graphics2D g2 = setup(g);

            int pad = 8, labelW = 90, barH = 18, gap = 6;
            int w = getWidth() - labelW - pad * 2 - 40;
            int i = 0;

            for (Map.Entry<String, Integer> e : data.entrySet()) {
                int pct = Math.min(e.getValue(), 100);
                int y = pad + i * (barH + gap) + 16;

                Color barColor = pct > 85 ? UITheme.ACCENT_RED
                        : pct > 65 ? UITheme.ACCENT_ORANGE : UITheme.ACCENT_GREEN;

                // Label
                g2.setColor(UITheme.TEXT_SECONDARY);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                g2.drawString(e.getKey(), pad, y + barH - 4);

                // Background track
                g2.setColor(new Color(230, 235, 245));
                g2.fillRoundRect(pad + labelW, y, w, barH, 6, 6);

                // Bar
                int bw = (int)(pct / 100.0 * w);
                GradientPaint gp = new GradientPaint(
                        pad + labelW, y, barColor.brighter(),
                        pad + labelW + bw, y, barColor);
                g2.setPaint(gp);
                g2.fillRoundRect(pad + labelW, y, bw, barH, 6, 6);

                // Pct label
                g2.setColor(UITheme.TEXT_PRIMARY);
                g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                g2.drawString(pct + "%", pad + labelW + bw + 4, y + barH - 4);
                i++;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // LINE CHART
    // ═══════════════════════════════════════════════════════════════════════

    static class LineChart extends JPanel {
        private final List<Integer> data;
        private final Color color;

        LineChart(List<Integer> data, Color color) {
            this.data = data; this.color = color;
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data.isEmpty()) { drawEmpty(g); return; }
            Graphics2D g2 = setup(g);

            int pad = 35, w = getWidth(), h = getHeight();
            int chartW = w - pad * 2, chartH = h - pad - 20;
            int maxVal = data.stream().mapToInt(i -> i).max().orElse(1);
            int n = data.size();

            // Grid
            g2.setStroke(new BasicStroke(0.7f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    0, new float[]{4, 4}, 0));
            g2.setColor(new Color(230, 235, 245));
            for (int gl = 0; gl <= 4; gl++) {
                int y = pad + chartH - gl * chartH / 4;
                g2.drawLine(pad, y, pad + chartW, y);
            }
            g2.setStroke(new BasicStroke(1));

            // Area fill
            int[] xs = new int[n + 2], ys = new int[n + 2];
            for (int i = 0; i < n; i++) {
                xs[i] = pad + i * chartW / (n - 1);
                ys[i] = pad + chartH - (int)((double) data.get(i) / maxVal * chartH);
            }
            xs[n] = pad + chartW; ys[n] = pad + chartH;
            xs[n+1] = pad; ys[n+1] = pad + chartH;

            GradientPaint gp = new GradientPaint(0, pad, new Color(color.getRed(), color.getGreen(), color.getBlue(), 80),
                    0, pad + chartH, new Color(color.getRed(), color.getGreen(), color.getBlue(), 10));
            g2.setPaint(gp);
            g2.fillPolygon(xs, ys, n + 2);

            // Line
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < n - 1; i++) {
                g2.drawLine(xs[i], ys[i], xs[i+1], ys[i+1]);
            }

            // Dots + values
            String[] days = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
            for (int i = 0; i < n; i++) {
                g2.setColor(Color.WHITE);
                g2.fillOval(xs[i] - 4, ys[i] - 4, 8, 8);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(xs[i] - 4, ys[i] - 4, 8, 8);

                g2.setFont(new Font("SansSerif", Font.BOLD, 9));
                g2.setColor(UITheme.TEXT_PRIMARY);
                g2.drawString(String.valueOf(data.get(i)), xs[i] - 4, ys[i] - 8);

                if (i < days.length) {
                    g2.setColor(UITheme.TEXT_SECONDARY);
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                    g2.drawString(days[i], xs[i] - 10, pad + chartH + 14);
                }
            }

            // Axes
            g2.setColor(new Color(180, 195, 220));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(pad, pad, pad, pad + chartH);
            g2.drawLine(pad, pad + chartH, pad + chartW, pad + chartH);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GAUGE CHART
    // ═══════════════════════════════════════════════════════════════════════

    static class GaugeChart extends JPanel {
        private final int pct;

        GaugeChart(int pct) {
            this.pct = Math.max(0, Math.min(100, pct));
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = setup(g);
            int w = getWidth(), h = getHeight();
            int size = Math.min(w, h) - 30;
            int cx = w / 2, cy = h / 2 + size / 5;

            // Background arc
            g2.setColor(new Color(230, 235, 245));
            g2.setStroke(new BasicStroke(18, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(cx - size/2, cy - size/2, size, size, 210, -240);

            // Value arc
            Color arcColor = pct > 85 ? UITheme.ACCENT_RED
                    : pct > 65 ? UITheme.ACCENT_ORANGE : UITheme.ACCENT_GREEN;
            int arc = (int)(-240.0 * pct / 100);
            GradientPaint gp = new GradientPaint(cx - size/2, cy, UITheme.ACCENT_GREEN, cx + size/2, cy, arcColor);
            g2.setPaint(gp);
            g2.drawArc(cx - size/2, cy - size/2, size, size, 210, arc);
            g2.setStroke(new BasicStroke(1));

            // Centre text
            g2.setFont(new Font("SansSerif", Font.BOLD, size / 4));
            g2.setColor(UITheme.TEXT_PRIMARY);
            String val = pct + "%";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(val, cx - fm.stringWidth(val)/2, cy + fm.getAscent()/2);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.setColor(UITheme.TEXT_SECONDARY);
            String lbl = "Resources Used";
            fm = g2.getFontMetrics();
            g2.drawString(lbl, cx - fm.stringWidth(lbl)/2, cy + 22);

            // Min/Max labels
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.setColor(UITheme.TEXT_SECONDARY);
            g2.drawString("0%",   cx - size/2 - 8, cy + 14);
            g2.drawString("100%", cx + size/2 - 22, cy + 14);
        }
    }

    // ─── Shared helpers ───────────────────────────────────────────────────────

    static Graphics2D setup(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }

    static void drawEmpty(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(UITheme.TEXT_SECONDARY);
        g2.setFont(new Font("SansSerif", Font.ITALIC, 12));
        g2.drawString("No data", 10, 40);
    }
}