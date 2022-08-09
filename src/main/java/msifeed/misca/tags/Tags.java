package msifeed.misca.tags;

import com.google.common.collect.Multimap;
import com.google.gson.reflect.TypeToken;
import msifeed.misca.Misca;
import msifeed.misca.charsheet.CharsheetConfig;
import msifeed.sys.sync.SyncChannel;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import java.util.*;

public class Tags {
    public static final SyncChannel<TagsConfig> TAGS
            = new SyncChannel<>(Misca.RPC, "tags.json", TypeToken.get(TagsConfig.class));

    final private static HashMap<ResourceLocation, EnumSet<ItemTags>> configTagsCache = new HashMap<>();

    public static void init() {
        MinecraftForge.EVENT_BUS.register(Tags.class);
    }

    public static void sync() throws Exception {
        TAGS.sync();
    }

    public static TagsConfig config() {
        return TAGS.get();
    }

    public static ItemTagsData get(ItemStack stack) {
        final Double speed = getItemAttackSpeed(stack);
        final ResourceLocation registryName = stack.getItem().getRegistryName();

        if (!configTagsCache.containsKey(registryName)) {
            EnumSet<ItemTags> set = Tags.TAGS.get().getTags(registryName);

            if (set != null) {
                configTagsCache.put(registryName, set);
            }
        }

        final EnumSet<ItemTags> configTags = configTagsCache.get(registryName);
        EnumSet<ItemTags> nbtTags = EnumSet.noneOf(ItemTags.class);
        final NBTTagCompound nbtTagCompound = stack.getTagCompound();

        if (nbtTagCompound != null && nbtTagCompound.hasKey("tags")) {
            for (NBTBase base : nbtTagCompound.getTagList("tags", 8)) {
                NBTTagString tag = (NBTTagString) base;

                nbtTags.add(ItemTags.valueOf(tag.getString()));
            };
        }

        ItemTagsData data = new ItemTagsData();
        data.speed = getSpeedTag(nbtTags, configTags, speed);
        data.hands = getHandsTag(nbtTags, configTags, speed);
        data.shield = getShieldTag(stack, nbtTags, configTags);
        data.skills = getSkillTags(nbtTags, configTags);
        data.other = getOtherTags(nbtTags, configTags);

        return data;
    }

    private static Double getItemAttackSpeed(ItemStack stack) {
        final Multimap<String, AttributeModifier> modifiers = stack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);
        final Collection<AttributeModifier> attribute = modifiers.get(SharedMonsterAttributes.ATTACK_SPEED.getName());

        if (!attribute.isEmpty()) {
            final AttributeModifier attributeModifier = attribute.stream().findFirst().get();
            return 4 + Math.round(attributeModifier.getAmount() * 100.0) / 100.0;
        }

        return null;
    }

    private static ItemTags getSpeedTag(EnumSet<ItemTags> nbtTags, EnumSet<ItemTags> configTags, Double speed) {
        ItemTags speedTag = null;

        if (nbtTags != null) {
            for (ItemTags tag : nbtTags) {
                if (tag.isSpeed()) {
                    speedTag = tag;
                    break;
                }
            }
        }

        if (speedTag == null && configTags != null) {
            for (ItemTags tag : configTags) {
                if (tag.isSpeed()) {
                    speedTag = tag;
                    break;
                }
            }
        }

        if (speedTag == null && speed != null) {
            final CharsheetConfig config = Misca.getSharedConfig().charsheet;
            if (speed > config.veryFastSpeed) {
                speedTag = ItemTags.veryfast;
            } else if (speed > config.fastSpeed) {
                speedTag = ItemTags.fast;
            } else if (speed > config.normalSpeed) {
                speedTag = ItemTags.normal;
            } else if (speed > config.slowSpeed) {
                speedTag = ItemTags.slow;
            } else {
                speedTag = ItemTags.veryslow;
            }
        }

        return speedTag;
    }

    private static ItemTags getHandsTag(EnumSet<ItemTags> nbtTags, EnumSet<ItemTags> configTags, Double speed) {
        ItemTags handsTag = null;

        if (nbtTags != null) {
            for (ItemTags tag : nbtTags) {
                if (tag.isHand()) {
                    handsTag = tag;
                    break;
                }
            }
        }

        if (handsTag == null && configTags != null) {
            for (ItemTags tag : configTags) {
                if (tag.isHand()) {
                    handsTag = tag;
                    break;
                }
            }
        }

        if (handsTag == null && speed != null) {
            handsTag = speed <= 1.25 ? ItemTags.twohanded : ItemTags.onehanded;
        }

        return handsTag;
    }

    private static ItemTags getShieldTag(ItemStack stack, EnumSet<ItemTags> nbtTags, EnumSet<ItemTags> configTags) {
        ItemTags shieldTag = null;

        if (nbtTags != null) {
            for (ItemTags tag : nbtTags) {
                if (tag.isShield()) {
                    shieldTag = tag;
                    break;
                }
            }
        }

        if (shieldTag == null && configTags != null) {
            for (ItemTags tag : configTags) {
                if (tag.isShield()) {
                    shieldTag = tag;
                    break;
                }
            }
        }

        if (shieldTag == null && stack.getItem() instanceof ItemShield) {
            shieldTag = ItemTags.shield;
        }

        return shieldTag;
    }

    private static List<ItemTags> getSkillTags(EnumSet<ItemTags> nbtTags, EnumSet<ItemTags> configTags) {
        List<ItemTags> skillTags = new LinkedList<>();

        if (nbtTags != null) {
            for (ItemTags tag : nbtTags) {
                if (tag.isSkill()) {
                    skillTags.add(tag);
                }
            }
        }

        if (skillTags.isEmpty() && configTags != null) {
            for (ItemTags tag : configTags) {
                if (tag.isSkill()) {
                    skillTags.add(tag);
                }
            }
        }

        return skillTags;
    }

    private static List<ItemTags> getOtherTags(EnumSet<ItemTags> nbtTags, EnumSet<ItemTags> configTags) {
        List<ItemTags> otherTags = new LinkedList<>();

        if (nbtTags != null) {
            for (ItemTags tag : nbtTags) {
                if (tag.isOther()) {
                    otherTags.add(tag);
                }
            }
        }

        if (otherTags.isEmpty() && configTags != null) {
            for (ItemTags tag : configTags) {
                if (tag.isOther()) {
                    otherTags.add(tag);
                }
            }
        }

        return otherTags;
    }
}
