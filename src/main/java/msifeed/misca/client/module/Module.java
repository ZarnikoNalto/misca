package msifeed.misca.client.module;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.lukflug.panelstudio.setting.ICategory;
import lukflug.panelstudio.setting.Setting;
import com.lukflug.panelstudio.base.IBoolean;
import com.lukflug.panelstudio.base.IToggleable;
import com.lukflug.panelstudio.setting.IModule;
import com.lukflug.panelstudio.setting.ISetting;

public class Module implements IModule {
	public final String displayName,description;
	public final IBoolean visible;
	public final List<Setting<?>> settings=new ArrayList<Setting<?>>();
	public final boolean toggleable;
	public ICategory category;
	private boolean enabled=false;
	
	public Module (String displayName, String description, IBoolean visible, boolean toggleable) {
		this.displayName=displayName;
		this.description=description;
		this.visible=visible;
		this.toggleable=toggleable;
	}
	
	@Override
	public String getDisplayName() {
		return displayName;
	}
	
	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public IBoolean isVisible() {
		return visible;
	}

	@Override
	public IToggleable isEnabled() {
		if (!toggleable) return null;
		return new IToggleable() {
			@Override
			public boolean isOn() {
				return enabled;
			}

			@Override
			public void toggle() {
				enabled=!enabled;
			}
		};
	}

	@Override
	public Stream<ISetting<?>> getSettings() {
		return settings.stream().filter(setting->setting instanceof ISetting).map(setting->(ISetting<?>)setting);
	}

	public void setCategory(ICategory category) {
		this.category = category;
	}
}
