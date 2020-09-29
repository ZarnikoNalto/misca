package msifeed.misca.genesis;

import msifeed.misca.genesis.blocks.BlockRule;
import msifeed.misca.genesis.items.ItemRule;
import msifeed.misca.genesis.rules.IGenesisRule;
import msifeed.misca.genesis.rules.RuleLoader;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

public class Genesis {
    private File genesisDir;

    public void preInit() {
        genesisDir = new File(Loader.instance().getConfigDir(), "genesis");
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        load(BlockRule.class, new File(genesisDir, "blocks"));
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        load(ItemRule.class, new File(genesisDir, "items"));
    }

    private void load(Class<? extends IGenesisRule> ruleType, File directory) {
        if (!directory.exists())
            directory.mkdirs();

        final RuleLoader loader = new RuleLoader(ruleType);
        loader.load(directory);
    }
}
