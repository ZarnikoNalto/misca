package msifeed.misca.locks;

import msifeed.mellow.FocusState;
import msifeed.mellow.MellowScreen;
import msifeed.mellow.utils.Direction;
import msifeed.mellow.utils.UiBuilder;
import msifeed.mellow.view.InputHandler;
import msifeed.mellow.view.button.ButtonLabel;
import msifeed.mellow.view.text.LabelTr;
import msifeed.misca.locks.items.ItemLock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;


/**
 * Класс интерфейса для цифрового замка.
 * Представляет из себя набор кнопок для ввода числового кода.
 * <p></p>
 *
 * <p>7 8 9</p>
 * <p>4 5 6</p>
 * <p>1 2 3</p>
 * <p>~ 0 ~</p>
 */
@SideOnly(Side.CLIENT)
public class GuiDigitalLock extends MellowScreen {

    static public enum GuiType {
        LOCK,
        KEY
    }

    private GuiType currnetGuiType;


    public String secret = ""; //Секретная комбинация, используемая для закрытия двери
    private LabelTr secretLabel = new LabelTr(""); //Визуальное отображение комбинации. Класс LabelTr используется в UIBuilder'e
    private int secretMaxLen = 4; //Максимальная длина последовательности

    private final EntityPlayer player = Minecraft.getMinecraft().player;

    private ItemLock usedLock;

    private final Vec2f buttonSize = new Vec2f(30, 30); //Визуальные размеры кнопки
    private int margin = 3; //Отступ одной кнопки от другой

    /**
     * Конструктор класса
     * <p></p>
     *
     * <p>
     * Пример использования: Minecraft.getMinecraft().displayGuiScreen(new GuiDigitalLock());
     * </p>
     */
    public GuiDigitalLock(ItemLock lock, GuiType guiType) {
        usedLock = lock;
        currnetGuiType = guiType;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void closeGui() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    protected void keyTyped(char c, int key) {
        if (key == Keyboard.KEY_ESCAPE) {
            if (FocusState.INSTANCE.getFocus().isPresent())
                FocusState.INSTANCE.clearFocus();
            else
                Minecraft.getMinecraft().displayGuiScreen(null);

            closingGui(false, null);
        }

        FocusState.INSTANCE.getFocus()
                .filter(view -> view instanceof InputHandler.Keyboard)
                .map(view -> (InputHandler.Keyboard) view)
                .ifPresent(view -> view.onKeyboard(c, key));
    }

    private void closingGui(boolean status, String result) {
        Minecraft.getMinecraft().displayGuiScreen(null);

        switch (currnetGuiType){
            case LOCK:
                usedLock.onCloseGui(status, result);
                break;
            case KEY:
                usedLock.tryApplyKey(status,result);
                break;
        }
    }


    public void initGui() {
        super.initGui();
        int btnX = (int) buttonSize.x;
        int btnY = (int) buttonSize.y;

        Vec3i left = new Vec3i(-(btnX + margin), 0, 0);
        Vec3i right = new Vec3i(btnX + margin, 0, 0);


        UiBuilder.of(container)
                .add(new LabelTr("Digital Lock")).center(Direction.HORIZONTAL)
                .move(0, -60, 0)


                .beginGroup() // Content

                .beginGroup() //Result label
                .add(new LabelTr("Secret:")).center(Direction.HORIZONTAL).move(-20, 0, 0)
                .add(secretLabel).center(Direction.HORIZONTAL).move(20, 0, 0)
                .moveGroup(0, -30, 0)
                .appendGroup() //Close result label
                .centerGroup(Direction.HORIZONTAL)


                .beginGroup()//Buttons
                .beginGroup()
                .add(NumButton("7")).move(left)
                .add(NumButton("8"))
                .add(NumButton("9")).move(right)
                .moveGroup(0, 0, 0)
                .appendGroup()


                .beginGroup()
                .add(NumButton("4")).move(left)
                .add(NumButton("5"))
                .add(NumButton("6")).move(right)
                .moveGroup(0, btnY + margin, 0)
                .appendGroup()


                .beginGroup()
                .add(NumButton("1")).move(left)
                .add(NumButton("2"))
                .add(NumButton("3")).move(right)
                .moveGroup(0, 2 * (btnY + margin), 0)
                .appendGroup()


                .beginGroup()
                .add(NumButton("0"))
                .add(DelButton()).move(right)
                .moveGroup(0, 3 * (btnY + margin), 0)
                .appendGroup()


                .beginGroup()
                .add(EnterButton()).move(left)
                .moveGroup(0, 4 * (btnY + margin), 0)
                .appendGroup()
                .centerGroup(Direction.HORIZONTAL)


                .appendGroup() //Close buttons
                .centerGroup(Direction.HORIZONTAL)


                .appendGroup() //Close Content
                .centerGroup(Direction.BOTH)
                .build();
    }

    private ButtonLabel NumButton(String num) {
        final ButtonLabel btn = new ButtonLabel(I18n.format(num));
        btn.setSize((int) buttonSize.x, (int) buttonSize.y);
        btn.setCallback(() -> addNumberToSecret(num));
        return btn;
    }

    private ButtonLabel DelButton() {
        final ButtonLabel btn = new ButtonLabel(I18n.format("Del"));
        btn.setSize((int) buttonSize.x, (int) buttonSize.y);
        btn.setCallback(() -> removeLastNumberFromSecret());
        return btn;
    }

    private ButtonLabel EnterButton() {
        final ButtonLabel btn = new ButtonLabel(I18n.format("Enter"));
        btn.setSize(3 * ((int) buttonSize.x + margin), (int) buttonSize.y);
        btn.setCallback(() -> closingGui(true, secret));
        return btn;
    }


    private void addNumberToSecret(String num) {
        if (secret.length() >= secretMaxLen) return;
        secret = secret.concat(String.valueOf(num));
        secretLabel.setText(secret);
    }

    private void removeLastNumberFromSecret() {
        int lastIndex = secret.length() - 1;
        if (lastIndex < 0) return;
        secret = secret.substring(0, lastIndex);
        secretLabel.setText(secret);
    }

}
