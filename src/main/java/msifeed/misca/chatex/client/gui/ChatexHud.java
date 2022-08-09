package msifeed.misca.chatex.client.gui;

import msifeed.misca.MiscaConfig;
import msifeed.misca.client.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.text.ITextComponent;

public class ChatexHud extends GuiNewChat {
    private final Minecraft mc = Minecraft.getMinecraft();

    public ChatexHud() {
        super(Minecraft.getMinecraft());
    }

    @Override
    public boolean getChatOpen() {
        return mc.currentScreen instanceof ChatexScreen;
    }

    @Override
    public int getChatWidth() {
        return MiscaConfig.client.chatSize.x;
    }

    @Override
    public int getChatHeight() {
        return MiscaConfig.client.chatSize.y;
    }

    @Override
    public void clearChatMessages(boolean clearSent) {
    }

    @Override
    public void printChatMessage(ITextComponent chatComponent) {
        super.printChatMessageWithOptionalDeletion(chatComponent, 0);

        if (!MiscaConfig.client.saveChatScrollState) {
            if (!getChatOpen()) {
                this.resetScroll();
            }
        }
    }
}
