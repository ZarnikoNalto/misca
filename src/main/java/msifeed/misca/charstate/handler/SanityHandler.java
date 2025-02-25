package msifeed.misca.charstate.handler;

import msifeed.misca.Misca;
import msifeed.misca.charsheet.CharNeed;
import msifeed.misca.charsheet.CharSkill;
import msifeed.misca.charstate.CharstateConfig;
import msifeed.misca.charstate.cap.CharstateProvider;
import msifeed.misca.combat.CharAttribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public class SanityHandler {
    public static final IAttribute SANITY = new RangedAttribute(null, Misca.MODID + ".sanity", 100, 0, 150).setShouldWatch(true);

    private static final AttributeModifier ATTRIBUTE_PENALTY_1 = new AttributeModifier(UUID.fromString("15bf9288-0473-409f-86cf-499a729e3b46"), "Attribute penalty I", -2, 0).setSaved(false);
    private static final AttributeModifier ATTRIBUTE_PENALTY_2 = new AttributeModifier(UUID.fromString("436866e3-3052-4d93-a54f-bfd7d593f532"), "Attribute penalty II", -4, 0).setSaved(false);
    private static final AttributeModifier ATTRIBUTE_PENALTY_3 = new AttributeModifier(UUID.fromString("a7e0d58e-633a-495e-a68c-b54b6a9134b7"), "Attribute penalty III", -8, 0).setSaved(false);

    private static final AttributeModifier SKILL_PENALTY_1 = new AttributeModifier(UUID.fromString("45d90c52-6ed7-4bb2-8ad1-7d8ba23f84c5"), "Skill penalty I", -1, 0).setSaved(false);
    private static final AttributeModifier SKILL_PENALTY_2 = new AttributeModifier(UUID.fromString("49495e01-77e9-4c1a-8b5c-bab0935bd898"), "Skill penalty II", -2, 0).setSaved(false);

    public void handleTime(EntityPlayer player, long secs, double factorMod) {
        if (player.isCreative() || player.isSpectator()) return;

        // TODO: a way disable darkness for nocturnal players
        final CharstateConfig config = Misca.getSharedConfig().charstate;
        final int light = player.world.getLight(player.getPosition(), false);
        final long passedInSilence = CharstateProvider.get(player).passedInSilence();
        double sanPerSec = config.sanityCostPerSec;

        if (light < 7) {
            sanPerSec = config.sanityCostPerSecInDarkness;
        }

        if (passedInSilence > config.sanitySilenceToLostSec) {
            sanPerSec -= config.sanityLostPerSecInSilence;
        }

        final double factor = Math.max(0, 1 + factorMod + CharSkill.composure.get(player) * config.composureSanityLostFactor);
        final double lost = secs * sanPerSec * factor * CharNeed.SAN.lostFactor(player);

        final IAttributeInstance inst = player.getEntityAttribute(SANITY);
        inst.setBaseValue(SANITY.clampValue(inst.getBaseValue() - lost));

        final double value = inst.getAttributeValue();
        final IAttributeInstance attrMod = player.getEntityAttribute(CharAttribute.MOD);
        final IAttributeInstance skillMod = player.getEntityAttribute(CharSkill.MOD);

        setMod(attrMod, ATTRIBUTE_PENALTY_1, value <= 75 && value > 50);
        setMod(attrMod, ATTRIBUTE_PENALTY_2, value <= 50 && value > 25);
        setMod(attrMod, ATTRIBUTE_PENALTY_3, value <= 25);
        setMod(skillMod, SKILL_PENALTY_1, value <= 50 && value > 25);
        setMod(skillMod, SKILL_PENALTY_2, value <= 25);
    }

    private static void setMod(IAttributeInstance attr, AttributeModifier mod, boolean activate) {
        if (attr.hasModifier(mod)) {
            if (!activate) attr.removeModifier(mod);
        } else if (activate) {
            attr.applyModifier(mod);
        }
    }

    public void handleDamage(EntityPlayer player, float amount) {
        final CharstateConfig config = Misca.getSharedConfig().charstate;
        final double factor = Math.max(0, 1 + CharSkill.composure.get(player) * config.composureSanityLostFactor);
        final double lost = amount * config.sanityCostPerDamage * factor * CharNeed.SAN.lostFactor(player);

        final IAttributeInstance inst = player.getEntityAttribute(SANITY);
        inst.setBaseValue(SANITY.clampValue(inst.getBaseValue() - lost));
    }

    public void handleItemUse(EntityPlayer player, ItemStack stack) {
        if (!(stack.getItem() instanceof ItemFood)) return;

        final ItemFood item = (ItemFood) stack.getItem();
        final CharstateConfig config = Misca.getSharedConfig().charstate;
        final double factor = Math.max(0, 1 + config.foodRestMod(player));
        final double restored = item.getHealAmount(stack) * config.sanityRestPerFood * factor * CharNeed.SAN.gainFactor(player);

        final IAttributeInstance inst = player.getEntityAttribute(SANITY);
        inst.setBaseValue(SANITY.clampValue(inst.getBaseValue() + restored));
    }

    public void handleSpeech(EntityPlayer listener, int chars, double speechMod, double regionMod) {
        final CharstateConfig config = Misca.getSharedConfig().charstate;

        final double factor = Math.max(0, 1 + regionMod + speechMod + config.foodRestMod(listener));
        final double restored = chars * config.sanityRestPerSpeechChar * factor * CharNeed.SAN.gainFactor(listener);

        final IAttributeInstance inst = listener.getEntityAttribute(SANITY);
        inst.setBaseValue(SANITY.clampValue(inst.getBaseValue() + restored));

        CharstateProvider.get(listener).resetSilenceTime();
    }

    /**
     * @return [-1; 0]
     */
    public static double getRestoreDebuffMod(EntityPlayer player) {
        final CharstateConfig config = Misca.getSharedConfig().charstate;
        final double sanity = player.getEntityAttribute(SANITY).getAttributeValue();
        if (sanity >= config.sanityDebuffToRestThreshold) return 0;

        return sanity / config.sanityDebuffToRestThreshold - 1;
    }
}
