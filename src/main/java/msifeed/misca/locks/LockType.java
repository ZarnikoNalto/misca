package msifeed.misca.locks;

import net.minecraft.item.Item;

public enum LockType {
    mechanical, magical, digital;

    public Item getItem() {
        switch (this) {
            case digital:
                return LockItems.lockDigital;
            case magical:
                return LockItems.lockMagical;
            case mechanical:
            default:
                return LockItems.lockMechanical;
        }
    }
}
