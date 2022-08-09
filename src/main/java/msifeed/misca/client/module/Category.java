package msifeed.misca.client.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.lukflug.panelstudio.setting.ICategory;
import com.lukflug.panelstudio.setting.IClient;
import com.lukflug.panelstudio.setting.IModule;
import net.minecraft.client.resources.I18n;

public enum Category implements ICategory {
	CHARACTER(I18n.format("gui.misca.character")),
	ROLLS(I18n.format("gui.misca.rolls"));

	public final String displayName;
	public final List<Module> modules=new ArrayList<Module>();

	Category (String displayName) {
		this.displayName=displayName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public Stream<IModule> getModules() {
		return modules.stream().map(module->module);
	}

	public static IClient getClient() {
		return new IClient() {
			@Override
			public Stream<ICategory> getCategories() {
				return Arrays.stream(Category.values());
			}
		};
	}
}
