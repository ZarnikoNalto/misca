package msifeed.misca.core;

import net.minecraft.client.gui.GuiChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiChat.class)
public interface GuiChatMixin {
    @Accessor
    String getDefaultInputFieldText();
}