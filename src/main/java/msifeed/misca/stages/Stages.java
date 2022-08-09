package msifeed.misca.stages;

import com.google.gson.reflect.TypeToken;
import msifeed.misca.Misca;
import msifeed.misca.charsheet.CharNeed;
import msifeed.misca.charsheet.ICharsheet;
import msifeed.misca.charsheet.cap.CharsheetProvider;
import msifeed.misca.charstate.cap.CharstateProvider;
import msifeed.misca.charstate.cap.CharstateSync;
import msifeed.misca.charstate.cap.ICharstate;
import msifeed.misca.regions.RegionControl;
import msifeed.sys.sync.SyncChannel;
import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.event.GameStageEvent;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.ArrayList;
import java.util.Map;

public class Stages {
    public static final SyncChannel<StagesConfig> STAGES
            = new SyncChannel<>(Misca.RPC, "stages.json", TypeToken.get(StagesConfig.class));

    public static void init() {
        MinecraftForge.EVENT_BUS.register(Stages.class);
    }

    public static void sync() throws Exception {
        STAGES.sync();
    }

    public static StagesConfig config() {
        return STAGES.get();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player.world.isRemote) return;

        StageCharacter.syncGameStages(event.player);
    }

    @SubscribeEvent
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        final IBlockState blockState = event.getWorld().getBlockState(event.getPos());
        final Block block = blockState.getBlock();
        final int meta = block.getMetaFromState(blockState);
        final String registryName = block.getRegistryName().toString();
        final String[] stages = config().getStages(registryName, meta);

        if (stages != null) {
            final EntityPlayer player = event.getEntityPlayer();
            if (!GameStageHelper.hasAnyOf(player, stages)) {
                final ArrayList<String> fancyStages = new ArrayList<>();

                for (String stage : stages) {
                    fancyStages.add(I18n.translateToLocal("enum.misca.stage." + stage));
                }

                player.sendStatusMessage(new TextComponentTranslation("hud.misca.no_craft", String.join(", ", fancyStages)), true);
                event.setUseBlock(Event.Result.DENY);
            }
        }
    }
}
