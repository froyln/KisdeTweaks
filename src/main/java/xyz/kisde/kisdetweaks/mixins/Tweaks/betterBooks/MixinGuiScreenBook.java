package xyz.kisde.kisdetweaks.mixins.Tweaks.betterBooks;

import java.util.List;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.kisde.kisdetweaks.config.Configs;
import xyz.kisde.kisdetweaks.util.BookEditor;
import xyz.kisde.kisdetweaks.util.BookEditor.Line;

@Mixin(GuiScreenBook.class)
public abstract class MixinGuiScreenBook extends GuiScreen {
    @Shadow @Final private boolean bookIsUnsigned;
    @Shadow private boolean bookGettingSigned;
    @Shadow private int currPage;
    @Shadow private NBTTagList bookPages;
    @Shadow private boolean bookIsModified;
    @Shadow private int updateCount;

    @Unique private final BookEditor kisdetweaks$editor = new BookEditor();
    @Unique private int kisdetweaks$page = -1;

    @Unique private String kisdetweaks$currentPage() {
        return bookPages != null && currPage >= 0 && currPage < bookPages.tagCount()
                ? bookPages.getStringTagAt(currPage) : "";
    }

    @Unique private boolean kisdetweaks$editing() {
        return Configs.Generic.BETTER_BOOKS_ENABLED.getBooleanValue() && bookIsUnsigned && !bookGettingSigned;
    }

    @Unique private void kisdetweaks$sync() {
        String text = kisdetweaks$currentPage();
        if (kisdetweaks$page != currPage || !kisdetweaks$editor.text.equals(text)) {
            kisdetweaks$editor.reset(text);
            kisdetweaks$page = currPage;
        }
    }

    @Unique private boolean kisdetweaks$allowed(String text) {
        // Preserve vanilla's page limits and its cursor-height allowance.
        return text.length() < 256 && fontRenderer.getWordWrappedHeight(text + "\u00a70_", 118) <= 128;
    }

    @Inject(method = "keyTyped(CI)V", at = @At("HEAD"), cancellable = true)
    private void kisdetweaks$editPage(char typedChar, int keyCode, CallbackInfo ci) {
        if (!Configs.Generic.BETTER_BOOKS_ENABLED.getBooleanValue() || bookGettingSigned) return;
        if (!bookIsUnsigned) {
            if (GuiScreen.isKeyComboCtrlC(keyCode)) {
                GuiScreen.setClipboardString(kisdetweaks$currentPage());
                ci.cancel();
            }
            return;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) return;
        kisdetweaks$sync();
        BookEditor editor = kisdetweaks$editor;
        boolean shift = GuiScreen.isShiftKeyDown();
        boolean ctrl = GuiScreen.isCtrlKeyDown();
        if (GuiScreen.isKeyComboCtrlA(keyCode)) {
            editor.move(0, false);
            editor.move(editor.text.length(), true);
        } else if (GuiScreen.isKeyComboCtrlC(keyCode) || GuiScreen.isKeyComboCtrlX(keyCode)) {
            GuiScreen.setClipboardString(editor.selected());
            if (GuiScreen.isKeyComboCtrlX(keyCode)) editor.replace("", this::kisdetweaks$allowed);
        } else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
            String pasted = GuiScreen.getClipboardString().replace("\r\n", "\n").replace('\r', '\n');
            StringBuilder clean = new StringBuilder();
            for (char c : pasted.toCharArray()) {
                if (c == '\n' || ChatAllowedCharacters.isAllowedCharacter(c)) clean.append(c);
            }
            editor.replace(clean.toString(), this::kisdetweaks$allowed);
        } else if (keyCode == Keyboard.KEY_LEFT || keyCode == Keyboard.KEY_RIGHT) {
            editor.horizontal(keyCode == Keyboard.KEY_LEFT ? -1 : 1, shift, ctrl);
        } else if (keyCode == Keyboard.KEY_UP || keyCode == Keyboard.KEY_DOWN) {
            editor.vertical(keyCode == Keyboard.KEY_UP ? -1 : 1, shift, fontRenderer::getStringWidth);
        } else if (keyCode == Keyboard.KEY_HOME || keyCode == Keyboard.KEY_END) {
            List<Line> lines = editor.lines(fontRenderer::getStringWidth);
            Line line = lines.get(editor.line(lines));
            editor.move(keyCode == Keyboard.KEY_HOME ? (ctrl ? 0 : line.start)
                    : (ctrl ? editor.text.length() : line.end), shift);
        } else if (keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_DELETE) {
            editor.delete(keyCode == Keyboard.KEY_BACK ? -1 : 1, ctrl, this::kisdetweaks$allowed);
        } else if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            editor.replace("\n", this::kisdetweaks$allowed);
        } else if (!ctrl && ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
            editor.replace(Character.toString(typedChar), this::kisdetweaks$allowed);
        }
        if (!editor.text.equals(kisdetweaks$currentPage()) && bookPages != null
                && currPage >= 0 && currPage < bookPages.tagCount()) {
            bookPages.set(currPage, new NBTTagString(editor.text));
            bookIsModified = true;
        }
        ci.cancel();
    }

    @Redirect(method = "drawScreen", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/FontRenderer;drawSplitString(Ljava/lang/String;IIII)V"))
    private void kisdetweaks$drawEditor(FontRenderer font, String text, int x, int y, int wrapWidth, int color) {
        if (!kisdetweaks$editing()) {
            font.drawSplitString(text, x, y, wrapWidth, color);
            return;
        }
        kisdetweaks$sync();
        BookEditor editor = kisdetweaks$editor;
        List<Line> lines = editor.lines(font::getStringWidth);
        int cursorRow = editor.line(lines);
        for (int row = 0; row < lines.size(); row++) {
            Line line = lines.get(row);
            int top = y + row * font.FONT_HEIGHT;
            int start = Math.max(line.start, editor.start());
            int end = Math.min(line.end, editor.end());
            if (start < end) {
                int left = x + font.getStringWidth(editor.text.substring(line.start, start));
                int right = x + font.getStringWidth(editor.text.substring(line.start, end));
                drawRect(left, top - 1, right, top + font.FONT_HEIGHT, 0x806699FF);
            }
            font.drawString(editor.text.substring(line.start, line.end), x, top, color);
            if (row == cursorRow && updateCount / 6 % 2 == 0) {
                int caret = x + font.getStringWidth(editor.text.substring(line.start, Math.min(editor.cursor, line.end)));
                drawRect(caret, top - 1, caret + 1, top + font.FONT_HEIGHT - 1, 0xFF000000);
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void kisdetweaks$placeCaret(int mouseX, int mouseY, int button, CallbackInfo ci) {
        int x = (width - 192) / 2 + 36;
        if (button != 0 || !kisdetweaks$editing() || mouseX < x || mouseX > x + 116
                || mouseY < 34 || mouseY >= 162) return;
        kisdetweaks$sync();
        List<Line> lines = kisdetweaks$editor.lines(fontRenderer::getStringWidth);
        int row = Math.min(lines.size() - 1, (mouseY - 34) / fontRenderer.FONT_HEIGHT);
        kisdetweaks$editor.move(kisdetweaks$editor.atX(lines.get(row), mouseX - x,
                fontRenderer::getStringWidth), GuiScreen.isShiftKeyDown());
        ci.cancel();
    }
}
