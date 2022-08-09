package msifeed.misca.charsheet;

import msifeed.misca.Misca;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Objects;
import java.util.UUID;

public class AttributesFlow {

    public static void updateAttributes(EntityPlayer player) {
        setMaxHealth(player);
    }

    private static void setMaxHealth(EntityPlayer player) {
        final IAttributeInstance maxHealth = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        final UUID uuid = UUID.fromString("84dea84b-860c-4eb6-b6c5-3d380619dbb5");
        final double mod = Math.max(CharSkill.fitness.get(player), CharSkill.composure.get(player))
                * Misca.getSharedConfig().charsheet.healthPerFitnessComposure;
        final AttributeModifier skillIncrease =
                new AttributeModifier(uuid, "Increase health based on character's skills", mod, 0);

        if (!maxHealth.hasModifier(skillIncrease)
                || Objects.requireNonNull(maxHealth.getModifier(uuid)).getAmount()!= skillIncrease.getAmount()) {
            maxHealth.removeModifier(skillIncrease);
            maxHealth.applyModifier(skillIncrease);
            player.setHealth(player.getMaxHealth());
        }
    }
}
