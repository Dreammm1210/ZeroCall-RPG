package vn.zerocall.rpg;

import java.util.Map;

/** Values never depend on a client, so the server is authoritative. */
public final class RpgClasses {
    public record ClassData(String name, String buff, int manaCost, int cooldownSeconds) {}
    public static final Map<String, ClassData> ALL = Map.of(
            "warrior", new ClassData("Chien Binh", "Suc manh I + Khang sat thuong I", 30, 25),
            "mage", new ClassData("Phap Su", "Hoi phuc I + Khang sat thuong I", 40, 28),
            "archer", new ClassData("Cung Thu", "Toc do II + Nhin dem", 25, 22),
            "assassin", new ClassData("Sat Thu", "Tang hinh + Toc do II", 30, 30),
            "paladin", new ClassData("Thanh Ky Si", "Hap thu II + Hoi phuc I", 45, 35),
            "berserker", new ClassData("Cuong Chien", "Suc manh II + Yeu duoi I", 40, 32),
            "druid", new ClassData("Druid", "Hoi phuc II + Khang sat thuong I", 40, 30),
            "monk", new ClassData("Vo Tang", "Dao nhanh II + Khang sat thuong I", 30, 25)
    );
    private RpgClasses() { }
}
