package msifeed.misca.stages;

import com.codetaylor.mc.athenaeum.util.ArrayHelper;
import com.google.common.base.Enums;
import msifeed.misca.charsheet.CharCraft;
import msifeed.misca.charsheet.ICharsheet;
import msifeed.misca.charsheet.cap.CharsheetProvider;
import msifeed.misca.regions.RegionControl;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import org.apache.commons.lang3.EnumUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class StageCharacter {
    private static final String[] intToTier = {
            "", "one", "two", "three", //"four", "five", "six", "seven", "eight", "nine", "ten"
    };

    public static void syncGameStages(EntityPlayer player) {
        syncCraftStages(player);
        syncGlobalStages(player);
        GameStageHelper.syncPlayer(player);
    }

    public static void syncRegionStages(EntityPlayer player) {
        final List<String> localStages = RegionControl.getLocalStages(player);
        final HashMap<String, Boolean> globalStages = Stages.config().global_stages;
        final ICharsheet sheet = CharsheetProvider.get(player);
        final HashMap<String, String[]> adjacentStages = Stages.config().adjacent_stages;

        for (String stage : globalStages.keySet()) {
            final String craft = stage.substring(0, 3);
            final String tier = stage.substring(4);

            if (EnumUtils.isValidEnum(CharCraft.class, craft)) {
                final CharCraft key = CharCraft.valueOf(craft);
                final boolean stageValue = localStages.contains(stage) && sheet.crafts().get(key) >= Arrays.asList(intToTier).indexOf(tier);

                setStage(player, stage, stageValue);

                if (adjacentStages.containsKey(stage)) {
                    for (String adjacentStage : adjacentStages.get(stage)) {
                        setStage(player, adjacentStage, localStages.contains(adjacentStage) && stageValue);
                    }
                }
            }
        }

        GameStageHelper.syncPlayer(player);

        if (!localStages.isEmpty()) {
            player.sendStatusMessage(new TextComponentTranslation("hud.misca.workshop", localStages.stream()
                    .map(r -> I18n.translateToLocal("enum.misca.stage." + r))
                    .collect(Collectors.joining(", "))), true);
        }
    }

    private static void syncCraftStages(EntityPlayer player) {
        final ICharsheet sheet = CharsheetProvider.get(player);
        final HashMap<String, String[]> adjacentStages = Stages.config().adjacent_stages;

        for (CharCraft key : CharCraft.values()) {
            final int tier = sheet.crafts().get(key);

            for (int i = 1; i < intToTier.length; i++) {
                final String stage = key.name() + "_" + intToTier[i];
                final boolean stageValue = tier >= i;

                setStage(player, stage, stageValue);

                if (adjacentStages.containsKey(stage) && stageValue) {
                    for (String adjacentStage : adjacentStages.get(stage)) {
                        setStage(player, adjacentStage, true);
                    }
                }
            }
        }
    }

    private static void syncGlobalStages(EntityPlayer player) {
        final HashMap<String, Boolean> globalStages = Stages.config().global_stages;

        for (String stage : globalStages.keySet()) {
            setStage(player, stage, globalStages.get(stage));
        }
    }

    private static void setStage(EntityPlayer player, String stage, boolean value) {
        final boolean hasStage = GameStageHelper.hasStage(player, stage);

        if (value) {
            if (!hasStage) {
                GameStageHelper.addStage(player, stage);
            }
        } else {
            if (hasStage) {
                GameStageHelper.removeStage(player, stage);
            }
        }
    }
}
