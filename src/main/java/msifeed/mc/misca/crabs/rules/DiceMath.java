package msifeed.mc.misca.crabs.rules;

import java.util.Random;

public final class DiceMath {
    private static final Random rand = new Random();

    public static double gauss(double mean, int from, int to) {
        final double std_dev = from / 2. + to / 2.;
        double roll = from - 1;
        while (roll < from || roll > to) {
            roll = rand.nextGaussian() * mean + std_dev;
        }
        return roll;
    }

    public static int g15() {
        return (int) Math.floor(gauss(4.15, 1, 16));
    }

    public static int g30() {
        return (int) Math.floor(gauss(6.8, 1, 31));
    }

    public static int g30_plus() {
        return (int) Math.floor(gauss(6.8, 3, 31));
    }

    public static int g30_minus() {
        return (int) Math.floor(gauss(6.8, 1, 29));
    }

    public enum DiceRank {
        REGULAR, FAIL, LUCK;

        public static DiceRank ofD15(int roll) {
            if (roll == 15) return LUCK;
            else if (roll == 1) return FAIL;
            else return REGULAR;
        }

        public static DiceRank ofD30(int roll) {
            if (roll >= 28) return LUCK;
            else if (roll <= 3) return FAIL;
            else return REGULAR;
        }

        public boolean beats(DiceRank other) {
            return (this == LUCK && other != LUCK) || (other == FAIL && this != FAIL);
        }
    }
}
