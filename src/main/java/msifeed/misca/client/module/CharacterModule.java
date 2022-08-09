package msifeed.misca.client.module;

import msifeed.misca.charsheet.ICharsheet;
import msifeed.misca.charsheet.cap.CharsheetProvider;
import msifeed.misca.charsheet.cap.CharsheetSync;
import lukflug.panelstudio.setting.RunnableSetting;
import lukflug.panelstudio.setting.StringSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;

public class CharacterModule extends Module {
	private static ICharsheet charsheet;
	private static EntityPlayer target;

	private static final StringSetting name = new StringSetting(I18n.format("gui.misca.character.name"), null, I18n.format("gui.misca.character.name.description"), ()->true, "");
	private static final RunnableSetting save = new RunnableSetting(I18n.format("gui.misca.character.save"), null, I18n.format("gui.misca.character.save.description"), ()->true, CharacterModule::save);

	public CharacterModule() {
		super(I18n.format("gui.misca.character.general"),I18n.format("gui.misca.character.general.description"),()->true,false);
		settings.add(name);
		settings.add(save);
	}

	public void setTarget(EntityPlayer player) {
		target = player;
		charsheet = CharsheetProvider.get(target);
		name.setValue(charsheet.getName());
	}

	private static void save() {
		charsheet.setName(name.getValue());
		CharsheetSync.post(target, charsheet);
		Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}
}
