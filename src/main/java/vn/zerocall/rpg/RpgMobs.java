package vn.zerocall.rpg;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import java.util.Set;

/** Makes *vanilla* hostile mobs stronger; no new models / AI loops. */
public final class RpgMobs {
    private static final String CHECKED = "zerocallrpg.checked";
    public static final String ELITE = "zerocallrpg.elite";
    public static final String CHAMPION = "zerocallrpg.champion";
    public static final String DUNGEON_GUARD = "zerocallrpg.dungeon_guard";
    public static final String DUNGEON_BOSS = "zerocallrpg.dungeon_boss";
    private static final Set<EntityType<?>> TYPES = Set.of(
            EntityType.ZOMBIE, EntityType.HUSK, EntityType.DROWNED, EntityType.SKELETON,
            EntityType.STRAY, EntityType.SPIDER, EntityType.CAVE_SPIDER);

    private RpgMobs() { }

    public static void maybeElite(Mob mob) {
        if (!TYPES.contains(mob.getType()) || mob.getTags().contains(CHECKED)) return;
        mob.addTag(CHECKED);
        double roll = mob.getRandom().nextDouble();
        if (roll < 0.005) upgrade(mob, 3, "CHAMPION", CHAMPION);
        else if (roll < 0.035) upgrade(mob, 1.8, "ELITE", ELITE);
    }

    public static void dungeonGuard(Mob mob) {
        mob.addTag(CHECKED);
        upgrade(mob, 1.5, "VE BINH", DUNGEON_GUARD);
    }

    public static void dungeonBoss(Mob mob, String bossName) {
        mob.addTag(CHECKED);
        upgrade(mob, 4.0, bossName, DUNGEON_BOSS);
        mob.setPersistenceRequired();
    }

    private static void upgrade(Mob mob, double mul, String name, String tag) {
        mob.addTag(tag);
        mob.setCustomName(Component.literal("[Zero Call] " + name));
        mob.setCustomNameVisible(true);
        if (mob.getAttribute(Attributes.MAX_HEALTH) != null) {
            mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(mob.getMaxHealth() * mul);
            mob.setHealth(mob.getMaxHealth());
        }
        if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            var a = mob.getAttribute(Attributes.ATTACK_DAMAGE);
            a.setBaseValue(Math.max(2, a.getBaseValue() * Math.min(mul, 2.0)));
        }
    }

    public static int xpFor(Mob mob) {
        int base = Math.max(8, Math.min(45, 7 + (int) (mob.getMaxHealth() / 3)));
        if (mob.getTags().contains(DUNGEON_BOSS)) return base + 200;
        if (mob.getTags().contains(CHAMPION)) return base + 75;
        if (mob.getTags().contains(DUNGEON_GUARD)) return base + 20;
        if (mob.getTags().contains(ELITE)) return base + 35;
        return base;
    }
}
