package msifeed.misca.regions;

import com.google.gson.reflect.TypeToken;
import msifeed.misca.charsheet.CharNeed;
import msifeed.misca.charsheet.ICharsheet;
import msifeed.misca.charsheet.cap.CharsheetProvider;
import msifeed.misca.stages.StageCharacter;
import msifeed.sys.sync.JsonConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegionControl {
    private static final JsonConfig<RegionConfig> config = new JsonConfig<>("regions.json", TypeToken.get(RegionConfig.class));

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new RegionHandler());
    }

    public static void preInit() {
        MinecraftForge.EVENT_BUS.register(RegionControl.class);
    }

    public static RegionConfig config() {
        return config.get();
    }

    public static void sync() throws Exception {
        config.sync();
    }

    public static void writeConfig() throws IOException {
        config.writeFile();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntitySpawn(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityPlayer) return;
        if (!(event.getEntity() instanceof EntityLivingBase)) return;

        if (isBlocked(event.getWorld(), (EntityLivingBase) event.getEntity())) {
            event.setCanceled(true);
        }
    }

    public static Stream<RegionConfig.Region> getLocalRules(World world, Vec3d pos) {
        final int dim = world.provider.getDimension();
        final List<RegionConfig.Region> regions = config.get().get(dim);
        if (regions == null || regions.isEmpty()) return Stream.empty();

        return regions.stream().filter(r -> r.contains(pos));
    }

    public static Map<CharNeed, Double> getLocalEffects(EntityPlayer player) {
        return getLocalRules(player.world, player.getPositionVector())
                .flatMap(r -> r.needs.entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingDouble(Map.Entry::getValue)));
    }

    public static double getLocalToleranceMod(EntityPlayer player) {
        return getLocalRules(player.world, player.getPositionVector())
                .mapToDouble(r -> r.toleranceMod)
                .sum();
    }

    public static List<String> getLocalStages(EntityPlayer player) {
        return getLocalRules(player.world, player.getPositionVector())
                .flatMap(r -> r.stages.stream())
                .collect(Collectors.toList());
    }

    private static boolean isBlocked(World world, EntityLivingBase entity) {
        final int dim = world.provider.getDimension();
        final List<RegionConfig.Region> regions = config.get().get(dim);
        if (regions == null || regions.isEmpty()) return false;

        final Class<?> ec = entity.getClass();
        for (RegionConfig.Region r : regions) {
            if (!r.contains(entity.getPositionVector())) continue;
            for (Class<?> c : r.whitelist) {
                if (c.isAssignableFrom(ec))
                    return false;
            }
            for (Class<?> c : r.blacklist) {
                if (c.isAssignableFrom(ec))
                    return true;
            }
        }

        return false;
    }
}
