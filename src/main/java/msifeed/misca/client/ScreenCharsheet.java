package msifeed.misca.client;

import msifeed.mellow.MellowScreen;
import msifeed.mellow.utils.Direction;
import msifeed.mellow.utils.UiBuilder;
import msifeed.mellow.view.button.ButtonLabel;
import msifeed.mellow.view.text.LabelTr;
import msifeed.mellow.view.text.TextInput;
import msifeed.misca.charsheet.ICharsheet;
import msifeed.misca.charsheet.cap.CharsheetProvider;
import msifeed.misca.charsheet.cap.CharsheetSync;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;

public class ScreenCharsheet extends MellowScreen {
    private final EntityPlayer target;
    private final ICharsheet charsheet;

    final TextInput nameInput = new TextInput();

    public ScreenCharsheet(EntityPlayer target) {
        this.target = target;
        this.charsheet = CharsheetProvider.get(target);

        nameInput.grow(100, 0);
        nameInput.insert(charsheet.getName());
        nameInput.getBackend().setMaxColumns(ICharsheet.MAX_NAME_LENGTH);
    }

    @Override
    public void initGui() {
        super.initGui();

        final int lineHeight = nameInput.getBaseGeom().h;

        UiBuilder.of(container)
                .add(new LabelTr("gui.misca.charsheet", target.getDisplayNameString())).center(Direction.HORIZONTAL)

                .beginGroup()
                    .add(new LabelTr("gui.misca.charsheet.name")).size(35, lineHeight).below().move(0, 10, 0)
                    .add(nameInput).right().move(0, -2, 0)
                    .centerGroup(Direction.HORIZONTAL)
                    .pinGroup()

                .add(() -> {
                    final ButtonLabel btn = new ButtonLabel(I18n.format("gui.misca.submit"));
                    btn.setSize(50, 15);
                    btn.setCallback(this::submit);
                    return btn;
                }).below().move(0, 10, 0).center(Direction.HORIZONTAL)

                .centerGroup(Direction.BOTH)
                .apply(ui -> ui.moveGroup(0, -ui.getGroupContent().h / 3, 0))
                .build();
    }

    private void submit() {
        charsheet.setName(nameInput.getText());
        CharsheetSync.post(target, charsheet);
    }
}
