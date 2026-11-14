import xyz.kisde.kisdetweaks.util.ChatCompletion;

public class ChatCompletionTest {
    public static void main(String[] args) {
        ChatCompletion state = new ChatCompletion();
        state.update("/", 1, false);
        assert "/".equals(state.request());
        String[] commands = new String[20];
        for (int i = 0; i < commands.length; i++) commands[i] = "/command" + i;
        java.util.Arrays.sort(commands);
        state.receive(commands, false);
        assert state.suggestions().size() == 20;
        for (int i = 0; i < 20; i++) {
            assert commands[i].equals(state.apply(false));
            assert state.firstVisible() == i / 8 * 8;
        }
        assert commands[0].equals(state.apply(false));
        assert commands[19].equals(state.apply(true));
        assert state.request() == null;

        state.update("/command1", 9, false);
        assert state.suggestions().size() == 11; // Immediate prefix filtering, before a reply.

        state.update("/give player di suffix", 15, false);
        assert "/give player di".equals(state.request());
        state.receive(new String[] {"diamond", "dirt", "diamond", ""}, false);
        assert state.suggestions().size() == 2;
        assert "/give player diamond suffix".equals(state.apply(false));
        assert "/give player dirt suffix".equals(state.apply(false));
        assert state.cursor() == 17;

        state.update("/a", 2, false);
        assert "/a".equals(state.request());
        state.update("/ab", 3, false);
        assert state.request() == null; // Only one untagged 1.12 request in flight.
        state.receive(new String[] {"/a"}, false);
        assert state.suggestions().isEmpty();
        assert "/ab".equals(state.request());
        state.receive(new String[] {"/abc"}, false);
        assert "/abc".equals(state.apply(false));
        state.update("/a", 2, false);
        assert state.request() == null; // Cached response is immediate.
        assert "/a".equals(state.apply(false));

        state.update("/c", 2, true);
        assert state.suggestions().contains("/calc");
        state.request();
        state.receive(new String[0], true);
        assert state.suggestions().contains("/calc");
        state.update("/calc it", 8, true);
        assert state.suggestions().contains("itemtosb");
        assert state.suggestions().contains("itemtostack");
        state.update("/cal", 4, false);
        assert state.suggestions().isEmpty();
        state.update("hello", 5, true);
        assert state.request() == null;
        assert state.suggestions().isEmpty();
        System.out.println("ChatCompletionTest passed");
    }
}
