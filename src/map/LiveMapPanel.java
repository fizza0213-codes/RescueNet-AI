package map;

import models.Victim;
import models.Shelter;
import models.RescueTeam;
import gui.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class LiveMapPanel extends JPanel {

    // ── Map tile constants ────────────────────────────────────────────────────
    private static final int    TILE_SIZE   = 256;
    private static final String TILE_URL    = "https://tile.openstreetmap.org/%d/%d/%d.png";
    private static final int    MAX_ZOOM    = 18;
    private static final int    MIN_ZOOM    = 2;

    // Default centre: Lahore, Pakistan
    private double centerLat = 31.5204;
    private double centerLon = 72.8584; // wider Pakistan view
    private int    zoom      = 7;

    // ── Data lists ────────────────────────────────────────────────────────────
    private final List<MapMarker> victims  = new ArrayList<>();
    private final List<MapMarker> shelters = new ArrayList<>();
    private final List<MapMarker> teams    = new ArrayList<>();

    // ── Tile cache ────────────────────────────────────────────────────────────
    private final Map<String, Image> tileCache = new LinkedHashMap<>(200, 0.75f, true) {
        protected boolean removeEldestEntry(Map.Entry<String, Image> e) { return size() > 150; }
    };

    // ── Interaction state ─────────────────────────────────────────────────────
    private Point   dragStart;
    private double  dragStartLat, dragStartLon;
    private MapMarker hoveredMarker;
    private MapMarker selectedMarker;

    // ── Animation ─────────────────────────────────────────────────────────────
    private float   pulsePhase = 0f;
    private javax.swing.Timer animTimer;
    private javax.swing.Timer simulationTimer;

    // ── Route ─────────────────────────────────────────────────────────────────
    private MapMarker routeFrom, routeTo;

    // ── Legend / controls visibility ──────────────────────────────────────────
    private boolean showLegend = true;

    public LiveMapPanel() {
        setBackground(new Color(200, 220, 240));
        setPreferredSize(new Dimension(900, 560));
        setMinimumSize(new Dimension(400, 300));

        setupMouseListeners();
        startAnimationTimer();
        startSimulation();
        loadDefaultSampleData();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PUBLIC API — called by MapFrame to push live data
    // ═══════════════════════════════════════════════════════════════════════

    public void clearAll() {
        victims.clear(); shelters.clear(); teams.clear();
        routeFrom = routeTo = null;
        repaint();
    }

    public void addVictim(Victim v) {
        double[] coords = geocodeLocation(v.getLocation());
        Color c = severityColor(v.getSeverityLevel());
        MapMarker m = new MapMarker(coords[0], coords[1],
                "V", v.getName() + "\nSeverity: " + v.getSeverityLabel()
                + "\nStatus: " + v.getStatus() + "\nLocation: " + v.getLocation(), c, MarkerType.VICTIM);
        m.id = v.getVictimId();
        victims.add(m);
        repaint();
    }

    public void addShelter(Shelter s) {
        double[] coords = geocodeLocation(s.getLocation() + " " + s.getCity());
        String tooltip = s.getName() + "\n" + s.getCity()
                + "\nCapacity: " + s.getOccupied() + "/" + s.getCapacity()
                + "\nStatus: " + s.getStatus()
                + "\n" + s.getAmenitiesStr();
        MapMarker m = new MapMarker(coords[0], coords[1], "S", tooltip,
                new Color(25, 140, 70), MarkerType.SHELTER);
        m.id = s.getShelterId();
        m.capacity = s.getCapacity();
        m.occupied = s.getOccupied();
        shelters.add(m);
        repaint();
    }

    public void addTeam(RescueTeam t) {
        double[] coords = geocodeLocation(t.getCurrentLocation());
        String tooltip = t.getTeamName() + "\nLeader: " + t.getLeader()
                + "\nVehicle: " + t.getVehicle()
                + "\nStatus: " + t.getStatus()
                + "\nLocation: " + t.getCurrentLocation();
        MapMarker m = new MapMarker(coords[0], coords[1], "T", tooltip,
                new Color(214, 110, 0), MarkerType.TEAM);
        m.id = t.getTeamId();
        teams.add(m);
        repaint();
    }

    /** Highlight nearest shelter to the selected victim */
    public void showNearestShelterRoute() {
        if (selectedMarker == null || selectedMarker.type != MarkerType.VICTIM) {
            JOptionPane.showMessageDialog(this,
                    "Please click a victim pin (V) first, then click 'Nearest Shelter'.",
                    "Select Victim", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        MapMarker nearest = null;
        double minDist = Double.MAX_VALUE;
        for (MapMarker s : shelters) {
            double d = haversine(selectedMarker.lat, selectedMarker.lon, s.lat, s.lon);
            if (d < minDist) { minDist = d; nearest = s; }
        }
        if (nearest != null) {
            routeFrom = selectedMarker;
            routeTo   = nearest;
            repaint();
            JOptionPane.showMessageDialog(this,
                    String.format("Nearest shelter: %s\nEstimated distance: %.1f km\nETA (30 km/h): %.0f min",
                            nearest.tooltip.split("\n")[0], minDist, minDist / 30 * 60),
                    "Route Found", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /** Highlight nearest rescue team to selected victim */
    public void showNearestTeamRoute() {
        if (selectedMarker == null || selectedMarker.type != MarkerType.VICTIM) {
            JOptionPane.showMessageDialog(this,
                    "Please click a victim pin (V) first, then click 'Nearest Team'.",
                    "Select Victim", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        MapMarker nearest = null;
        double minDist = Double.MAX_VALUE;
        for (MapMarker t : teams) {
            double d = haversine(selectedMarker.lat, selectedMarker.lon, t.lat, t.lon);
            if (d < minDist) { minDist = d; nearest = t; }
        }
        if (nearest != null) {
            routeFrom = selectedMarker;
            routeTo   = nearest;
            repaint();
            JOptionPane.showMessageDialog(this,
                    String.format("Nearest team: %s\nEstimated distance: %.1f km\nETA (60 km/h): %.0f min",
                            nearest.tooltip.split("\n")[0], minDist, minDist / 60 * 60),
                    "Team Dispatched", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void clearRoute() { routeFrom = routeTo = null; repaint(); }

    public void zoomIn()  { if (zoom < MAX_ZOOM) { zoom++; repaint(); } }
    public void zoomOut() { if (zoom > MIN_ZOOM) { zoom--; repaint(); } }

    public void centreOnPakistan() {
        centerLat = 30.3753; centerLon = 69.3451; zoom = 6; repaint();
    }

    public void centreOnLahore() {
        centerLat = 31.5204; centerLon = 74.3587; zoom = 12; repaint();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PAINTING
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawTiles(g2);
        drawRoute(g2);
        drawShelterZones(g2);
        drawMarkers(g2, shelters, 18);
        drawMarkers(g2, victims,  16);
        drawMarkers(g2, teams,    18);
        drawTooltip(g2);
        if (showLegend) drawLegend(g2);
        drawCompass(g2);
        drawScale(g2);
        drawCoordinateOverlay(g2);
    }

    // ── OSM tile rendering ────────────────────────────────────────────────────
    private void drawTiles(Graphics2D g2) {
        int w = getWidth(), h = getHeight();
        double tilesAtZoom = Math.pow(2, zoom);

        double centerTileX = lonToTileX(centerLon, zoom);
        double centerTileY = latToTileY(centerLat, zoom);

        int startTileX = (int) Math.floor(centerTileX - (w / 2.0 / TILE_SIZE));
        int startTileY = (int) Math.floor(centerTileY - (h / 2.0 / TILE_SIZE));
        int endTileX   = (int) Math.ceil (centerTileX + (w / 2.0 / TILE_SIZE));
        int endTileY   = (int) Math.ceil (centerTileY + (h / 2.0 / TILE_SIZE));

        for (int tx = startTileX; tx <= endTileX; tx++) {
            for (int ty = startTileY; ty <= endTileY; ty++) {
                int nTx = (int)(((tx % tilesAtZoom) + tilesAtZoom) % tilesAtZoom);
                int nTy = ty;
                if (nTy < 0 || nTy >= tilesAtZoom) continue;

                int pixX = (int)((tx - centerTileX) * TILE_SIZE + w / 2.0);
                int pixY = (int)((ty - centerTileY) * TILE_SIZE + h / 2.0);

                Image tile = getTile(zoom, nTx, nTy);
                if (tile != null) {
                    g2.drawImage(tile, pixX, pixY, TILE_SIZE, TILE_SIZE, this);
                } else {
                    g2.setColor(new Color(210, 225, 240));
                    g2.fillRect(pixX, pixY, TILE_SIZE, TILE_SIZE);
                    g2.setColor(new Color(180, 200, 220));
                    g2.drawRect(pixX, pixY, TILE_SIZE, TILE_SIZE);
                    g2.setColor(new Color(140, 165, 190));
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                    g2.drawString("Loading...", pixX + 4, pixY + 14);
                }
            }
        }
    }

    private Image getTile(int z, int x, int y) {
        String key = z + "/" + x + "/" + y;
        if (tileCache.containsKey(key)) return tileCache.get(key);

        // Async fetch
        final String url = String.format(TILE_URL, z, x, y);
        final String cacheKey = key;
        new Thread(() -> {
            try {
                java.net.URL u = new java.net.URL(url);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
                conn.setRequestProperty("User-Agent", "RescueNetAI/1.0 (Emergency Management; contact@rescuenet.pk)");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(8000);
                Image img = javax.imageio.ImageIO.read(conn.getInputStream());
                if (img != null) {
                    tileCache.put(cacheKey, img);
                    SwingUtilities.invokeLater(this::repaint);
                }
            } catch (Exception ignored) {}
        }).start();

        return null;
    }

    // ── Route ─────────────────────────────────────────────────────────────────
    private void drawRoute(Graphics2D g2) {
        if (routeFrom == null || routeTo == null) return;
        Point pf = latLonToPixel(routeFrom.lat, routeFrom.lon);
        Point pt = latLonToPixel(routeTo.lat, routeTo.lon);

        // Dashed route line
        g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                0, new float[]{10, 6}, 0));
        g2.setColor(new Color(255, 180, 0, 200));
        g2.drawLine(pf.x, pf.y, pt.x, pt.y);
        g2.setStroke(new BasicStroke(1));

        // Direction arrow midpoint
        int mx = (pf.x + pt.x) / 2, my = (pf.y + pt.y) / 2;
        g2.setColor(new Color(255, 180, 0));
        g2.fillOval(mx - 5, my - 5, 10, 10);

        double dist = haversine(routeFrom.lat, routeFrom.lon, routeTo.lat, routeTo.lon);
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.setColor(Color.WHITE);
        String label = String.format("%.1f km", dist);
        g2.drawString(label, mx + 8, my - 4);
    }

    // ── Shelter zones ─────────────────────────────────────────────────────────
    private void drawShelterZones(Graphics2D g2) {
        for (MapMarker s : shelters) {
            Point p = latLonToPixel(s.lat, s.lon);
            int r = 30 + (int)(s.capacity / 50);
            g2.setColor(new Color(25, 140, 70, 25));
            g2.fillOval(p.x - r, p.y - r, r * 2, r * 2);
            g2.setColor(new Color(25, 140, 70, 80));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(p.x - r, p.y - r, r * 2, r * 2);
            g2.setStroke(new BasicStroke(1));
        }
    }

    // ── Generic marker drawing ────────────────────────────────────────────────
    private void drawMarkers(Graphics2D g2, List<MapMarker> markers, int size) {
        for (MapMarker m : markers) {
            Point p = latLonToPixel(m.lat, m.lon);
            if (p.x < -size || p.x > getWidth() + size) continue;
            if (p.y < -size || p.y > getHeight() + size) continue;

            boolean isSelected = m == selectedMarker;
            boolean isHovered  = m == hoveredMarker;

            // Pulse ring for teams
            if (m.type == MarkerType.TEAM) {
                float pulse = (float)(Math.sin(pulsePhase) * 0.5 + 0.5);
                int pr = (int)(size + 12 * pulse);
                g2.setColor(new Color(214, 110, 0, (int)(80 * (1 - pulse))));
                g2.fillOval(p.x - pr, p.y - pr, pr * 2, pr * 2);
            }

            // Selection glow
            if (isSelected) {
                g2.setColor(new Color(255, 220, 50, 150));
                g2.fillOval(p.x - size - 6, p.y - size - 6, (size + 6) * 2, (size + 6) * 2);
            }

            // Shadow
            g2.setColor(new Color(0, 0, 0, 60));
            g2.fillOval(p.x - size + 3, p.y - size + 3, size * 2, size * 2);

            // Body
            Color fill = isHovered ? m.color.brighter() : m.color;
            g2.setColor(fill);
            g2.fillOval(p.x - size, p.y - size, size * 2, size * 2);

            // Border
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(p.x - size, p.y - size, size * 2, size * 2);
            g2.setStroke(new BasicStroke(1));

            // Label
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, size - 4));
            FontMetrics fm = g2.getFontMetrics();
            String label = m.label;
            g2.drawString(label, p.x - fm.stringWidth(label) / 2, p.y + fm.getAscent() / 2 - 1);
        }
    }

    // ── Tooltip ───────────────────────────────────────────────────────────────
    private void drawTooltip(Graphics2D g2) {
        MapMarker hovered = hoveredMarker != null ? hoveredMarker : selectedMarker;
        if (hovered == null) return;

        Point p = latLonToPixel(hovered.lat, hovered.lon);
        String[] lines = hovered.tooltip.split("\n");
        int lineH = 17, padX = 10, padY = 8;
        int tw = 0;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        for (String l : lines) tw = Math.max(tw, g2.getFontMetrics().stringWidth(l));

        int bw = tw + padX * 2, bh = lines.length * lineH + padY * 2;
        int bx = p.x + 14, by = p.y - bh / 2;

        // Keep in bounds
        if (bx + bw > getWidth() - 10) bx = p.x - bw - 14;
        by = Math.max(10, Math.min(getHeight() - bh - 10, by));

        // Background
        g2.setColor(new Color(15, 25, 50, 230));
        g2.fillRoundRect(bx, by, bw, bh, 10, 10);
        g2.setColor(hovered.color);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(bx, by, bw, bh, 10, 10);
        g2.setStroke(new BasicStroke(1));

        // First line bold
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.setColor(Color.WHITE);
        g2.drawString(lines[0], bx + padX, by + padY + 12);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(new Color(180, 200, 235));
        for (int i = 1; i < lines.length; i++) {
            g2.drawString(lines[i], bx + padX, by + padY + 12 + i * lineH);
        }
    }

    // ── Legend ────────────────────────────────────────────────────────────────
    private void drawLegend(Graphics2D g2) {
        int lx = 10, ly = getHeight() - 145;
        g2.setColor(new Color(15, 25, 50, 210));
        g2.fillRoundRect(lx, ly, 160, 130, 8, 8);
        g2.setColor(new Color(60, 90, 150));
        g2.drawRoundRect(lx, ly, 160, 130, 8, 8);

        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.setColor(Color.WHITE);
        g2.drawString("MAP LEGEND", lx + 10, ly + 18);

        Object[][] items = {
                {"V", new Color(194, 30, 30),   "Victim (Critical)"},
                {"V", new Color(214, 110, 0),   "Victim (Moderate)"},
                {"S", new Color(25, 140, 70),   "Shelter / Safe Zone"},
                {"T", new Color(214, 110, 0),   "Rescue Team"},
                {"─", new Color(255, 180, 0),   "Optimised Route"},
        };
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        for (int i = 0; i < items.length; i++) {
            int iy = ly + 34 + i * 19;
            g2.setColor((Color) items[i][1]);
            g2.fillOval(lx + 10, iy - 8, 14, 14);
            g2.setColor(new Color(180, 200, 235));
            g2.drawString((String) items[i][2], lx + 30, iy + 3);
        }
    }

    // ── Compass ───────────────────────────────────────────────────────────────
    private void drawCompass(Graphics2D g2) {
        int cx = getWidth() - 40, cy = 40, r = 20;
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillOval(cx - r, cy - r, r * 2, r * 2);
        g2.setColor(new Color(60, 90, 150));
        g2.drawOval(cx - r, cy - r, r * 2, r * 2);
        // N arrow
        int[] nx = {cx, cx - 5, cx + 5};
        int[] ny = {cy - r + 5, cy, cy};
        g2.setColor(new Color(220, 50, 50));
        g2.fillPolygon(nx, ny, 3);
        // S arrow
        int[] sx = {cx, cx - 5, cx + 5};
        int[] sy = {cy + r - 5, cy, cy};
        g2.setColor(Color.WHITE);
        g2.fillPolygon(sx, sy, 3);
        g2.setFont(new Font("SansSerif", Font.BOLD, 9));
        g2.setColor(Color.WHITE);
        g2.drawString("N", cx - 4, cy - r + 18);
    }

    // ── Scale bar ─────────────────────────────────────────────────────────────
    private void drawScale(Graphics2D g2) {
        double metersPerPixel = 156543.03392 * Math.cos(Math.toRadians(centerLat)) / Math.pow(2, zoom);
        double scaleKm = metersPerPixel * 80 / 1000;
        String scaleLabel = scaleKm > 1 ? String.format("%.0f km", scaleKm) : String.format("%.0f m", scaleKm * 1000);

        int sx = 10, sy = getHeight() - 22;
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(sx, sy, 80, 6);
        g2.setColor(Color.WHITE);
        g2.fillRect(sx, sy, 40, 6);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.setColor(Color.WHITE);
        g2.drawString(scaleLabel, sx + 84, sy + 7);
    }

    // ── Coordinate overlay ────────────────────────────────────────────────────
    private void drawCoordinateOverlay(Graphics2D g2) {
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(getWidth() - 200, 10, 190, 18, 4, 4);
        g2.setColor(new Color(180, 210, 255));
        g2.drawString(String.format("Zoom:%d  Lat:%.4f  Lon:%.4f", zoom, centerLat, centerLon),
                getWidth() - 196, 22);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // INTERACTION
    // ═══════════════════════════════════════════════════════════════════════

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                dragStart    = e.getPoint();
                dragStartLat = centerLat;
                dragStartLon = centerLon;
            }
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    // Double-click: zoom in and centre
                    double[] ll = pixelToLatLon(e.getX(), e.getY());
                    centerLat = ll[0]; centerLon = ll[1];
                    if (zoom < MAX_ZOOM) zoom++;
                    repaint();
                    return;
                }
                // Single click: select marker
                selectedMarker = findMarkerAt(e.getPoint());
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (dragStart == null) return;
                double dx = e.getX() - dragStart.x;
                double dy = e.getY() - dragStart.y;
                double tilesAtZoom = Math.pow(2, zoom);
                double lonPerPix = 360.0 / (tilesAtZoom * TILE_SIZE);
                double latPerPix = 360.0 / (tilesAtZoom * TILE_SIZE); // approx

                centerLon = dragStartLon - dx * lonPerPix;
                centerLat = dragStartLat + dy * latPerPix;
                centerLat = Math.max(-85, Math.min(85, centerLat));
                repaint();
            }
            public void mouseMoved(MouseEvent e) {
                MapMarker prev = hoveredMarker;
                hoveredMarker = findMarkerAt(e.getPoint());
                if (hoveredMarker != prev) repaint();
                setCursor(hoveredMarker != null
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
            }
        });

        addMouseWheelListener(e -> {
            int notches = e.getWheelRotation();
            if (notches < 0 && zoom < MAX_ZOOM) zoom++;
            else if (notches > 0 && zoom > MIN_ZOOM) zoom--;
            repaint();
        });
    }

    private MapMarker findMarkerAt(Point p) {
        List<MapMarker> all = new ArrayList<>();
        all.addAll(victims); all.addAll(shelters); all.addAll(teams);
        for (MapMarker m : all) {
            Point mp = latLonToPixel(m.lat, m.lon);
            if (mp.distance(p) < 20) return m;
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // TIMERS
    // ═══════════════════════════════════════════════════════════════════════

    private void startAnimationTimer() {
        animTimer = new javax.swing.Timer(50, e -> {
            pulsePhase += 0.15f;
            if (pulsePhase > Math.PI * 2) pulsePhase = 0;
            repaint();
        });
        animTimer.start();
    }

    /** Simulate rescue teams slowly moving toward victims */
    private void startSimulation() {
        simulationTimer = new javax.swing.Timer(3000, e -> {
            for (MapMarker t : teams) {
                if (!victims.isEmpty()) {
                    MapMarker target = victims.get(new Random().nextInt(victims.size()));
                    t.lat += (target.lat - t.lat) * 0.05;
                    t.lon += (target.lon - t.lon) * 0.05;
                }
            }
            repaint();
        });
        simulationTimer.start();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // COORDINATE MATH
    // ═══════════════════════════════════════════════════════════════════════

    private Point latLonToPixel(double lat, double lon) {
        double tilesAtZoom = Math.pow(2, zoom);
        double centerTileX = lonToTileX(centerLon, zoom);
        double centerTileY = latToTileY(centerLat, zoom);
        double tileLon     = lonToTileX(lon, zoom);
        double tileLat     = latToTileY(lat, zoom);
        int px = (int)((tileLon - centerTileX) * TILE_SIZE + getWidth()  / 2.0);
        int py = (int)((tileLat - centerTileY) * TILE_SIZE + getHeight() / 2.0);
        return new Point(px, py);
    }

    private double[] pixelToLatLon(int x, int y) {
        double centerTileX = lonToTileX(centerLon, zoom);
        double centerTileY = latToTileY(centerLat, zoom);
        double tx = centerTileX + (x - getWidth()  / 2.0) / TILE_SIZE;
        double ty = centerTileY + (y - getHeight() / 2.0) / TILE_SIZE;
        double lon = tx / Math.pow(2, zoom) * 360.0 - 180.0;
        double n   = Math.PI - 2 * Math.PI * ty / Math.pow(2, zoom);
        double lat = Math.toDegrees(Math.atan(Math.sinh(n)));
        return new double[]{lat, lon};
    }

    private double lonToTileX(double lon, int z) {
        return (lon + 180.0) / 360.0 * Math.pow(2, z);
    }

    private double latToTileY(double lat, int z) {
        double rad = Math.toRadians(lat);
        return (1.0 - Math.log(Math.tan(rad) + 1.0 / Math.cos(rad)) / Math.PI) / 2.0 * Math.pow(2, z);
    }

    /** Haversine distance in km */
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GEOCODING SIMULATION
    // (In production: call Nominatim API / Google Maps Geocoding)
    // ═══════════════════════════════════════════════════════════════════════

    private static final Map<String, double[]> CITY_COORDS = new HashMap<>();
    static {
        CITY_COORDS.put("lahore",       new double[]{31.5204, 74.3587});
        CITY_COORDS.put("karachi",      new double[]{24.8607, 67.0011});
        CITY_COORDS.put("islamabad",    new double[]{33.6844, 73.0479});
        CITY_COORDS.put("rawalpindi",   new double[]{33.6007, 73.0679});
        CITY_COORDS.put("peshawar",     new double[]{34.0151, 71.5249});
        CITY_COORDS.put("quetta",       new double[]{30.1798, 66.9750});
        CITY_COORDS.put("multan",       new double[]{30.1575, 71.5249});
        CITY_COORDS.put("faisalabad",   new double[]{31.4504, 73.1350});
        CITY_COORDS.put("gujranwala",   new double[]{32.1877, 74.1945});
        CITY_COORDS.put("sialkot",      new double[]{32.4945, 74.5229});
        CITY_COORDS.put("hyderabad",    new double[]{25.3960, 68.3578});
        CITY_COORDS.put("sukkur",       new double[]{27.7052, 68.8574});
        CITY_COORDS.put("abbottabad",   new double[]{34.1688, 73.2215});
        CITY_COORDS.put("muzaffarabad", new double[]{34.3500, 73.4667});
        CITY_COORDS.put("gilgit",       new double[]{35.9220, 74.3085});
        CITY_COORDS.put("swat",         new double[]{35.2227, 72.4258});
    }

    private double[] geocodeLocation(String location) {
        if (location == null || location.isBlank())
            return randomPakistanCoord();
        String lower = location.toLowerCase();
        for (Map.Entry<String, double[]> e : CITY_COORDS.entrySet()) {
            if (lower.contains(e.getKey())) {
                double[] base = e.getValue();
                // Add small random offset so pins don't stack exactly
                return new double[]{
                        base[0] + (Math.random() - 0.5) * 0.08,
                        base[1] + (Math.random() - 0.5) * 0.08
                };
            }
        }
        return randomPakistanCoord();
    }

    private double[] randomPakistanCoord() {
        return new double[]{
                24 + Math.random() * 13,   // lat 24–37
                61 + Math.random() * 16    // lon 61–77
        };
    }

    // ═══════════════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════════════

    private Color severityColor(int level) {
        if (level >= 9) return new Color(194, 30, 30);
        if (level >= 7) return new Color(214, 80, 0);
        if (level >= 4) return new Color(180, 140, 0);
        return new Color(25, 140, 70);
    }

    private void loadDefaultSampleData() {
        // Sample data shown before user loads real DB data
        String[][] sampleVictims = {
                {"Ali Hassan",    "Lahore",     "9", "Flood"},
                {"Sara Ahmed",    "Karachi",    "5", "Earthquake"},
                {"Tariq Malik",   "Peshawar",   "8", "Landslide"},
                {"Zara Khan",     "Rawalpindi", "3", "Fire"},
                {"Usman Ali",     "Multan",     "7", "Flood"},
        };
        for (String[] v : sampleVictims) {
            double[] c = geocodeLocation(v[1]);
            int sev = Integer.parseInt(v[2]);
            MapMarker m = new MapMarker(c[0], c[1], "V",
                    v[0] + "\nSeverity: " + v[3] + "\nDisaster: " + v[3]
                            + "\nLocation: " + v[1], severityColor(sev), MarkerType.VICTIM);
            victims.add(m);
        }

        String[][] sampleShelters = {
                {"Central Relief Camp", "Lahore",    "500"},
                {"Emergency Shelter A", "Karachi",   "300"},
                {"NDMA Camp 1",         "Islamabad", "200"},
        };
        for (String[] s : sampleShelters) {
            double[] c = geocodeLocation(s[1]);
            MapMarker m = new MapMarker(c[0], c[1], "S",
                    s[0] + "\n" + s[1] + "\nCapacity: " + s[2]
                            + "\nStatus: OPEN\n🏥Med 🍲Food 💧Water",
                    new Color(25, 140, 70), MarkerType.SHELTER);
            m.capacity = Integer.parseInt(s[2]);
            shelters.add(m);
        }

        String[][] sampleTeams = {
                {"Alpha Rescue",  "Lahore",   "En-Route"},
                {"Bravo Medical", "Karachi",  "Available"},
                {"Delta Ops",     "Peshawar", "En-Route"},
        };
        for (String[] t : sampleTeams) {
            double[] c = geocodeLocation(t[1]);
            MapMarker m = new MapMarker(c[0], c[1], "T",
                    t[0] + "\nStatus: " + t[2] + "\nLocation: " + t[1],
                    new Color(214, 110, 0), MarkerType.TEAM);
            teams.add(m);
        }
    }

    public void stopTimers() {
        if (animTimer != null)       animTimer.stop();
        if (simulationTimer != null) simulationTimer.stop();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // INNER TYPES
    // ═══════════════════════════════════════════════════════════════════════

    public enum MarkerType { VICTIM, SHELTER, TEAM }

    public static class MapMarker {
        double lat, lon;
        String label, tooltip;
        Color  color;
        MarkerType type;
        int    id, capacity, occupied;

        MapMarker(double lat, double lon, String label, String tooltip, Color color, MarkerType type) {
            this.lat = lat; this.lon = lon; this.label = label;
            this.tooltip = tooltip; this.color = color; this.type = type;
        }
    }
}
