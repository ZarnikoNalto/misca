package msifeed.misca.rolls.dice;

import com.google.common.base.Joiner;
import scala.actors.threadpool.Arrays;

import java.util.List;
import java.util.Random;

public class Dice extends DiceMember {
    private int diceCount = 1;
    private int sides = 6;
    private int advantage = 0;
    private final Integer[] dices;
    private Integer firstResult;

    private final Random random = new Random();

    public Dice(int diceCount, int sides) {
        this(diceCount, sides, true);
    }

    public Dice(int diceCount, int sides, boolean positive) {
        this.diceCount = diceCount;
        this.sides = sides;
        this.negative = !positive;
        dices = new Integer[diceCount];
    }

    public void advantage()
    {
        this.advantage = 1;
    }

    public void disadvantage()
    {
        this.advantage = -1;
    }

    public int getDiceCount() {
        return diceCount;
    }

    public int getSides() {
        return sides;
    }

    public int getMax() {
        return sides * diceCount;
    }

    public int getMin() {
        return diceCount;
    }

    public void roll(boolean second) {
        result = 0;

        for (int i = 0; i < Math.max(Math.abs(diceCount), 1); i++) {
            final int roll = random.nextInt(Math.abs(sides)) + 1;
            dices[i] = roll;
            result += roll;
        }

        if (negative) {
            result = -result;
        }

        if (!second && this.advantage != 0) {
            firstResult = result;

            roll(true);
        }
    }

    public int getFirstResult()
    {
        return this.firstResult;
    }

    public boolean isFirstUsed()
    {
        return advantage != 0 && (advantage == 1 ? firstResult > result : firstResult < result);
    }

    public boolean isCrit(Integer value)
    {
        if (value == null) {
            value = getResult();
        }

        return value == getMax() || value == getMin();
    }

    public boolean isCrit()
    {
        return isCrit(null);
    }

    @Override
    public Integer getResult()
    {
        return result != null ? isFirstUsed() ? firstResult : result : null;
    }

    public List getDices() {
        return Arrays.asList(dices);
    }

    @Override
    public String toString() {
        return String.format("%s%dd%d", isFirst() ? "" : (isNegative() ? "-" : "+"), diceCount, sides);
    }

    public String getTooltipLine() {
        String resultString = (isCrit(result) ? "\u00A7l\u00A7n" : "") + result + "\u00A7r";

        if (advantage != 0) {
            String firstResultString = (isCrit(firstResult) ? "\u00A7l\u00A7n" : "") + firstResult + "\u00A7r";

            if (isFirstUsed()) {
                resultString = "\u00A77" + resultString;
            } else {
                firstResultString = "\u00A77" + firstResultString;
            }

            return String.format("\n%s = [%s, %s]",
                    formatted(),
                    firstResultString, resultString);
        }
        else
        {
            if (getDiceCount() == 1) {
                return String.format("\n%s = %s",
                        formatted(),
                        resultString);
            } else {
                return String.format("\n%s = %s = %s",
                        formatted(),
                        String.format(isNegative() ? "-(%s)" : "%s", Joiner.on(" + ")
                                .join(getDices())),
                        resultString);
            }
        }
    }
}
