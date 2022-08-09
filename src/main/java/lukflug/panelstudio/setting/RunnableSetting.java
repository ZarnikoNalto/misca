package lukflug.panelstudio.setting;

import com.lukflug.panelstudio.base.IBoolean;

public class RunnableSetting extends Setting<Runnable> implements IRunnableSetting {
    public RunnableSetting(String displayName, String configName, String description, IBoolean visible, Runnable value) {
        super(displayName, configName, description, visible, value);
    }
}
