import xyz.kisde.kisdetweaks.util.ChatCompletion;
import xyz.kisde.kisdetweaks.util.NbtSuggestions;
import xyz.kisde.kisdetweaks.util.SuggestionMatching.Mode;

public class NbtSuggestionsTest {
    private static NbtSuggestions.Result atEnd(String text) {
        NbtSuggestions.Result result = NbtSuggestions.complete(text, text.length());
        assert result != null : text;
        return result;
    }

    public static void main(String[] args) {
        assert atEnd("/give @p diamond_sword 1 0 {").candidates.get("display:").equals("compound");
        assert atEnd("/give @p diamond_sword 1 0 {display:{").candidates.get("Lore:").equals("list");
        assert atEnd("/give @p diamond_sword 1 0 {ench:[{").candidates.get("lvl:").equals("short");
        assert atEnd("/give @p diamond_sword 1 0 {ench:[{id:16s,lvl:5s}],").candidates.containsKey("Unbreakable:");
        assert !atEnd("/give @p diamond_sword 1 0 {Unbreakable:1b,").candidates.containsKey("Unbreakable:");
        assert atEnd("/give @p diamond_sword 1 0 {display:{Name:\"a,b} \\\"test\\\"\"},").candidates.containsKey("ench:");
        assert atEnd("/summon zombie ~ ~ ~ {").candidates.containsKey("IsBaby:");
        assert atEnd("/summon item ~ ~ ~ {").candidates.get("Health:").equals("short");
        assert !atEnd("/summon item ~ ~ ~ {").candidates.containsKey("NoAI:");
        assert atEnd("/summon item ~ ~ ~ {Item:{tag:{display:{").candidates.containsKey("Name:");
        assert atEnd("/summon zombie ~ ~ ~ {Passengers:[{").candidates.containsKey("Health:");
        assert atEnd("/entitydata @e[type=creeper] {").candidates.containsKey("powered:");
        assert atEnd("/testfor @e[type=armor_stand] {").candidates.containsKey("Pose:");
        assert atEnd("/setblock ~ ~ ~ chest 0 replace {").candidates.containsKey("Items:");
        assert !atEnd("/setblock ~ ~ ~ chest 0 replace {").candidates.containsKey("Text1:");
        assert atEnd("/blockdata ~ ~ ~ {Items:[{tag:{display:{").candidates.containsKey("Lore:");
        assert atEnd("/fill ~ ~ ~ ~1 ~1 ~1 chest 0 replace {").candidates.containsKey("Items:");
        assert atEnd("/testforblock ~ ~ ~ standing_sign 0 {").candidates.containsKey("Text1:");
        assert atEnd("/replaceitem entity @p slot.hotbar.0 diamond_sword 1 0 {").candidates.containsKey("ench:");
        assert atEnd("/replaceitem block ~ ~ ~ slot.container.0 diamond_sword 1 0 {").candidates.containsKey("ench:");
        assert atEnd("/clear @p diamond_sword 0 1 {").candidates.containsKey("ench:");
        assert atEnd("/execute @p ~ ~ ~ summon zombie ~ ~ ~ {").candidates.containsKey("IsBaby:");
        assert atEnd("/execute @p ~ ~ ~ detect ~ ~-1 ~ stone 0 give @p stone 1 0 {").candidates.containsKey("display:");
        assert atEnd("/give @p potion 1 0 {").candidates.containsKey("Potion:");
        assert !atEnd("/give @p stone 1 0 {").candidates.containsKey("Potion:");
        assert atEnd("/minecraft:give @p minecraft:stone 1 0 {").candidates.containsKey("display:");
        assert NbtSuggestions.complete("/tellraw @p {", 13) == null;
        assert NbtSuggestions.complete("/say {", 6) == null;
        assert NbtSuggestions.complete("/give @p stone {", 16) == null;
        assert atEnd("/give @p stone 1 0 {unknown:{").candidates.isEmpty();
        assert atEnd("/give @p stone 1 0 {display:{Name:\"hello").candidates.isEmpty();
        assert atEnd("/give @p stone 1 0 {display:").candidates.containsKey("{");
        assert atEnd("/give @p stone 1 0 {Unbreakable:").candidates.containsKey("1b");
        assert atEnd("/give @p stone 1 0 {foo:[I;1,2],").candidates.containsKey("display:");

        ChatCompletion state = new ChatCompletion();
        state.configure(Mode.LOOSE, false, "", true);
        String text = "/give @p stone 1 0 {display:{Na}}";
        state.update(text, text.indexOf("Na") + 2, false);
        assert state.request() == null;
        assert state.label(0).equals("Name:  (string)");
        assert state.apply(false).equals("/give @p stone 1 0 {display:{Name:}}");
        text = "/give @p stone 1 0 {\"displ\":{Name:\"saved\"}}";
        state.configure(Mode.STRICT, true, "", true);
        state.update(text, text.indexOf("displ") + 3, false);
        assert state.apply(false).equals("/give @p stone 1 0 {\"display\":{Name:\"saved\"}}");
        text = "/give @p stone 1 0 {displ : {Name:\"saved\"}}";
        state.update(text, text.indexOf("displ") + 3, false);
        assert state.apply(false).equals("/give @p stone 1 0 {display: {Name:\"saved\"}}");
        text = "/give @p stone 1 0 {display:{Name:\"saved\"}}";
        state.update(text, text.indexOf("Name:") + 5, false);
        assert state.suggestions().isEmpty();
        text = "/give @p stone 1 0 {display:{Name:";
        state.update(text, text.length(), false);
        assert state.apply(false).endsWith("Name:\"\"");
        assert state.cursor() == text.length() + 1;
        state.update("/give @p stone 1 0 {", 20, false);
        assert state.suggestions().contains("display:") : state.suggestions();
        state.configure(Mode.STRICT, true, "", false);
        state.update("/give @p stone 1 0 {", 20, false);
        assert state.suggestions().isEmpty();
        assert state.request() != null;
        System.out.println("NbtSuggestionsTest passed");
    }
}
