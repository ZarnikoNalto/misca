package msifeed.misca;

import msifeed.misca.combat.ItemCombatTool;
import msifeed.misca.supplies.ItemSuppliesInvoice;
import msifeed.misca.tools.ItemDebugTool;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;

//@GameRegistry.ObjectHolder(Misca.MODID)
public enum  MiscaThings {
    INSTANCE;

//    @GameRegistry.ObjectHolder(ItemSuppliesInvoice.ID)
    public static final ItemSuppliesInvoice invoice = new ItemSuppliesInvoice();

    public static void init() {
        MinecraftForge.EVENT_BUS.register(INSTANCE);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        final Item[] items = {
                invoice,
                new ItemDebugTool(),
                new ItemCombatTool()
        };

        for (Item i : items) {
            event.getRegistry().register(i);

            if (FMLCommonHandler.instance().getSide().isClient()) {
                ModelLoader.setCustomModelResourceLocation(i, 0,
                        new ModelResourceLocation(Objects.requireNonNull(i.getRegistryName()), "inventory"));
            }
        }
    }
}
