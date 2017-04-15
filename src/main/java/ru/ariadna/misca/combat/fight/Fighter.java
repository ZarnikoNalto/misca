package ru.ariadna.misca.combat.fight;

import net.minecraft.entity.player.EntityPlayer;
import ru.ariadna.misca.combat.characters.Character;

public class Fighter {
    final EntityPlayer player;
    final Character character;
    Action.Stage stage = Action.Stage.ATTACK;
    Fighter target;
    boolean isDead = false;


    public Fighter(EntityPlayer player, Character character) {
        this.player = player;
        this.character = character;
    }

    public Action.Stage getStage() {
        return stage;
    }

    public boolean isDead() {
        return isDead;
    }
}
