package realtime;

import database.VictimDAO;
import database.ShelterDAO;
import database.RescueTeamDAO;
import database.ResourceDAO;

import javax.swing.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * RescueNet AI — Real-Time Data Engine
 *
 * Polls the database every N seconds and fires callbacks on change.
 * Also generates simulated live disaster events for demo/testing.
 *
 * Pattern: Observable / Listener
 * Thread-safe: all callbacks dispatched on EDT via SwingUtilities.invokeLater
 */
public class RealTimeDataEngine {

    // ── Poll interval ─────────────────────────────────────────────────────────
    public static final int POLL_INTERVAL_SEC = 15;

    // ── DAOs ──────────────────────────────────────────────────────────────────
    private final VictimDAO    victimDAO    = new VictimDAO();
    private final ShelterDAO   shelterDAO   = new ShelterDAO();
    private final RescueTeamDAO teamDAO     = new RescueTeamDAO();
    private final ResourceDAO  resourceDAO  = new ResourceDAO();

    // ── Snapshot cache ────────────────────────────────────────────────────────
    private int lastVictimCount   = -1;
    private int lastShelterCount  = -1;
    private int lastTeamCount     = -1;
    private int lastResourceCount = -1;

    // ── Listeners ─────────────────────────────────────────────────────────────
    private final List<Consumer<DataSnapshot>> dataListeners  = new CopyOnWriteArrayList<>();
    private final List<Consumer<AlertEvent>>   alertListeners = new CopyOnWriteArrayList<>();

    // ── Executor ──────────────────────────────────────────────────────────────
    private ScheduledExecutorService executor;
    private boolean running = false;

    // ── Simulation ────────────────────────────────────────────────────────────
    private final Random rand = new Random();
    private final String[] DISASTER_TYPES = {"Flood","Earthquake","Landslide","Fire","Cyclone","Drought"};
    private final String[] CITIES = {"Lahore","Karachi","Islamabad","Peshawar","Quetta","Multan","Swat"};
    private final String[] ALERT_MSGS = {
            "⚠ New victims reported in %s — dispatch teams immediately",
            "🚨 CRITICAL: Shelter capacity exceeded in %s",
            "🔴 Rescue team BRAVO reporting distress in %s",
            "⚡ Power outage affecting relief operations in %s",
            "🌊 Flash flood warning issued for %s and surrounding areas",
            "📡 NDMA has upgraded disaster level to RED in %s",
            "🏥 Medical supplies critically low at %s relief camp",
    };

    // ── Singleton ─────────────────────────────────────────────────────────────
    private static RealTimeDataEngine instance;
    public static synchronized RealTimeDataEngine getInstance() {
        if (instance == null) instance = new RealTimeDataEngine();
        return instance;
    }

    private RealTimeDataEngine() {}

    // ═══════════════════════════════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════════════════════════════

    public void addDataListener(Consumer<DataSnapshot> l)  { dataListeners.add(l); }
    public void addAlertListener(Consumer<AlertEvent> l)   { alertListeners.add(l); }
    public void removeDataListener(Consumer<DataSnapshot> l)  { dataListeners.remove(l); }
    public void removeAlertListener(Consumer<AlertEvent> l)   { alertListeners.remove(l); }

    public synchronized void start() {
        if (running) return;
        running = true;
        executor = Executors.newScheduledThreadPool(2);

        // DB poll
        executor.scheduleAtFixedRate(this::pollDatabase,
                0, POLL_INTERVAL_SEC, TimeUnit.SECONDS);

        // Simulated alert every 20–40 s
        executor.scheduleAtFixedRate(this::fireSimulatedAlert,
                8, 25, TimeUnit.SECONDS);
    }

    public synchronized void stop() {
        if (!running) return;
        running = false;
        if (executor != null) executor.shutdownNow();
    }

    public boolean isRunning() { return running; }

    /** Fire a manual snapshot refresh */
    public void forceRefresh() {
        executor.submit(this::pollDatabase);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // POLLING
    // ═══════════════════════════════════════════════════════════════════════

    private void pollDatabase() {
        try {
            int vc = safeCount(() -> victimDAO.getTotalCount());
            int sc = safeCount(() -> shelterDAO.getOpenCount());
            int tc = safeCount(() -> teamDAO.getAvailableCount());
            int rc = safeCount(() -> resourceDAO.getTotalItems());

            DataSnapshot snap = new DataSnapshot(vc, sc, tc, rc,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

            boolean changed = (vc != lastVictimCount || sc != lastShelterCount
                    || tc != lastTeamCount || rc != lastResourceCount);

            lastVictimCount = vc; lastShelterCount = sc;
            lastTeamCount   = tc; lastResourceCount = rc;

            if (changed) {
                SwingUtilities.invokeLater(() -> dataListeners.forEach(l -> l.accept(snap)));
            }

            // Always fire every interval so the "last updated" ticker works
            SwingUtilities.invokeLater(() -> dataListeners.forEach(l -> l.accept(snap)));

        } catch (Exception e) {
            System.err.println("[RealTimeEngine] Poll error: " + e.getMessage());
        }
    }

    private void fireSimulatedAlert() {
        String city = CITIES[rand.nextInt(CITIES.length)];
        String msg  = String.format(ALERT_MSGS[rand.nextInt(ALERT_MSGS.length)], city);
        AlertEvent.Severity sev = rand.nextInt(10) < 3
                ? AlertEvent.Severity.CRITICAL
                : rand.nextInt(10) < 6
                ? AlertEvent.Severity.WARNING
                : AlertEvent.Severity.INFO;

        AlertEvent alert = new AlertEvent(msg, sev,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                city, DISASTER_TYPES[rand.nextInt(DISASTER_TYPES.length)]);

        SwingUtilities.invokeLater(() -> alertListeners.forEach(l -> l.accept(alert)));
    }

    private int safeCount(Callable0 fn) {
        try { return fn.call(); } catch (Exception e) { return 0; }
    }

    @FunctionalInterface
    interface Callable0 { int call() throws Exception; }

    // ═══════════════════════════════════════════════════════════════════════
    // DATA TYPES
    // ═══════════════════════════════════════════════════════════════════════

    /** Immutable snapshot of all live counts */
    public static class DataSnapshot {
        public final int victims, shelters, teams, resources;
        public final String timestamp;

        public DataSnapshot(int v, int s, int t, int r, String ts) {
            victims = v; shelters = s; teams = t; resources = r; timestamp = ts;
        }
    }

    /** A discrete alert event */
    public static class AlertEvent {
        public enum Severity { INFO, WARNING, CRITICAL }

        public final String   message, timestamp, city, disasterType;
        public final Severity severity;

        public AlertEvent(String msg, Severity sev, String ts, String city, String dt) {
            message = msg; severity = sev; timestamp = ts;
            this.city = city; disasterType = dt;
        }
    }
}