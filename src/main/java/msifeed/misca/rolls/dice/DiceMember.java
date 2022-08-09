package msifeed.misca.rolls.dice;

public abstract class DiceMember {
    protected boolean first = false;
    protected boolean hidden = false;
    protected boolean negative = false;
    protected Integer result;
    protected String name = "";

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setNegative(boolean negative) {
        if (this.negative != negative) {
            result = -result;
        }

        this.negative = true;
    }

    public boolean isNegative() {
        return negative;
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String formatted() {
        return (isFirst() ? "" : (isNegative() ? "\u00A7c" : "\u00A7a"))
                + (!getName().isEmpty() && !isHidden() ? getName() + " " : "")
                + this;
    }
}
