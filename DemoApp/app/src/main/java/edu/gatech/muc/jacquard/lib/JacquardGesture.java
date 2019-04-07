package edu.gatech.muc.jacquard.lib;

public enum JacquardGesture {
    DOUBLE_TAP(0x01),
    BRUSH_IN(0x02),
    BRUSH_OUT(0x03),
    COVER(0x07),
    SCRATCH(0x08),
    UNKNOWN(-1);

    private int id;

    JacquardGesture(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }

    public static JacquardGesture findByID(int id) {
        for (JacquardGesture gesture : JacquardGesture.values()) {
            if (gesture.id == id) {
                return gesture;
            }
        }
        return UNKNOWN;
    }
}
