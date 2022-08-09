package msifeed.misca.client.module;

import com.lukflug.panelstudio.theme.IColorScheme;
import com.lukflug.panelstudio.theme.ITheme;
import lukflug.panelstudio.setting.ColorSetting;
import lukflug.panelstudio.setting.EnumSetting;

import java.awt.*;

public class ClickGUIModule extends Module {
	public static final EnumSetting<Theme> theme=new EnumSetting<Theme>("Theme","theme","What theme to use.",()->true,Theme.Impact,Theme.class);

	public ClickGUIModule() {
		super("GUI","Module containing GUI settings.",()->true,false);
		settings.add(theme);
	}
	
	public enum ColorModel {
		RGB,HSB;
	}
	
	public enum Theme {
		Impact;
	}

	public static class ThemeScheme implements IColorScheme {
		private final Theme themeValue;
		private final String themeName;

		public ThemeScheme (Theme themeValue) {
			this.themeValue=themeValue;
			this.themeName=themeValue.toString().toLowerCase();
		}

		@Override
		public void createSetting (ITheme theme, String name, String description, boolean hasAlpha, boolean allowsRainbow, Color color, boolean rainbow) {
			ClickGUIModule.theme.subSettings.add(new ColorSetting(name,themeName+"-"+name,description,()->ClickGUIModule.theme.getValue()==themeValue,hasAlpha,allowsRainbow,color,rainbow));
		}

		@Override
		public Color getColor (String name) {
			return ((ColorSetting)ClickGUIModule.theme.subSettings.stream().filter(setting->setting.configName.equals(themeName+"-"+name)).findFirst().orElse(null)).getValue();
		}
	}
}
