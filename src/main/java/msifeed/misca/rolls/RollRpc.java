package msifeed.misca.rolls;

import msifeed.misca.Misca;
import msifeed.misca.charsheet.CharSkill;
import msifeed.misca.charsheet.CharsheetConfig;
import msifeed.misca.charsheet.ICharsheet;
import msifeed.misca.charsheet.cap.CharsheetProvider;
import msifeed.misca.chatex.ChatexRpc;
import msifeed.misca.rolls.dice.Dice;
import msifeed.misca.rolls.dice.DiceRoll;
import msifeed.misca.tags.ItemTags;
import msifeed.misca.tags.ItemTagsData;
import msifeed.misca.tags.Tags;
import msifeed.sys.rpc.RpcContext;
import msifeed.sys.rpc.RpcMethodHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RollRpc {
    private static final String skillDice = "roll.skillDice";
    private static final String showDefense = "roll.showDefense";

    @RpcMethodHandler(skillDice)
    public void onSkillDice(RpcContext ctx, int skillOrd, int advantage, String mod) {
        final EntityPlayerMP sender = ctx.getServerHandler().player;
        final ICharsheet sheet = CharsheetProvider.get(sender);

        CharSkill skill = null;
        int skillValue = 0;

        if (skillOrd != -1) {
            skill = CharSkill.values()[skillOrd];
            skillValue = sheet.skills().get(skill);
        }

        final CharsheetConfig config = Misca.getSharedConfig().charsheet;
        final Dice dice = new Dice(config.baseDiceCount, config.baseDiceSides);
        dice.setName(I18n.translateToLocal("misca.roll.d20"));

        if (advantage == 1) {
            dice.advantage();
        } else if (advantage == -1) {
            dice.disadvantage();
        }

        final DiceRoll diceRoll = new DiceRoll(dice);

        if (skill != null && skillValue > 0) {
            diceRoll.addMod(skillValue, skill.tr());
        }

        diceRoll.setName((skill != null ? skill.tr() : I18n.translateToLocal("misca.roll.base")) +
                (advantage != 0 ? (" (" + I18n.translateToLocal(advantage == 1 ? "misca.roll.advantage" : "misca.roll.disadvantage") + ")") : ""));

        diceRoll.critCondition = roll -> {
            final Dice firstDice = roll.getFirstDice();

            if (firstDice != null) {
                return firstDice.isCrit();
            }

            return false;
        };

        if (!mod.isEmpty()) {
            try {
                final DiceRoll modRoll = Dices.parseThrow(mod, true);
                modRoll.roll();
                diceRoll.addRoll(modRoll);
            } catch (Exception e) {}
        }

        diceRoll.roll();

        ChatexRpc.broadcastDiceRoll(sender, diceRoll);
    }

    @RpcMethodHandler(showDefense)
    public void onShowDefense(RpcContext ctx, String mod) {
        final EntityPlayerMP sender = ctx.getServerHandler().player;
        final ICharsheet sheet = CharsheetProvider.get(sender);
        final CharsheetConfig config = Misca.getSharedConfig().charsheet;

        final DiceRoll diceRoll = new DiceRoll();
        diceRoll.setName(I18n.translateToLocal("misca.defense.title"));
        diceRoll.addMod(config.baseDefenseClass, I18n.translateToLocal("misca.defense.base"));

        final int armor = sender.getTotalArmorValue();

        if (armor > config.armorPerDefensePenalty) {
            diceRoll.addMod(-armor / config.armorPerDefensePenalty, I18n.translateToLocal("misca.defense.armorpenalty"));
        }

        final ItemStack itemMainHand = sender.getHeldItemMainhand();
        final ItemStack itemOffhand = sender.getHeldItemOffhand();
        ItemTagsData tags = Tags.get(itemMainHand);

        if (tags.shield == null) {
            tags = Tags.get(itemOffhand);
        }

        if (tags.shield != null) {
            final boolean isTowerShield = tags.shield == ItemTags.towershield;
            diceRoll.addMod(isTowerShield ? config.towerShieldBonus : config.shieldBonus,
                    I18n.translateToLocal("misca.defense." + (isTowerShield ? "towershield" : "shield")));
        }

        if (!mod.isEmpty()) {
            try {
                final DiceRoll modRoll = Dices.parseThrow(mod, true);
                modRoll.roll();
                diceRoll.addRoll(modRoll);
            } catch (Exception e) {}
        }

        diceRoll.roll();

        ChatexRpc.broadcastDiceRoll(sender, diceRoll);
    }

    @SideOnly(Side.CLIENT)
    public static void doSkillRoll(CharSkill skill, int advantage, String mod) {
        Misca.RPC.sendToServer(skillDice, skill != null ? skill.ordinal() : -1, advantage, mod);
    }

    @SideOnly(Side.CLIENT)
    public static void doShowDefense(String mod) {
        Misca.RPC.sendToServer(showDefense, mod);
    }
}
