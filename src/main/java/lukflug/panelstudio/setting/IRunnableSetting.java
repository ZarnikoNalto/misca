package lukflug.panelstudio.setting;

import com.lukflug.panelstudio.setting.ISetting;

public interface IRunnableSetting extends ISetting<Runnable> {
    Runnable getValue();

    void setValue (Runnable callback);

    @Override
    default Runnable getSettingState() {
        return getValue();
    }

    @Override
    default Class<Runnable> getSettingClass() {
        return Runnable.class;
    }

    default void run () { getValue().run(); }
}
