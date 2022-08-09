package msifeed.misca.client;

import msifeed.misca.MiscaConfig;
import msifeed.misca.client.module.*;
import msifeed.misca.client.module.CharacterModule;
import msifeed.misca.client.module.SkillsModule;
import msifeed.misca.combat.client.GuiCombatOverlay;
import msifeed.misca.tags.TagsTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

@SideOnly(Side.CLIENT)
public enum MiscaClient {
    INSTANCE;

    public static KeyBinding guiKey = new KeyBinding("key.misca.gui", KeyConflictContext.IN_GAME,  Keyboard.KEY_I, "key.categories.misca");

    private static ScreenGUI gui;

    private static final CharacterModule characterModule = new CharacterModule();
    private static final SkillsModule skillsModule = new SkillsModule();

    public void preInit() {
        Display.setTitle(MiscaConfig.client.windowTitle);
    }

    public void init() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(GuiCombatOverlay.class);
        MinecraftForge.EVENT_BUS.register(TagsTooltip.class);

        MiscaTheme.load();
        ClientRegistry.registerKeyBinding(guiKey);

        Category.CHARACTER.modules.add(characterModule);
        Category.ROLLS.modules.add(skillsModule);
        gui = new ScreenGUI();
    }

    @SubscribeEvent
    void onKeyTyped(InputEvent.KeyInputEvent event) {
        if (guiKey.isPressed()) {
            final EntityPlayer target = Minecraft.getMinecraft().player;
            characterModule.setTarget(target);
            skillsModule.setTarget(target);

            gui.enterGUI();
        }
    }
}
