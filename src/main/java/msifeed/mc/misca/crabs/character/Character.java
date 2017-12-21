package msifeed.mc.misca.crabs.character;

import java.util.EnumMap;

public class Character {
    public String name = "";
    public EnumMap<Stats, Integer> stats = new EnumMap<>(Stats.class);

    public transient boolean isPlayer = false; // Используется только при сохранении стат, отсеивая не-игроков

    Character() {
    }

    public Character(int[] stats) {
        fill(stats);
    }

    public void fill(int[] stats) {
        final Stats[] sv = Stats.values();
        for (int i = 0; i < sv.length; i++) {
            this.stats.put(sv[i], stats[i]);
        }
    }

    public int stat(Stats s) {
        return stats.getOrDefault(s, 0);
    }

    public String compactStats() {
        final StringBuilder sb = new StringBuilder();
        final Stats[] sv = Stats.values();
        for (Stats s : sv) {
            sb.append(stats.getOrDefault(s, 0));
            sb.append(' ');
        }
        return sb.substring(0, sb.length() - 1);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Character)) return false;
        final Character character = (Character) obj;
        return this.stats.equals(character.stats);
    }
}
