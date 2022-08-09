package msifeed.misca.charstate;

import net.minecraft.entity.player.EntityPlayer;

public class CharstateConfig {
    public double globalMiningSpeedModifier = 0.5;

    public double integrityRestPerSec = 0.000016;
    public double integrityCostPerDamage = 0.1;

    public double sanityRestPerFood = 1;
    public double sanityRestPerSpeechChar = 0.001;
    public double sanityCostPerSec = 0.000027;
    public double sanityCostPerSecInDarkness = 0.000055;
    public double sanityCostPerDamage = 0.1;
    public double sanityLevelToRegenStamina = 100;
    public double sanityDebuffToRestThreshold = 0.75;

    public double staminaRestPerSec = 0.0014;
    public long staminaRestMiningTimeoutSec = 2;
    public double staminaRestPerSpeechChar = 0.0005;
    public double staminaMiningSlowdownThreshold = 0.25;
    public double staminaCostPerMiningTick = 0.01;
    public double staminaCostPerIngredient = 0.5;
    public int craftMaxIngredientsOfOneType = 4;

    public double sanitySilenceToLostSec = 3600;
    public double sanityLostPerSecInSilence = 0.00005;

    public float fitnessIntegrityLostFactor = -0.05f;
    public float fitnessStaminaLostFactor = -0.05f;
    public float composureSanityLostFactor = -0.05f;
    public float coordinationSkillCraftCostFactor = -0.015f;
    public float fitnessSkillMiningSpeedFactor = 0.11f;

    public double charismaSanityGainFactor = 0.05;
    public double charismaStaminaGainFactor = 0.005;

    public double ingenuitySkillFreeCraftChance = 0.03;
    public double ingenuitySkillRestoreIngredientChance = 0.015;

    public int foodEffectThreshold = 16;
    public float foodNeedsRestMod = 0.25f;
    public float foodExhaustionMod = 0.25f;

    public float foodRestMod(EntityPlayer player) {
        final float food = player.getFoodStats().getFoodLevel();
        return food > foodEffectThreshold ? foodNeedsRestMod : 0;
    }

    public float exhaustionMod(EntityPlayer player) {
        final float food = player.getFoodStats().getFoodLevel();
        return food > foodEffectThreshold ? foodExhaustionMod : 0;
    }

    public double toleranceGainPerPoint = 0.2;
    public double toleranceLostBasePerSec = 0.00027;
    public double toleranceLostMinPerSec = 0.00005;

    public double getToleranceLost(double tolerance) {
        return Math.max(toleranceLostBasePerSec * (1 - tolerance), toleranceLostMinPerSec);
    }
}
