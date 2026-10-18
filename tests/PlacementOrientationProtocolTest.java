import xyz.kisde.kisdetweaks.util.PlacementOrientationProtocol;

public class PlacementOrientationProtocolTest {
    public static void main(String[] args) {
        for (int facing = 0; facing < 6; facing++) {
            int encoded = PlacementOrientationProtocol.encode(facing, 0);
            if (Math.abs((encoded - 2) % 6) != facing || encoded < 2 || encoded > 7)
                throw new AssertionError("Six-way direction changed: " + encoded);
        }
        for (int facing = 2; facing < 6; facing++) {
            for (int properties : new int[] {0, 10, 20, 30}) {
                int encoded = PlacementOrientationProtocol.encode(facing, properties);
                if (encoded % 10 - 2 != facing || encoded / 10 != properties / 10)
                    throw new AssertionError("Horizontal direction or properties changed: " + encoded);
            }
        }
        System.out.println("Placement orientation: six-way facing, horizontal facing and property checks passed");
    }
}
