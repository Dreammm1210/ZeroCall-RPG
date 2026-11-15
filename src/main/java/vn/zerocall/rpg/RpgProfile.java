package vn.zerocall.rpg;

/** Saved per player; no quests or shop. Pure Java for offline unit tests. */
public final class RpgProfile {
    public String job = "none";
    public int level = 1;
    public int exp = 0;
    public int mana = 100;
    public int points = 0;
    public int strength = 0;
    public int vitality = 0;
    public int agility = 0;
    public int intellect = 0;
    public int kills = 0;
    public int eliteKills = 0;
    public int bossKills = 0;
    public int dungeonClears = 0;
    public long skillReadyAt = 0; // milliseconds since epoch, survives a restart

    public int neededExp() { return 45 + level * 20 + level * level * 5; }
    public int maxMana() { return 100 + intellect * 5 + (level - 1) * 2; }

    public int gainExp(int amount) {
        if (amount < 0) return 0;
        exp = Math.max(0, exp + amount);
        int gained = 0;
        while (level < 100 && exp >= neededExp()) {
            exp -= neededExp();
            level++;
            points += 3;
            gained++;
        }
        if (level >= 100) exp = 0;
        return gained;
    }

    public boolean chooseJob(String selected) {
        if (!"none".equals(job) || !RpgClasses.ALL.containsKey(selected)) return false;
        job = selected;
        return true;
    }

    public boolean putPoint(String stat) {
        if (points <= 0) return false;
        switch (stat) {
            case "str" -> strength++;
            case "vit" -> vitality++;
            case "agi" -> agility++;
            case "int" -> intellect++;
            default -> { return false; }
        }
        points--;
        return true;
    }
}
