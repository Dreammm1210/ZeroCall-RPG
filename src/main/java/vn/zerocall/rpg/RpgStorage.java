package vn.zerocall.rpg;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Lives in the world save, not a global profile and not on the phone client. */
public final class RpgStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type TYPE = new TypeToken<Map<String, RpgProfile>>() {}.getType();
    private final Path path;
    private Map<String, RpgProfile> players = new HashMap<>();

    public RpgStorage(MinecraftServer server) {
        this.path = server.getWorldPath(LevelResource.ROOT).resolve("data/zerocall_rpg.json");
        load();
    }

    public RpgProfile player(UUID id) {
        return players.computeIfAbsent(id.toString(), ignored -> new RpgProfile());
    }

    private void load() {
        try {
            if (Files.exists(path)) {
                Map<String, RpgProfile> loaded = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), TYPE);
                if (loaded != null) players = loaded;
            }
        } catch (Exception e) {
            ZeroCallRpg.LOGGER.error("RPG player data load failed, kept save unchanged: {}", path, e);
        }
    }

    public void save() {
        try {
            Files.createDirectories(path.getParent());
            Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
            Files.writeString(tmp, GSON.toJson(players, TYPE), StandardCharsets.UTF_8);
            try {
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException unsupported) {
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            ZeroCallRpg.LOGGER.error("RPG player data save failed: {}", path, e);
        }
    }
}
