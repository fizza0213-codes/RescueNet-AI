package bst;

import models.Victim;
import java.util.ArrayList;
import java.util.List;

/**
 * Binary Search Tree for O(log n) victim lookup by ID.
 * Supports insert, search, delete, range search, filter, and traversals.
 */
public class VictimBST {
    private VictimNode root;
    private int size;

    public VictimBST() { root = null; size = 0; }

    // ── INSERT ──────────────────────────────────────────────
    public void insert(Victim v) {
        if (search(v.getVictimId()) == null) {
            root = insertRec(root, v);
            size++;
        } else {
            root = updateRec(root, v); // update existing
        }
    }

    private VictimNode insertRec(VictimNode node, Victim v) {
        if (node == null) return new VictimNode(v);
        if (v.getVictimId() < node.victim.getVictimId())
            node.left  = insertRec(node.left,  v);
        else if (v.getVictimId() > node.victim.getVictimId())
            node.right = insertRec(node.right, v);
        updateNodeHeight(node);
        return node;
    }

    private VictimNode updateRec(VictimNode node, Victim v) {
        if (node == null) return null;
        if (v.getVictimId() == node.victim.getVictimId()) {
            node.victim = v;
        } else if (v.getVictimId() < node.victim.getVictimId()) {
            node.left = updateRec(node.left, v);
        } else {
            node.right = updateRec(node.right, v);
        }
        return node;
    }

    // ── SEARCH by ID ────────────────────────────────────────
    public Victim search(int id) { return searchRec(root, id); }

    private Victim searchRec(VictimNode node, int id) {
        if (node == null) return null;
        if (id == node.victim.getVictimId()) return node.victim;
        return id < node.victim.getVictimId()
                ? searchRec(node.left, id) : searchRec(node.right, id);
    }

    // ── DELETE ──────────────────────────────────────────────
    public boolean delete(int id) {
        if (search(id) == null) return false;
        root = deleteRec(root, id);
        size--;
        return true;
    }

    private VictimNode deleteRec(VictimNode node, int id) {
        if (node == null) return null;
        if (id < node.victim.getVictimId())
            node.left  = deleteRec(node.left,  id);
        else if (id > node.victim.getVictimId())
            node.right = deleteRec(node.right, id);
        else {
            if (node.left  == null) return node.right;
            if (node.right == null) return node.left;
            VictimNode min = findMin(node.right);
            node.victim = min.victim;
            node.right  = deleteRec(node.right, min.victim.getVictimId());
        }
        updateNodeHeight(node);
        return node;
    }

    private VictimNode findMin(VictimNode n) {
        while (n.left != null) n = n.left;
        return n;
    }

    // ── RANGE SEARCH ────────────────────────────────────────
    public List<Victim> rangeSearch(int minId, int maxId) {
        List<Victim> result = new ArrayList<>();
        rangeRec(root, minId, maxId, result);
        return result;
    }

    private void rangeRec(VictimNode node, int minId, int maxId, List<Victim> result) {
        if (node == null) return;
        if (node.victim.getVictimId() > minId) rangeRec(node.left, minId, maxId, result);
        if (node.victim.getVictimId() >= minId && node.victim.getVictimId() <= maxId)
            result.add(node.victim);
        if (node.victim.getVictimId() < maxId) rangeRec(node.right, minId, maxId, result);
    }

    // ── FILTER BY STATUS ────────────────────────────────────
    public List<Victim> filterByStatus(String status) {
        List<Victim> result = new ArrayList<>();
        filterStatusRec(root, status.toUpperCase(), result);
        return result;
    }

    private void filterStatusRec(VictimNode n, String status, List<Victim> result) {
        if (n == null) return;
        filterStatusRec(n.left, status, result);
        if (n.victim.getStatus().equalsIgnoreCase(status)) result.add(n.victim);
        filterStatusRec(n.right, status, result);
    }

    // ── FILTER BY SEVERITY >= threshold ─────────────────────
    public List<Victim> filterBySeverity(int minSeverity) {
        List<Victim> result = new ArrayList<>();
        filterSeverityRec(root, minSeverity, result);
        return result;
    }

    private void filterSeverityRec(VictimNode n, int min, List<Victim> result) {
        if (n == null) return;
        filterSeverityRec(n.left, min, result);
        if (n.victim.getSeverityLevel() >= min) result.add(n.victim);
        filterSeverityRec(n.right, min, result);
    }

    // ── TRAVERSALS ──────────────────────────────────────────
    public List<Victim> inorder() {
        List<Victim> list = new ArrayList<>();
        inorderRec(root, list);
        return list;
    }

    private void inorderRec(VictimNode n, List<Victim> list) {
        if (n == null) return;
        inorderRec(n.left, list);
        list.add(n.victim);
        inorderRec(n.right, list);
    }

    public List<Victim> preorder() {
        List<Victim> list = new ArrayList<>();
        preorderRec(root, list);
        return list;
    }

    private void preorderRec(VictimNode n, List<Victim> list) {
        if (n == null) return;
        list.add(n.victim);
        preorderRec(n.left, list);
        preorderRec(n.right, list);
    }

    // ── STATS ───────────────────────────────────────────────
    public int height()         { return heightRec(root); }
    public int size()           { return size; }
    public VictimNode root()    { return root; }
    public boolean isEmpty()    { return root == null; }
    public void clear()         { root = null; size = 0; }

    private int heightRec(VictimNode n) {
        if (n == null) return 0;
        return 1 + Math.max(heightRec(n.left), heightRec(n.right));
    }

    private void updateNodeHeight(VictimNode n) {
        if (n != null) n.height = 1 + Math.max(heightRec(n.left), heightRec(n.right));
    }

    public String getStats() {
        int optH = size > 0 ? (int)(Math.log(size + 1) / Math.log(2)) + 1 : 0;
        String balance = height() <= optH + 2 ? "Balanced" : "Unbalanced";
        return String.format("Nodes: %d | Height: %d | State: %s", size, height(), balance);
    }

    /** Build string representation for visualization panel */
    public String buildVisualString() {
        if (root == null) return "(empty BST)";
        StringBuilder sb = new StringBuilder();
        buildVisualRec(root, "", true, sb);
        return sb.toString();
    }

    private void buildVisualRec(VictimNode node, String prefix, boolean isLeft, StringBuilder sb) {
        if (node == null) return;
        sb.append(prefix);
        sb.append(isLeft ? "├── " : "└── ");
        sb.append("[").append(node.victim.getVictimId()).append("] ")
          .append(node.victim.getName()).append(" (S:")
          .append(node.victim.getSeverityLevel()).append(")\n");
        buildVisualRec(node.left,  prefix + (isLeft ? "│   " : "    "), true, sb);
        buildVisualRec(node.right, prefix + (isLeft ? "│   " : "    "), false, sb);
    }
}
