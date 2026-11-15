package vn.zerocall.rpg;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client sees only a small snapshot once per second, with no quest field. */
public record RpgStatePayload(
        String job, int level, int exp, int needed, int mana, int maxMana,
        int points, int kills, int eliteKills, int bossKills, int dungeonClears
) implements CustomPacketPayload {
    public static final Type<RpgStatePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(ZeroCallRpg.MOD_ID, "state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RpgStatePayload> CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeUtf(data.job(), 16);
                buf.writeVarInt(data.level());
                buf.writeVarInt(data.exp());
                buf.writeVarInt(data.needed());
                buf.writeVarInt(data.mana());
                buf.writeVarInt(data.maxMana());
                buf.writeVarInt(data.points());
                buf.writeVarInt(data.kills());
                buf.writeVarInt(data.eliteKills());
                buf.writeVarInt(data.bossKills());
                buf.writeVarInt(data.dungeonClears());
            },
            buf -> new RpgStatePayload(
                    buf.readUtf(16), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt())
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
