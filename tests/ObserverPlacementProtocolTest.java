import xyz.kisde.kisdetweaks.util.ObserverPlacementProtocol;

public class ObserverPlacementProtocolTest {
    public static void main(String[] args) {
        for (int facing = 0; facing < 6; facing++) {
            int encoded = ObserverPlacementProtocol.encode(facing);
            // Carpet 1.12 observers decode via EnumFacing.byIndex(hitX - 2).
            if (Math.abs((encoded - 2) % 6) != facing || encoded < 2 || encoded > 7)
                throw new AssertionError("Observer direction changed: " + encoded);
        }
        System.out.println("Observer placement protocol: all six directions passed");
    }
}
