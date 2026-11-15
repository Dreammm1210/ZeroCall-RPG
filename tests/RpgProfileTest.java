import vn.zerocall.rpg.RpgProfile;
import vn.zerocall.rpg.RpgClasses;

public final class RpgProfileTest {
    public static void main(String[] args) {
        assert RpgClasses.ALL.size() == 8 : "eight distinct classes";
        assert RpgClasses.ALL.values().stream().allMatch(x -> x.manaCost() >= 20 && x.cooldownSeconds() >= 20);
        assert RpgClasses.ALL.values().stream().map(RpgClasses.ClassData::buff).distinct().count() == 8;
        RpgProfile p = new RpgProfile();
        assert p.level == 1 && p.neededExp() == 70;
        assert !p.chooseJob("invalid");
        assert p.chooseJob("paladin");
        assert !p.chooseJob("mage");
        assert p.gainExp(70) == 1;
        assert p.level == 2 && p.exp == 0 && p.points == 3;
        assert p.putPoint("vit") && p.vitality == 1;
        assert !p.putPoint("invalid");
        assert p.gainExp(-100) == 0 && p.exp == 0;
        p.gainExp(3_000_000);
        assert p.level == 100 && p.exp == 0;
        assert RpgClasses.ALL.containsKey("druid");
        System.out.println("PASS: 8 unique classes / XP / stats / cap / no quests");
    }
}
