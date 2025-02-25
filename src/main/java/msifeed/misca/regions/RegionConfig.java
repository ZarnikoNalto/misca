package msifeed.misca.regions;

import msifeed.misca.charsheet.CharNeed;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionConfig extends HashMap<Integer, List<RegionConfig.Region>> {
    @Nullable
    public Region get(int dim, String name) {
        final List<Region> regions = this.get(dim);
        if (regions == null) return null;

        for (Region r : regions) {
            if (r.name.equals(name)) return r;
        }

        return null;
    }

    public void add(int dim, Region region) {
        this.computeIfAbsent(dim, integer -> new ArrayList<>())
                .add(region);
    }

    public void delete(int dim, String name) {
        final List<Region> regions = this.get(dim);
        if (regions != null)
            regions.removeIf(r -> r.name.equals(name));
    }

    public static class Region {
        public String name = "";
        public @Nullable AxisAlignedBB aabb;
        public Map<CharNeed, Double> needs = new HashMap<>();
        public double toleranceMod = 0;
        public List<Class<?>> blacklist = new ArrayList<>();
        public List<Class<?>> whitelist = new ArrayList<>();
        public List<String> stages = new ArrayList<>();

        public boolean contains(Vec3d pos) {
            return aabb == null || aabb.contains(pos);
        }
    }
}
