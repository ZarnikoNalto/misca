package msifeed.misca.rolls;

import msifeed.misca.rolls.dice.Dice;
import msifeed.misca.rolls.dice.DiceRoll;
import net.minecraft.command.NumberInvalidException;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

public class Dices {
    private static final Random rand = new Random();

    static Pattern patternFullDice = Pattern.compile("([+-]?\\d*D\\d*)", Pattern.CASE_INSENSITIVE);
    static Pattern patternDice = Pattern.compile("[+-]?(\\d*)D(\\d*)", Pattern.CASE_INSENSITIVE);
    static Pattern patternMod = Pattern.compile("([+-]\\d+(?![D\\d]))", Pattern.CASE_INSENSITIVE);

    public static boolean check(double chance) {
        return Math.random() < chance;
    }

    public static boolean checkWithNonce(long nonce, double chance) {
        final Random rand = new Random(nonce);
        rand.setSeed(rand.nextLong()); // Nonce itself may be not random enough to produce random numbers
        return rand.nextDouble() < chance;
    }

    public static int checkInt(double chance) {
        return Math.random() < chance ? 1 : 0;
    }

    public static int roll(int min, int d) {
        return min + rand.nextInt(d);
    }

    public static DiceRoll parseThrow(String str) throws NumberInvalidException  {
        return parseThrow(str, false);
    }

    public static DiceRoll parseThrow(String str, boolean allowNoDice) throws NumberInvalidException  {
        final DiceRoll diceRoll = new DiceRoll();
        final Matcher matcherFullDice = patternFullDice.matcher(str);
        final Matcher matcherMod = patternMod.matcher(str);

        if (!allowNoDice && !matcherFullDice.find()) {
            throw new NumberInvalidException();
        }

        matcherFullDice.reset();

        while (matcherFullDice.find()) {
            final Dice dice = parseDice(matcherFullDice.group(1));
            if (dice != null) {
                diceRoll.addDice(dice);
            }
        }

        while (matcherMod.find()) {
            diceRoll.addMod(parseInt(matcherMod.group(1)));
        }

        return diceRoll;
    }

    public static Dice parseDice(String str) throws NumberInvalidException {
        final Matcher matcherDice = patternDice.matcher(str);

        if (matcherDice.matches()) {
            final String rawDiceCount = matcherDice.group(1);
            final String rawSides = matcherDice.group(2);

            final int diceCount;
            final int side;

            diceCount = !rawDiceCount.isEmpty() ? parseInt(rawDiceCount) : 1;
            side = !rawSides.isEmpty() ? parseInt(rawSides) : 6;

            return new Dice(diceCount, side, !str.startsWith("-"));
        }

        return null;
    }
}
