package msifeed.misca.regions;

import msifeed.misca.charsheet.ICharsheet;
import msifeed.misca.charsheet.cap.CharsheetProvider;
import msifeed.misca.stages.StageCharacter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class RegionHandler {

    private static final HashMap<EntityPlayer, List<RegionConfig.Region>> playerRegions = new HashMap<>();

    private static final int UPDATE_INTERVAL_SEC = 10;

    @SubscribeEvent
    public void onPlayerTick(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntity() instanceof EntityPlayerMP)) return;
        if (event.getEntity().world.isRemote) return;

        final EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();

        final ICharsheet sheet = CharsheetProvider.get(player);
        final long passedSec = sheet.timeSinceUpdate();
        if (passedSec < UPDATE_INTERVAL_SEC) return;

        final List<RegionConfig.Region> regions = RegionControl.getLocalRules(player.getEntityWorld(), player.getPositionVector())
                .collect(Collectors.toList());

        if (!playerRegions.containsKey(player) || !playerRegions.get(player).equals(regions)) {
            playerRegions.put(player, regions);
            StageCharacter.syncRegionStages(player);
        }

        sheet.setLastUpdated(System.currentTimeMillis() / 1000);
    }
}
