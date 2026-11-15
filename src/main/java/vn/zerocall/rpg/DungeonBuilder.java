package vn.zerocall.rpg;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** OP-placed three-room dungeon. Explicit creation only: never generates while exploring chunks. */
public final class DungeonBuilder {
    public enum Theme {
        CRYPT("Ham Mo", Blocks.DEEPSLATE_BRICKS, Blocks.CRACKED_DEEPSLATE_BRICKS,
                EntityType.ZOMBIE, EntityType.SKELETON, EntityType.WITHER_SKELETON, "VAO CHUA HAM MO"),
        GROVE("Rung Nhen", Blocks.MOSSY_COBBLESTONE, Blocks.MOSS_BLOCK,
                EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.WITCH, "NU HOANG NHEN"),
        FORTRESS("Phao Dai", Blocks.STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS,
                EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.EVOKER, "CHU PHAO DAI");

        public final String name;
        public final Block wall;
        public final Block floor;
        public final EntityType<? extends Mob> a;
        public final EntityType<? extends Mob> b;
        public final EntityType<? extends Mob> boss;
        public final String bossName;
        Theme(String name, Block wall, Block floor, EntityType<? extends Mob> a,
                EntityType<? extends Mob> b, EntityType<? extends Mob> boss, String bossName) {
            this.name = name; this.wall = wall; this.floor = floor;
            this.a = a; this.b = b; this.boss = boss; this.bossName = bossName;
        }
    }

    private DungeonBuilder() { }

    public static String build(ServerPlayer player, Theme theme) {
        ServerLevel level = player.level();
        if (level.dimension() != Level.OVERWORLD) return "Chi tao dungeon trong Overworld.";

        // Admin deliberately chooses the site. Do not build near the player or at the feet.
        BlockPos origin = player.blockPosition().offset(8, 0, 8);
        int ox = origin.getX(), oy = origin.getY(), oz = origin.getZ();
        if (oy < level.getMinY() + 2 || oy + 7 > level.getMaxY()) return "Do cao khong hop le.";

        // Protect block entities (chests, signs, modded machines) before modifying any block.
        // WARNING: ordinary terrain/building blocks in the region WILL be overwritten.
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = 0; x <= 36; x++) {
            for (int z = 0; z <= 12; z++) {
                for (int y = 0; y <= 6; y++) {
                    cursor.set(ox + x, oy + y, oz + z);
                    if (level.getBlockEntity(cursor) != null)
                        return "Co ruong / may / block entity trong khu vuc. Khong the ghi de!";
                }
            }
        }
        // 3 rooms, each 12x12, with two open connectors. No chunk-generation hooks.
        for (int x = 0; x <= 36; x++) {
            for (int z = 0; z <= 12; z++) {
                for (int y = 0; y <= 6; y++) {
                    boolean floor = y == 0;
                    boolean ceiling = y == 6;
                    boolean outer = x == 0 || x == 36 || z == 0 || z == 12;
                    boolean divider = x == 12 || x == 24;
                    boolean arch = z >= 5 && z <= 7 && y >= 1 && y <= 3;
                    Block block;
                    if (floor) block = (x + z) % 5 == 0 ? theme.floor : theme.wall;
                    else if (ceiling) block = theme.wall;
                    else if ((outer || divider) && !(arch && (x == 0 || divider))) block = theme.wall;
                    else block = Blocks.AIR;
                    cursor.set(ox + x, oy + y, oz + z);
                    level.setBlock(cursor, block.defaultBlockState(), 2); // avoid expensive neighbor cascades
                }
            }
        }

        // Decorative, always-lit landmarks; no particles or animations.
        for (int x : new int[] {6, 18, 30}) {
            level.setBlock(origin.offset(x, 5, 6), Blocks.GLOWSTONE.defaultBlockState(), 3);
            level.setBlock(origin.offset(x, 1, 1), theme.wall.defaultBlockState(), 3);
            level.setBlock(origin.offset(x, 1, 11), theme.wall.defaultBlockState(), 3);
        }

        // Last room has reward chest; progression is solely combat, no quests or quest NPCs.
        BlockPos chest = origin.offset(34, 1, 6);
        level.setBlock(chest, Blocks.CHEST.defaultBlockState(), 3);
        if (level.getBlockEntity(chest) instanceof Container inventory) {
            inventory.setItem(0, new ItemStack(Items.DIAMOND, 2));
            inventory.setItem(1, new ItemStack(Items.GOLDEN_APPLE, 2));
            inventory.setItem(2, new ItemStack(Items.EMERALD, 5));
            inventory.setChanged();
        }

        spawnGuard(level, origin.offset(5, 1, 4), theme.a);
        spawnGuard(level, origin.offset(7, 1, 8), theme.a);
        spawnGuard(level, origin.offset(17, 1, 4), theme.b);
        spawnGuard(level, origin.offset(19, 1, 8), theme.b);
        theme.boss.spawn(level, mob -> RpgMobs.dungeonBoss(mob, theme.bossName),
                origin.offset(29, 1, 6), EntitySpawnReason.COMMAND, false, false);

        player.sendSystemMessage(Component.literal("[Zero Call RPG] Dungeon " + theme.name
                + " da duoc tao tai X=" + ox + " Y=" + oy + " Z=" + oz
                + ". Di tu cua phia tay. Boss o phong cuoi."));
        return "OK";
    }

    private static void spawnGuard(ServerLevel level, BlockPos pos, EntityType<? extends Mob> type) {
        type.spawn(level, RpgMobs::dungeonGuard, pos, EntitySpawnReason.COMMAND, false, false);
    }
}
