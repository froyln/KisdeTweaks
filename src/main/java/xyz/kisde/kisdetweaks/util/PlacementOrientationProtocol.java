package xyz.kisde.kisdetweaks.util;

public final class PlacementOrientationProtocol {
    private PlacementOrientationProtocol() {}

    public static int encode(int facing, int properties) {
        return facing + 2 + properties;
    }
}
