package msifeed.misca.rolls.dice;

public class DiceMod extends DiceMember {
    public DiceMod(int value, String source) {
        this.result = value;
        this.name = source;
    }

    public DiceMod(int value) {
        this(value, "");
    }

    @Override
    public boolean isNegative() {
        return getResult() < 0;
    }

    @Override
    public String toString() {
        return (getResult() < 0 ? "" : "+") + getResult();
    }
}
