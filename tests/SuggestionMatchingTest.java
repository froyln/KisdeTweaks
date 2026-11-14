import java.util.Arrays;
import java.util.List;
import xyz.kisde.kisdetweaks.util.SuggestionMatching;
import xyz.kisde.kisdetweaks.util.SuggestionMatching.Mode;

public class SuggestionMatchingTest {
    public static void main(String[] args) {
        List<String> values = Arrays.asList("minecraft:stone_bricks", "minecraft:redstone_block", "minecraft:stone",
                "minecraft:glowstone", "minecraft:stonebrick", "minecraft:wooden_button");
        List<String> strict = SuggestionMatching.filter(values, "stone", Mode.STRICT, false, "");
        assert strict.contains("minecraft:stone");
        assert !strict.contains("minecraft:glowstone");
        List<String> loose = SuggestionMatching.filter(values, "stone", Mode.SLIGHTLY_LOOSE, false, "");
        assert loose.indexOf("minecraft:stone") < loose.indexOf("minecraft:glowstone");
        assert loose.contains("minecraft:redstone_block");
        assert SuggestionMatching.filter(values, "red_block", Mode.LOOSE, false, "").equals(Arrays.asList("minecraft:redstone_block"));
        assert SuggestionMatching.filter(values, "rdstblk", Mode.VERY_LOOSE, false, "").equals(Arrays.asList("minecraft:redstone_block"));
        assert SuggestionMatching.filter(values, "rdstblk", Mode.LOOSE, false, "").isEmpty();
        assert SuggestionMatching.filter(values, "STONE", Mode.LOOSE, true, "").isEmpty();
        assert !SuggestionMatching.filter(values, "STONE", Mode.LOOSE, false, "").isEmpty();
        assert SuggestionMatching.filter(Arrays.asList("pack:_hidden/test", "pack:path/_hidden", "pack:path/test"),
                "", Mode.LOOSE, false, "_").equals(Arrays.asList("pack:path/test"));
        assert SuggestionMatching.rank("/gamemode", "mode", false) == 1;
        assert SuggestionMatching.rank("\"CustomName\":", "Cus", true) == 0;

        ChatStateTest.run();
        System.out.println("SuggestionMatchingTest passed");
    }

    private static class ChatStateTest {
        static void run() {
            xyz.kisde.kisdetweaks.util.ChatCompletion state = new xyz.kisde.kisdetweaks.util.ChatCompletion();
            state.configure(Mode.LOOSE, false, "", false);
            state.update("/mode", 5, true);
            assert "/".equals(state.request());
            state.receive(new String[] {"/gamemode", "/time", "/model", "/custommodel"}, true);
            assert state.suggestions().get(0).equals("/model");
            assert state.suggestions().contains("/gamemode");
            assert state.suggestions().size() == 3;
            assert state.request() == null;
            state.update("/give @p stone", 14, false);
            assert "/give @p ".equals(state.request());
            state.receive(new String[] {"minecraft:stone", "minecraft:glowstone"}, false);
            assert state.suggestions().size() == 2;
            assert "/give @p minecraft:stone".equals(state.apply(false));
            state.update("/give @p glow", 13, false);
            assert state.request() == null;
            assert "/give @p minecraft:glowstone".equals(state.apply(false));
        }
    }
}
