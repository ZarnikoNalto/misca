package msifeed.misca.client.module;

import msifeed.misca.charsheet.CharSkill;
import msifeed.misca.charsheet.ICharsheet;
import msifeed.misca.charsheet.cap.CharsheetProvider;
import lukflug.panelstudio.setting.RunnableSetting;
import lukflug.panelstudio.setting.StringSetting;
import msifeed.misca.rolls.RollRpc;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;
import java.util.Map;

public class SkillsModule extends Module {
	private static ICharsheet charsheet;
	private static EntityPlayer target;

	private static final StringSetting mod = new StringSetting(I18n.format("gui.misca.rolls.mod"), null, I18n.format("gui.misca.rolls.mod.description"), ()->true, "");
	private static final HashMap<CharSkill, RunnableSetting> skills = new HashMap();

	public SkillsModule() {
		super(I18n.format("gui.misca.rolls.skills"),I18n.format("gui.misca.rolls.skills.description"),()->true,false);
		settings.add(mod);

		for(CharSkill skill : CharSkill.values()) {
			final RunnableSetting skillSetting = new RunnableSetting(skill.tr(), null, skill.description(), ()->true, ()->roll(skill));
			skills.put(skill, skillSetting);
			settings.add(skillSetting);
		}
	}

	public void setTarget(EntityPlayer player) {
		target = player;
		charsheet = CharsheetProvider.get(target);

		for (Map.Entry<CharSkill, RunnableSetting> entry : skills.entrySet()) {
			final CharSkill skill = entry.getKey();
			final int value = charsheet.skills().get(skill);
			entry.getValue().displayName = String.format("%s %d", skill.tr(), value);
		}
	}

	private static void roll(CharSkill skill) {
		String modText = mod.getValue();

		if (!modText.matches("([+-]).*")) {
			modText = "+" + modText;
		}

		RollRpc.doSkillRoll(skill, GuiScreen.isShiftKeyDown() ? 1 : GuiScreen.isCtrlKeyDown() ? -1 : 0, modText);
	}
}
