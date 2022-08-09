package msifeed.misca.tags;

import com.google.common.collect.Multimap;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;
import java.util.stream.Collectors;

public class TagsTooltip {

    final private static HashMap<ResourceLocation, EnumSet<ItemTags>> configTagsCache = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void addAdditionalTooltips(ItemTooltipEvent event) {
        final List<String> tooltip = event.getToolTip();
        final ItemStack stack = event.getItemStack();
        final ItemTagsData tags = Tags.get(stack);

        if (tags.speed != null) {
            tooltip.add(tags.speed.tr());
        }

        if (tags.hands != null) {
            tooltip.add(tags.hands.tr());
        }

        if (tags.shield != null) {
            tooltip.add(tags.shield.tr());
        }

        if (!tags.skills.isEmpty()) {
            tooltip.add(tags.skills.stream().map(tag -> tag.tr()).collect(Collectors.joining(" ")));
        }

        if (!tags.other.isEmpty()) {
            tooltip.add(tags.other.stream().map(tag -> tag.tr()).collect(Collectors.joining(" ")));
        }
    }
}
