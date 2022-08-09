package msifeed.misca.client;

import java.awt.Dimension;
import java.awt.Point;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.Supplier;

import com.lukflug.panelstudio.component.*;
import com.lukflug.panelstudio.widget.*;
import lukflug.panelstudio.layout.MiscaComponentGenerator;
import msifeed.misca.client.module.*;
import org.lwjgl.input.Keyboard;

import msifeed.misca.client.module.ClickGUIModule.Theme;
import com.lukflug.panelstudio.base.Animation;
import com.lukflug.panelstudio.base.Context;
import com.lukflug.panelstudio.base.IToggleable;
import com.lukflug.panelstudio.base.SettingsAnimation;
import com.lukflug.panelstudio.base.SimpleToggleable;
import com.lukflug.panelstudio.hud.HUDGUI;
import com.lukflug.panelstudio.layout.ChildUtil.ChildMode;
import com.lukflug.panelstudio.layout.IComponentAdder;
import com.lukflug.panelstudio.layout.IComponentGenerator;
import com.lukflug.panelstudio.layout.ILayout;
import com.lukflug.panelstudio.layout.PanelAdder;
import com.lukflug.panelstudio.layout.PanelLayout;
import com.lukflug.panelstudio.mc12.MinecraftHUDGUI;
import com.lukflug.panelstudio.popup.MousePositioner;
import com.lukflug.panelstudio.popup.PanelPositioner;
import com.lukflug.panelstudio.popup.PopupTuple;
import com.lukflug.panelstudio.setting.IClient;
import com.lukflug.panelstudio.theme.ITheme;
import com.lukflug.panelstudio.theme.ImpactTheme;

public class ScreenGUI extends MinecraftHUDGUI {
	private final GUIInterface inter;
	private final HUDGUI gui;
	public static final int WIDTH=120,HEIGHT=12,DISTANCE=6;
	
	public ScreenGUI() {
		IClient client=Category.getClient();

		inter=new GUIInterface(false) {
			@Override
			protected String getResourcePrefix() {
				return "misca:";
			}
		};

		ITheme theme=new ImpactTheme(new ClickGUIModule.ThemeScheme(Theme.Impact),9,2);

		IToggleable guiToggle=new SimpleToggleable(false);
		IToggleable hudToggle=new SimpleToggleable(false) {
			@Override
			public boolean isOn() {
				return guiToggle.isOn() || super.isOn();
			}
		};
		gui=new HUDGUI(inter,theme.getDescriptionRenderer(), new MousePositioner(new Point(10,10)),guiToggle,hudToggle);

		Supplier<Animation> animation=()->new SettingsAnimation(()->200, inter::getTime);

		BiFunction<Context,Integer,Integer> scrollHeight=(context,componentHeight)->Math.min(componentHeight,Math.max(HEIGHT*4, ScreenGUI.this.height-context.getPos().y-HEIGHT));
		PopupTuple popupType=new PopupTuple(new PanelPositioner(new Point(0,0)),false,new IScrollSize() {
			@Override
			public int getScrollHeight (Context context, int componentHeight) {
				return scrollHeight.apply(context,componentHeight);
			}
		});

		IntFunction<IResizable> resizable=width->new IResizable() {
			Dimension size=new Dimension(width,200);

			@Override
			public Dimension getSize() {
				return new Dimension(size);
			}

			@Override
			public void setSize (Dimension size) {
				this.size.width=size.width;
				this.size.height=size.height;
				if (size.width<75) this.size.width=75;
				if (size.height<50) this.size.height=50;
			}
		};

		Function<IResizable,IScrollSize> resizableHeight=size->new IScrollSize() {
			@Override
			public int getScrollHeight (Context context, int componentHeight) {
				return size.getSize().height;
			}
		};

		IntPredicate keybindKey=scancode->scancode==Keyboard.KEY_DELETE;
		IntPredicate charFilter=character-> character>=' ';
		ITextFieldKeys keys=new ITextFieldKeys() {
			@Override
			public boolean isBackspaceKey (int scancode) {
				return scancode==Keyboard.KEY_BACK;
			}

			@Override
			public boolean isDeleteKey (int scancode) {
				return scancode==Keyboard.KEY_DELETE;
			}

			@Override
			public boolean isInsertKey (int scancode) {
				return scancode==Keyboard.KEY_INSERT;
			}

			@Override
			public boolean isLeftKey (int scancode) {
				return scancode==Keyboard.KEY_LEFT;
			}

			@Override
			public boolean isRightKey (int scancode) {
				return scancode==Keyboard.KEY_RIGHT;
			}

			@Override
			public boolean isHomeKey (int scancode) {
				return scancode==Keyboard.KEY_HOME;
			}

			@Override
			public boolean isEndKey (int scancode) {
				return scancode==Keyboard.KEY_END;
			}

			@Override
			public boolean isCopyKey (int scancode) {
				return scancode==Keyboard.KEY_C;
			}

			@Override
			public boolean isPasteKey (int scancode) {
				return scancode==Keyboard.KEY_V;
			}

			@Override
			public boolean isCutKey (int scancode) {
				return scancode==Keyboard.KEY_X;
			}

			@Override
			public boolean isAllKey (int scancode) {
				return scancode==Keyboard.KEY_A;
			}			
		};

		IComponentGenerator generator=new MiscaComponentGenerator(keybindKey,charFilter,keys);

		IComponentAdder classicPanelAdder=new PanelAdder(gui,false,()->true,title->"classicPanel_"+title) {
			@Override
			protected IResizable getResizable (int width) {
				return resizable.apply(width);
			}
			
			@Override
			protected IScrollSize getScrollSize (IResizable size) {
				return resizableHeight.apply(size);
			}
		};
		ILayout classicPanelLayout=new PanelLayout(WIDTH,new Point(DISTANCE,DISTANCE),(WIDTH+DISTANCE)/2,HEIGHT+DISTANCE,animation,level->ChildMode.DOWN,level->ChildMode.DOWN,popupType);
		classicPanelLayout.populateGUI(classicPanelAdder,generator,client,theme);

		if (!getGUI().getGUIVisibility().isOn()) getGUI().getGUIVisibility().toggle();
		if (!getGUI().getHUDVisibility().isOn()) getGUI().getHUDVisibility().toggle();
	}

	@Override
	protected HUDGUI getGUI() {
		return gui;
	}

	@Override
	protected GUIInterface getInterface() {
		return inter;
	}

	@Override
	protected int getScrollSpeed() {
		return 10;
	}
}
