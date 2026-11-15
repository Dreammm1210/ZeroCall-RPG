package vn.zerocall.rpg;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ZeroCallRpg implements ModInitializer {
    public static final String MOD_ID = "zerocallrpg";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static RpgStorage storage;
    private static long serverTicks = 0;

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(RpgStatePayload.TYPE, RpgStatePayload.CODEC);
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            storage = new RpgStorage(server);
            serverTicks = 0;
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (storage != null) storage.save();
            storage = null;
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (storage == null) return;
            RpgProfile data = get(handler.player);
            updateAttributes(handler.player, data);
            sync(handler.player);
            handler.player.sendSystemMessage(message("Da vao Zero Call RPG! /rpg help"));
        });
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof Mob mob) RpgMobs.maybeElite(mob);
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((victim, cause) -> {
            if (!(victim instanceof Mob mob) || !(cause.getEntity() instanceof ServerPlayer killer)) return;
            if (storage == null) return;
            RpgProfile data = get(killer);
            data.kills++;
            boolean elite = mob.getTags().contains(RpgMobs.ELITE) || mob.getTags().contains(RpgMobs.CHAMPION);
            boolean boss = mob.getTags().contains(RpgMobs.DUNGEON_BOSS);
            if (elite) data.eliteKills++;
            if (boss) { data.bossKills++; data.dungeonClears++; }
            int earned = RpgMobs.xpFor(mob);
            int ups = data.gainExp(earned);
            if (ups > 0) {
                updateAttributes(killer, data);
                killer.sendSystemMessage(message("Len cap " + data.level + "! +" + (ups * 3) + " diem chi so."));
            }
            if (boss) {
                giveReward(killer, new ItemStack(Items.DIAMOND, 4));
                giveReward(killer, new ItemStack(Items.GOLDEN_APPLE, 1));
                killer.sendSystemMessage(message("DA HA BOSS! +" + earned + " XP, +4 kim cuong, +1 tao vang."));
            } else if (mob.getTags().contains(RpgMobs.CHAMPION)) {
                giveReward(killer, new ItemStack(Items.EMERALD, 3));
                killer.sendSystemMessage(message("CHAMPION bi ha! +" + earned + " XP."));
            } else if (elite) {
                if (killer.getRandom().nextDouble() < 0.5)
                    giveReward(killer, new ItemStack(Items.EMERALD, 1));
            }
            sync(killer);
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (storage == null) return;
            serverTicks++;
            if (serverTicks % 20 == 0) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    RpgProfile data = get(player);
                    data.mana = Math.min(data.maxMana(), data.mana + 3);
                    sync(player);
                }
            }
            if (serverTicks % 1200 == 0) storage.save();
        });
        registerCommands();
        LOGGER.info("Zero Call RPG 0.2.0 loaded - 8 classes, elites, dungeon; no quests");
    }

    private static void giveReward(ServerPlayer player, ItemStack item) {
        if (!player.getInventory().add(item)) player.drop(item, false);
    }

    private static RpgProfile get(ServerPlayer player) { return storage.player(player.getUUID()); }
    private static Component message(String text) {
        return Component.literal("[Zero Call RPG] ").withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(Component.literal(text).withStyle(ChatFormatting.WHITE));
    }

    private static void sync(ServerPlayer player) {
        if (storage == null || !ServerPlayNetworking.canSend(player, RpgStatePayload.TYPE)) return;
        RpgProfile d = get(player);
        ServerPlayNetworking.send(player, new RpgStatePayload(d.job, d.level, d.exp, d.neededExp(),
                d.mana, d.maxMana(), d.points, d.kills, d.eliteKills, d.bossKills, d.dungeonClears));
    }

    private static void updateAttributes(ServerPlayer p, RpgProfile d) {
        // WARNING: changing base attributes can interact with other RPG mods.
        if (p.getAttribute(Attributes.MAX_HEALTH) != null) {
            p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20 + d.vitality * 2);
            if (p.getHealth() > p.getMaxHealth()) p.setHealth(p.getMaxHealth());
        }
        if (p.getAttribute(Attributes.ATTACK_DAMAGE) != null)
            p.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(1 + d.strength * 0.18);
        if (p.getAttribute(Attributes.MOVEMENT_SPEED) != null)
            p.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.1 + d.agility * 0.0015);
    }

    private static void registerCommands() {
        var root = Commands.literal("rpg")
                .executes(c -> show(c.getSource().getPlayerOrException()))
                .then(Commands.literal("help").executes(c -> help(c.getSource().getPlayerOrException())))
                .then(Commands.literal("classes").executes(c -> classes(c.getSource().getPlayerOrException())))
                .then(Commands.literal("skill").executes(c -> skill(c.getSource().getPlayerOrException())))
                .then(Commands.literal("add")
                        .then(Commands.literal("str").executes(c -> add(c.getSource().getPlayerOrException(), "str")))
                        .then(Commands.literal("vit").executes(c -> add(c.getSource().getPlayerOrException(), "vit")))
                        .then(Commands.literal("agi").executes(c -> add(c.getSource().getPlayerOrException(), "agi")))
                        .then(Commands.literal("int").executes(c -> add(c.getSource().getPlayerOrException(), "int"))));

        var classNode = Commands.literal("class");
        for (String key : RpgClasses.ALL.keySet()) {
            classNode.then(Commands.literal(key).executes(c -> choose(c.getSource().getPlayerOrException(), key)));
        }
        root.then(classNode);

        // Dungeon creation replaces blocks in the world; restrict to server operators!
        var create = Commands.literal("create").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
        create.then(Commands.literal("crypt").executes(c -> dungeon(c.getSource().getPlayerOrException(), DungeonBuilder.Theme.CRYPT)));
        create.then(Commands.literal("grove").executes(c -> dungeon(c.getSource().getPlayerOrException(), DungeonBuilder.Theme.GROVE)));
        create.then(Commands.literal("fortress").executes(c -> dungeon(c.getSource().getPlayerOrException(), DungeonBuilder.Theme.FORTRESS)));
        root.then(Commands.literal("dungeon")
                .executes(c -> dungeonInfo(c.getSource().getPlayerOrException()))
                .then(create));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(root));
    }

    private static int show(ServerPlayer p) {
        RpgProfile d = get(p);
        p.sendSystemMessage(message("LV " + d.level + " | Class: " + d.job + " | XP: " + d.exp + "/" + d.neededExp()
                + " | MP: " + d.mana + "/" + d.maxMana()));
        p.sendSystemMessage(message("STR " + d.strength + " | VIT " + d.vitality + " | AGI " + d.agility
                + " | INT " + d.intellect + " | Diem: " + d.points));
        p.sendSystemMessage(message("Mob: " + d.kills + " | Elite: " + d.eliteKills + " | Boss: " + d.bossKills
                + " | Dungeon: " + d.dungeonClears));
        return 1;
    }

    private static int help(ServerPlayer p) {
        p.sendSystemMessage(message("/rpg; /rpg classes; /rpg class <ten>; /rpg skill"));
        p.sendSystemMessage(message("/rpg add str|vit|agi|int; /rpg dungeon"));
        p.sendSystemMessage(message("Admin: /rpg dungeon create crypt|grove|fortress"));
        return 1;
    }

    private static int classes(ServerPlayer p) {
        for (var entry : RpgClasses.ALL.entrySet()) {
            var c = entry.getValue();
            p.sendSystemMessage(message(entry.getKey() + " - " + c.name() + " | " + c.buff()
                    + " | MP " + c.manaCost() + " | CD " + c.cooldownSeconds() + "s"));
        }
        return 1;
    }

    private static int choose(ServerPlayer p, String job) {
        RpgProfile d = get(p);
        p.sendSystemMessage(message(d.chooseJob(job)
                ? "Da chon class " + job + ". /rpg skill de buff!"
                : "Da chon class truoc do, hoac class khong hop le."));
        sync(p);
        return 1;
    }

    private static int add(ServerPlayer p, String stat) {
        RpgProfile d = get(p);
        if (!d.putPoint(stat)) { p.sendSystemMessage(message("Khong du diem / sai chi so.")); return 0; }
        updateAttributes(p, d);
        p.sendSystemMessage(message("+1 " + stat.toUpperCase() + ", con " + d.points + " diem."));
        sync(p);
        return 1;
    }

    private static int skill(ServerPlayer p) {
        RpgProfile d = get(p);
        RpgClasses.ClassData clazz = RpgClasses.ALL.get(d.job);
        if (clazz == null) { p.sendSystemMessage(message("Chon class truoc: /rpg classes")); return 0; }
        long now = System.currentTimeMillis();
        if (now < d.skillReadyAt) {
            p.sendSystemMessage(message("Con " + ((d.skillReadyAt - now + 999) / 1000) + "s hoi chieu."));
            return 0;
        }
        if (d.mana < clazz.manaCost()) {
            p.sendSystemMessage(message("Khong du mana! Can " + clazz.manaCost() + " MP."));
            return 0;
        }
        switch (d.job) {
            case "warrior" -> { buff(p, MobEffects.STRENGTH, 240, 0); buff(p, MobEffects.RESISTANCE, 160, 0); }
            case "mage" -> { buff(p, MobEffects.REGENERATION, 120, 0); buff(p, MobEffects.RESISTANCE, 160, 0); }
            case "archer" -> { buff(p, MobEffects.SPEED, 240, 1); buff(p, MobEffects.NIGHT_VISION, 300, 0); }
            case "assassin" -> { buff(p, MobEffects.INVISIBILITY, 160, 0); buff(p, MobEffects.SPEED, 160, 1); }
            case "paladin" -> { buff(p, MobEffects.ABSORPTION, 300, 1); buff(p, MobEffects.REGENERATION, 160, 0); }
            case "berserker" -> { buff(p, MobEffects.STRENGTH, 160, 1); buff(p, MobEffects.WEAKNESS, 160, 0); }
            case "druid" -> { buff(p, MobEffects.REGENERATION, 160, 1); buff(p, MobEffects.RESISTANCE, 200, 0); }
            case "monk" -> { buff(p, MobEffects.HASTE, 240, 1); buff(p, MobEffects.RESISTANCE, 200, 0); }
            default -> { return 0; }
        }
        d.mana -= clazz.manaCost();
        d.skillReadyAt = now + clazz.cooldownSeconds() * 1000L;
        p.sendSystemMessage(message("BUFF: " + clazz.buff() + "!"));
        sync(p);
        return 1;
    }

    private static void buff(ServerPlayer p, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,
                             int ticks, int amplifier) {
        p.addEffect(new MobEffectInstance(effect, ticks, amplifier));
    }

    private static int dungeonInfo(ServerPlayer p) {
        p.sendSystemMessage(message("Dungeon: Ham Mo [crypt], Rung Nhen [grove], Phao Dai [fortress]."));
        p.sendSystemMessage(message("Chi OP duoc tao dungeon tai noi dung: /rpg dungeon create <loai>."));
        p.sendSystemMessage(message("CANH BAO: tao dungeon se THAY THE block trong khu 37x13x7! Sao luu world truoc."));
        return 1;
    }

    private static int dungeon(ServerPlayer p, DungeonBuilder.Theme theme) {
        String result = DungeonBuilder.build(p, theme);
        if (!"OK".equals(result)) { p.sendSystemMessage(message(result)); return 0; }
        return 1;
    }
}
