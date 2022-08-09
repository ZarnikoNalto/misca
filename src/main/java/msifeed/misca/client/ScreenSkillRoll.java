package msifeed.misca.client;

import msifeed.mellow.MellowScreen;
import msifeed.mellow.utils.Direction;
import msifeed.mellow.utils.UiBuilder;
import msifeed.mellow.view.button.ButtonLabel;
import msifeed.mellow.view.text.Label;
import msifeed.mellow.view.text.LabelTr;
import msifeed.mellow.view.text.TextInput;
import msifeed.misca.charsheet.CharSkill;
import msifeed.misca.charsheet.ICharsheet;
import msifeed.misca.charsheet.cap.CharsheetProvider;
import msifeed.misca.rolls.RollRpc;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;

public class ScreenSkillRoll extends MellowScreen {
    private final EntityPlayer target;
    private final ICharsheet charsheet;

    static final TextInput modifiersInput = new TextInput();
    static final TextInput defenseModifiersInput = new TextInput();

    public ScreenSkillRoll(EntityPlayer target) {
        this.target = target;
        this.charsheet = CharsheetProvider.get(target);

        modifiersInput.grow(80, 0);
        defenseModifiersInput.grow(90, 0);
    }

    @Override
    public void initGui() {
        super.initGui();

        UiBuilder.of(container)
                .add(new LabelTr("gui.misca.roll.title")).center(Direction.HORIZONTAL)

                .beginGroup()
                    .add(new Label(I18n.format("gui.misca.roll.advantage"), 0x00ff73)).size(50, 12).below()
                    .add(new Label(I18n.format("gui.misca.roll.disadvantage"), 0xf0535a)).size(50, 12).below()
                    .moveGroup(0, 4, 0)
                    .pinGroup()

                .beginGroup()

                .beginGroup()
                    .add(new LabelTr("gui.misca.roll.mod")).size(50, 12).below()
                    .add(modifiersInput).size(114, 12).right().move(17, 0, 0)
                    .moveGroup(0, 12, 0)
                    .pinGroup()

                .beginGroup()
                    .add(() -> {
                        final ButtonLabel btn = new ButtonLabel(I18n.format("gui.misca.roll.base"));
                        btn.setSize(182, 12);
                        btn.setCallback(() -> roll(null));
                        return btn;
                    }).below()
                    .moveGroup(0, 4, 0)
                    .pinGroup()

                .beginGroup()
                    .beginGroup()
                        .add(new LabelTr("gui.misca.defense.title")).size(80, 12).below()
                        .add(new LabelTr("gui.misca.defense.mod")).size(80, 12).below()
                        .add(defenseModifiersInput).size(90, 12).below().move(0, 1, 0)
                        .add(() -> {
                            final ButtonLabel btn = new ButtonLabel(I18n.format("gui.misca.defense.send"));
                            btn.setSize(90, 12);
                            btn.setCallback(this::showDefense);
                            return btn;
                        }).below().move(0, 1, 0)
                        .moveGroup(0, 4, 0)
                        .pinGroup()
                    .beginGroup()
                        .add(new LabelTr("gui.misca.roll.skills")).size(80, 12).right()
                        .forEach(CharSkill.values(), (ui, skill) -> {
                            final int value = charsheet.skills().get(skill);
                            final String label = String.format("%s %d", skill.tr(), value);
                            final ButtonLabel skillButton = new ButtonLabel(label);
                            skillButton.setSize(90, 12);
                            skillButton.setCallback(() -> roll(skill));

                            ui.add(skillButton).below().move(0, 2, 0);
                        })
                        .moveGroup(12, 0, 0)
                        .pinGroup()
                .appendGroup()

                .appendGroup()

                .centerGroup(Direction.BOTH)
                .moveGroup(120, -10, 0)
                .build();
    }

    private void roll(CharSkill skill) {
        String modText = modifiersInput.getText();

        if (!modText.matches("([+-]).*")) {
            modText = "+" + modText;
        }

        RollRpc.doSkillRoll(skill, isShiftKeyDown() ? 1 : isCtrlKeyDown() ? -1 : 0, modText);
    }

    private void showDefense() {
        String modText = defenseModifiersInput.getText();

        if (!modText.matches("([+-]).*")) {
            modText = "+" + modText;
        }

        RollRpc.doShowDefense(modText);
    }
}
