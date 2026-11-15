package vn.zerocall.rpg.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import vn.zerocall.rpg.RpgClasses;
import vn.zerocall.rpg.RpgStatePayload;
import vn.zerocall.rpg.ZeroCallRpg;

/** Compact native HUD; no shaders, textures, particles, or render loops beyond HUD draw. */
public final class ZeroCallRpgClient implements ClientModInitializer {
    private static RpgStatePayload state;
    private static final int WHITE = 0xFFF3F0FF;
    private static final int MUTED = 0xFFC3BED3;
    private static final int GOLD = 0xFFF7C85E;
    private static final int BG = 0xC914192B;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(RpgStatePayload.TYPE, (packet, context) -> state = packet);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> state = null);
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(ZeroCallRpg.MOD_ID, "hud"),
                (graphics, tick) -> render(graphics));
    }

    private static void render(GuiGraphics gfx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || state == null || mc.options.hideGui) return;
        int x = 6, y = 6, w = 179, h = 73;
        gfx.fill(x, y, x + w, y + h, BG);
        gfx.fill(x, y, x + w, y + 2, GOLD);
        draw(gfx, "ZERO CALL RPG", x + 6, y + 6, GOLD);
        String job = RpgClasses.ALL.containsKey(state.job()) ? RpgClasses.ALL.get(state.job()).name() : "Tan Thu";
        draw(gfx, "LV " + state.level() + "  " + job, x + 72, y + 6, WHITE);

        float hp = mc.player.getHealth();
        float maxHp = Math.max(1, mc.player.getMaxHealth());
        bar(gfx, x + 6, y + 21, 167, 8, hp / maxHp, 0xFFF45F74);
        draw(gfx, "HP " + (int)Math.ceil(hp) + "/" + (int)maxHp, x + 9, y + 20, WHITE);
        bar(gfx, x + 6, y + 34, 167, 8,
                (float)state.mana() / Math.max(1, state.maxMana()), 0xFF55AFFF);
        draw(gfx, "MP " + state.mana() + "/" + state.maxMana(), x + 9, y + 33, WHITE);
        bar(gfx, x + 6, y + 47, 167, 8,
                (float)state.exp() / Math.max(1, state.needed()), 0xFFF1C859);
        draw(gfx, "XP " + state.exp() + "/" + state.needed(), x + 9, y + 46, WHITE);
        draw(gfx, "POINT " + state.points() + "   ELITE " + state.eliteKills()
                + "   BOSS " + state.bossKills(), x + 6, y + 61, MUTED);
    }

    private static void draw(GuiGraphics gfx, String text, int x, int y, int color) {
        gfx.drawString(Minecraft.getInstance().font, text, x, y, color, true);
    }

    private static void bar(GuiGraphics gfx, int x, int y, int w, int h, float value, int color) {
        gfx.fill(x, y, x + w, y + h, 0xFF293146);
        int fill = Math.round((w - 2) * Math.max(0, Math.min(1, value)));
        if (fill > 0) gfx.fill(x + 1, y + 1, x + 1 + fill, y + h - 1, color);
    }
}
