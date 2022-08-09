package msifeed.misca.tags;

import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.EnumUtils;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class TagsConfig {
    public HashMap<ResourceLocation, String[]> item_tags = new HashMap<>();

    public EnumSet<ItemTags> getTags(ResourceLocation registryName) {
        List<ItemTags> tags = new LinkedList<>();
        String[] stringTags = item_tags.get(registryName);

        if (stringTags != null) {
            for (String tag : stringTags) {
                if (tag == null)
                    continue;

                tag = tag.toLowerCase();

                if (EnumUtils.isValidEnum(ItemTags.class, tag)) {
                    tags.add(ItemTags.valueOf(tag));
                }
            }

            if (!tags.isEmpty()) {
                return EnumSet.copyOf(tags);
            }
        }

        return null;
    }
}
