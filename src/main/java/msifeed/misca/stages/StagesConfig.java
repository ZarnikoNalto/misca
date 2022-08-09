package msifeed.misca.stages;

import net.minecraft.util.ResourceLocation;

import java.util.HashMap;

public class StagesConfig {
    private HashMap<ResourceLocation, String[]> block_stage = new HashMap<>();
    public HashMap<String, Boolean> global_stages = new HashMap<>();
    public HashMap<String, String[]> adjacent_stages = new HashMap<>();

    public String[] getStages(String registryName, int meta) {
        String[] stage = block_stage.get(new ResourceLocation(registryName + ":*"));

        if (stage == null) {
            stage = block_stage.get(new ResourceLocation(registryName + ":" + meta));
        }

        if (stage == null && meta == 0) {
            stage = block_stage.get(new ResourceLocation(registryName));
        }

        return stage;
    }
}
