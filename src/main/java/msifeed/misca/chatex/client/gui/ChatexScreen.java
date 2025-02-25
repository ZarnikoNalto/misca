package msifeed.misca.chatex.client.gui;

import msifeed.mellow.FocusState;
import msifeed.mellow.MellowScreen;
import msifeed.mellow.render.RenderUtils;
import msifeed.mellow.view.button.ButtonIcon;
import msifeed.mellow.view.button.ButtonLabel;
import msifeed.mellow.view.text.TextInput;
import msifeed.mellow.view.text.backend.AutoCompleter;
import msifeed.misca.Misca;
import msifeed.misca.MiscaConfig;
import msifeed.misca.chatex.ChatexRpc;
import msifeed.misca.chatex.client.TypingState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ITabCompleter;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import vazkii.quark.base.network.message.MessageRequestEmote;
import vazkii.arl.network.NetworkHandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ChatexScreen extends MellowScreen implements ITabCompleter {
    private static final int MAX_MSG_BYTES = 20000;
    private static final List<String> EMOTE_NAME_LIST = Arrays.asList(Misca.getSharedConfig().chat.emotesList);

    private final ChatexHud hud = (ChatexHud) Minecraft.getMinecraft().ingameGUI.getChatGUI();
    private final TextInput input = new TextInput();
    private final ResizeHandle resizer = new ResizeHandle();
    private final AutoCompleter autoCompleter = new AutoCompleter(input.getBackend());
    private final ButtonLabel emotesButton = new ButtonLabel(I18n.format("quark.gui.emotes"));

    private int historyCursor = 0;
    private String historyInputBuffer = "";
    private static boolean emotesVisible = false;
    private List<ButtonIcon> emoteButtons = new LinkedList<>();

    public ChatexScreen(String text) {
        input.getBackend().setMaxLines(100);
        input.setSize(width, RenderUtils.lineHeight());
        input.getTextOffset().setPos(2, 3);
        input.insert(text);

        emotesButton.setCallback(this::toggleEmotes);

        for (String emote : EMOTE_NAME_LIST) {
            final ButtonIcon emoteButton = new ButtonIcon(new ResourceLocation("quark", "textures/emotes/" + emote + ".png"), 16, 16);
            emoteButton.setVisible(emotesVisible);
            emoteButton.setCallback(() -> performEmote(emote));
            container.addView(emoteButton);
            emoteButtons.add(emoteButton);
        }

        container.addView(input);
        container.addView(resizer);
        container.addView(emotesButton);
    }

    @Override
    public void initGui() {
        super.initGui();

        Keyboard.enableRepeatEvents(true);

        final int inputWidth = width - 10;
        final int inputLineHeight = RenderUtils.lineHeight() + input.getRenderPref().gap;
        final int inputHeight = inputLineHeight * input.getBackend().getLineCount() + 3;
        input.setPos(5, height - inputHeight - 5, 1);
        input.setSize(inputWidth, inputHeight);
        input.getBackend().getView().setSize(inputWidth, inputHeight);
        FocusState.INSTANCE.setFocus(input);

        resizer.getScreenSize().set(width, height);
        resizer.resetPos();

        int emotesX = width - 79;
        int emotesY = height - inputLineHeight - 30;
        int emoteRowCount = 0;
        final int emoteIconSize = 24;
        final int emoteMaxRowCount = 3;

        emotesButton.setPos(emotesX, emotesY, 1);
        emotesButton.setSize(74, 20);

        emotesY -= emoteIconSize + 1;

        for (ButtonIcon emoteButton : emoteButtons) {
            emoteButton.setSize(emoteIconSize, emoteIconSize);
            emoteButton.setPos(emotesX + emoteRowCount * (emoteIconSize + 1), emotesY, 1);
            emoteRowCount++;

            if (emoteRowCount >= emoteMaxRowCount) {
                emoteRowCount = 0;
                emotesY -= emoteIconSize + 1;
            }
        }
    }

    @Override
    public void closeGui() {
        Keyboard.enableRepeatEvents(false);
        ConfigManager.sync(Misca.MODID, Config.Type.INSTANCE); // Save chat size
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        final ITextComponent hovered = hud.getChatComponent(Mouse.getX(), Mouse.getY());
        if (hovered != null && hovered.getStyle().getHoverEvent() != null) {
            handleComponentHover(hovered, mouseX, mouseY);
        }
    }

    @Override
    protected void setText(String newChatText, boolean shouldOverwrite) {
        if (shouldOverwrite)
            input.clear();
        input.insert(newChatText);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        if (!FocusState.INSTANCE.getFocus().isPresent())
            FocusState.INSTANCE.setFocus(input);
    }

    @Override
    protected void handleWheel(int mouseX, int mouseY) {
        if (Mouse.hasWheel()) {
            hud.scroll(MathHelper.clamp(Mouse.getDWheel(), -5, 5));
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            final ITextComponent tc = hud.getChatComponent(Mouse.getX(), Mouse.getY());
            if (tc != null && this.handleComponentClick(tc))
                return;
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char c, int key) {
        final int linesBefore = input.getBackend().getLineCount();
        final boolean shiftPressed = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        final boolean singleLine = input.getBackend().getLineCount() == 1;

        switch (key) {
            case Keyboard.KEY_ESCAPE:
                Minecraft.getMinecraft().displayGuiScreen(null);
                return;
            case Keyboard.KEY_RETURN:
            case Keyboard.KEY_NUMPADENTER:
                if (shiftPressed) inputKey(c, key);
                else commitMessage();
                break;
            case Keyboard.KEY_UP:
                if (singleLine || shiftPressed) setHistoryMessage(1);
                else inputKey(c, key);
                break;
            case Keyboard.KEY_DOWN:
                if (singleLine || shiftPressed) setHistoryMessage(-1);
                else inputKey(c, key);
                break;
            case Keyboard.KEY_TAB:
                final boolean prev = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
                if (prev)
                    autoCompleter.completePrev();
                else
                    autoCompleter.completeNext();
                break;
            default:
                inputKey(c, key);
        }

        final int linesAfter = input.getBackend().getLineCount();
        if (linesAfter != linesBefore)
            initGui();
    }

    private void inputKey(char c, int key) {
        if (input.onKeyboard(c, key)) {
            historyCursor = 0;
            autoCompleter.reset();
            TypingState.notifyTyping();
        }
    }

    private void commitMessage() {
        final String text = input.getBackend().getLines().collect(Collectors.joining("\n  ")).trim();
        if (text.getBytes().length > MAX_MSG_BYTES) return;

        input.clear();
        if (text.isEmpty()) return;

        if (text.startsWith("/")) {
            sendCommand(text);
        } else {
            ChatexRpc.sendSpeech(text);
        }

        hud.addToSentMessages(text);
        historyCursor = 0;

        if (!MiscaConfig.client.saveChatScrollState) {
            hud.resetScroll();
        }

        if (MiscaConfig.client.closeChatAfterMessage) {
            Minecraft.getMinecraft().displayGuiScreen(null);
        }
    }

    private void sendCommand(String msg) {
        msg = net.minecraftforge.event.ForgeEventFactory.onClientSendMessage(msg);
        if (msg.isEmpty()) return;
        if (ClientCommandHandler.instance.executeCommand(mc.player, msg) != 0) return;

        ChatexRpc.sendCommand(msg);
    }

    private void setHistoryMessage(int cursorDelta) {
        final List<String> history = hud.getSentMessages();
        final int cursor = MathHelper.clamp(historyCursor + cursorDelta, 0, history.size());
        if (historyCursor == cursor) return;

        if (historyCursor == 0) {
            historyInputBuffer = input.getText().trim();
        }

        final String text = cursor == 0 ? historyInputBuffer : history.get(history.size() - cursor);

        input.clear();
        for (String line : text.split("\\R", -1)) {
            if (!input.isEmpty())
                input.insert("\n");
            input.insert(line.trim());
        }

        historyCursor = cursor;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void setCompletions(String... newCompletions) {
        autoCompleter.setCompletions(newCompletions);
        final boolean prev = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        if (prev)
            autoCompleter.completePrev();
        else
            autoCompleter.completeNext();
    }

    private void toggleEmotes() {
        emotesVisible = !emotesVisible;

        for (ButtonIcon emoteButton : emoteButtons) {
            emoteButton.setVisible(emotesVisible);
        }
    }

    private void performEmote(String emote) {
        NetworkHandler.INSTANCE.sendToServer(new MessageRequestEmote(emote));
    }
}
