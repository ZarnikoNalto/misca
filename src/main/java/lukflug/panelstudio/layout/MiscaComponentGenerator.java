package lukflug.panelstudio.layout;

import com.lukflug.panelstudio.base.Animation;
import com.lukflug.panelstudio.base.Context;
import com.lukflug.panelstudio.component.IComponent;
import com.lukflug.panelstudio.layout.ComponentGenerator;
import com.lukflug.panelstudio.layout.IComponentAdder;
import com.lukflug.panelstudio.setting.*;
import com.lukflug.panelstudio.theme.ThemeTuple;
import com.lukflug.panelstudio.widget.Button;
import com.lukflug.panelstudio.widget.ITextFieldKeys;
import lukflug.panelstudio.setting.IRunnableSetting;

import java.util.function.IntPredicate;
import java.util.function.Supplier;

public class MiscaComponentGenerator extends ComponentGenerator {

    public MiscaComponentGenerator(IntPredicate keybindKey, IntPredicate charFilter, ITextFieldKeys keys) {
        super(keybindKey, charFilter, keys);
    }

    @Override
    public IComponent getComponent (ISetting<?> setting, Supplier<Animation> animation, IComponentAdder adder, ThemeTuple theme, int colorLevel, boolean isContainer) {
        if (setting instanceof IBooleanSetting) {
            return getBooleanComponent((IBooleanSetting)setting,animation,adder,theme,colorLevel,isContainer);
        } else if (setting instanceof INumberSetting) {
            return getNumberComponent((INumberSetting)setting,animation,adder,theme,colorLevel,isContainer);
        } else if (setting instanceof IEnumSetting) {
            return getEnumComponent((IEnumSetting)setting,animation,adder,theme,colorLevel,isContainer);
        } else if (setting instanceof IColorSetting) {
            return getColorComponent((IColorSetting)setting,animation,adder,theme,colorLevel,isContainer);
        } else if (setting instanceof IKeybindSetting) {
            return getKeybindComponent((IKeybindSetting)setting,animation,adder,theme,colorLevel,isContainer);
        } else if (setting instanceof IStringSetting) {
            return getStringComponent((IStringSetting) setting, animation, adder, theme, colorLevel, isContainer);
        } else if (setting instanceof IRunnableSetting) {
            return getRunnableComponent((IRunnableSetting) setting, animation, adder, theme, colorLevel, isContainer);
        } else {
            return new Button<Void>(setting,()->null,theme.getButtonRenderer(Void.class,isContainer));
        }
    }

    public IComponent getRunnableComponent(IRunnableSetting setting, Supplier<Animation> animation, IComponentAdder adder, ThemeTuple theme, int colorLevel, boolean isContainer) {
        return new Button<Void>(setting,()->null,theme.getButtonRenderer(Void.class,isContainer)) {
            @Override
            public void handleButton(Context context, int button) {
                super.handleButton(context,button);

                if (context.isClicked(button)) {
                    setting.run();
                }
            }
        };
    }
}
