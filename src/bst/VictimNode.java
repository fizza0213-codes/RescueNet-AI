package bst;

import models.Victim;

public class VictimNode {
    public Victim victim;
    public VictimNode left;
    public VictimNode right;
    public int height;

    public VictimNode(Victim victim) {
        this.victim = victim;
        this.left   = null;
        this.right  = null;
        this.height = 1;
    }
}
