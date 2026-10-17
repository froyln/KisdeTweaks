package xyz.kisde.kisdetweaks.util;

public final class ObserverPlacementProtocol {
    private ObserverPlacementProtocol() {}

    public static int encode(int facing) {
        return facing + 2;
    }
}
