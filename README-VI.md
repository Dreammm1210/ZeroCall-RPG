# HƯỚNG DẪN GITHUB ACTIONS — TỰ BUILD JAR

**Đây là mã nguồn đưa lên GitHub, không phải JAR đã build.**

- Giải nén ZIP, đưa **toàn bộ file/thư mục đã giải nén** lên repository (không chỉ upload file ZIP).
- Đảm bảo `.github/workflows/build.yml` tồn tại trong repo.
- Mở **Actions → Build Zero Call RPG JAR → Run workflow**; hoặc commit sẽ tự chạy.
- Build thành công (dấu ✓): trong run, phần **Artifacts**, tải `ZeroCall-RPG-Fabric-1.21.11-JAR`; giải nén artifact để lấy `.jar`.
- Build thất bại (dấu ✗): vào job `build`, chụp hoặc sao chép lỗi ở bước `Build Minecraft Fabric mod` để sửa.
- Xem thêm `HUONG-DAN-GITHUB.txt`. Chưa kiểm chứng biên dịch trên môi trường có đầy đủ Minecraft/Fabric; có thể cần sửa lỗi API.

---

# ZERO CALL RPG v0.2.0 — FABRIC 1.21.11

**Phạm vi mới theo yêu cầu:** class + buff riêng + quái Elite/Champion + dungeon + boss, **không có nhiệm vụ, NPC nhiệm vụ, cửa hàng hay tiền tệ**.

## Trạng thái thật của dự án

- Đây là **mã nguồn**, không phải `.jar` có thể thả vào thư mục `mods`.
- Logic Java độc lập được test qua `tests/test-logic.sh`.
- Chưa biên dịch bằng toàn bộ Minecraft 1.21.11/Fabric API, chưa thử trên Zalith Launcher và chưa thử chung với 50 mod của server Zero Call. Vì vậy có thể cần sửa tương thích API sau lần build đầu.
- Nền tảng: Minecraft Java **1.21.11**, **Fabric**, Fabric API, **JDK 21**.

## 8 class và buff riêng

| Class | Gõ lệnh chọn | Buff chính | MP | Hồi chiêu |
|---|---|---|---:|---:|
| Chiến binh | `/rpg class warrior` | Strength I 12s + Resistance I 8s | 30 | 25s |
| Pháp sư | `/rpg class mage` | Regeneration I 6s + Resistance I 8s | 40 | 28s |
| Cung thủ | `/rpg class archer` | Speed II 12s + Night Vision 15s | 25 | 22s |
| Sát thủ | `/rpg class assassin` | Invisibility 8s + Speed II 8s | 30 | 30s |
| Thánh kỵ sĩ | `/rpg class paladin` | Absorption II 15s + Regeneration I 8s | 45 | 35s |
| Cuồng chiến | `/rpg class berserker` | Strength II 8s + Weakness I 8s (đánh đổi) | 40 | 32s |
| Druid | `/rpg class druid` | Regeneration II 8s + Resistance I 10s | 40 | 30s |
| Võ tăng | `/rpg class monk` | Haste II 12s + Resistance I 10s | 30 | 25s |

Hiện mỗi class **có một kỹ năng kích hoạt tổ hợp 2 buff**, qua `/rpg skill`, không phải một cây kỹ năng đa nhánh. Mỗi người chọn class **một lần** để tránh đổi nghề vô hạn. Admin có thể sửa file save sau khi backup nếu cần. MP tự hồi 3/giây. Level lên bằng đánh quái, nhận 3 điểm mỗi cấp (STR/VIT/AGI/INT).

## Quái thường, Elite, Champion

- Chỉ những mob vanilla được liệt kê sau mới có cơ hội nâng cấp: Zombie, Husk, Drowned, Skeleton, Stray, Spider, Cave Spider.
- Khi entity được nạp lần đầu: **3% Elite**, **0,5% Champion**, còn lại thường. Có tag để không nâng cấp lại lúc reload chunk.
- Elite tăng máu x1.8, Champion x3.0, tăng sát thương tối đa x2; bảng tên nổi và thêm XP, rơi emerald có tỉ lệ. Không thêm model/animation/shader, không quét mob mỗi tick.
- Boss dungeon tăng máu x4, thưởng XP, 4 kim cương và 1 táo vàng khi bị người chơi hạ. Có buff giám sát tự động chỉ lúc mob load.

## Dungeon — bố trí 3 phòng và quái

**Bản này KHÔNG sinh dungeon tự nhiên**: chủ server/OP dùng lệnh đặt một dungeon 3 phòng trong Overworld để mọi người cùng tới chiến đấu. Không có nhiệm vụ hoặc yêu cầu giết X quái để nhận thưởng.

| Dungeon | Tạo bởi OP | Phòng 1–2 | Boss |
|---|---|---|---|
| Hầm Mộ | `/rpg dungeon create crypt` | Zombie / Skeleton | Wither Skeleton |
| Rừng Nhện | `/rpg dungeon create grove` | Spider / Cave Spider | Witch |
| Pháo Đài | `/rpg dungeon create fortress` | Pillager / Vindicator | Evoker |

- Mỗi dungeon **37 × 13 × 7 block**, tạo từ vị trí người nhập lệnh + (8,0,8).
- Có 3 phòng nối nhau, đèn, rương gồm diamond, golden apple và emerald; 4 quái canh + 1 boss.
- **CẢNH BÁO:** Đây là trình đặt công trình bằng block. Lệnh sẽ **ghi đè địa hình và block thường** trong vùng 37 × 13 × 7. Chỉ OP được dùng. Không có chức năng hoàn tác. **BACKUP WORLD** và chọn khu đất trống trước khi thử. Công trình do lệnh tạo sẽ tồn tại trong world.
- Bảo vệ tối thiểu: sẽ từ chối nếu phát hiện block entity (rương, máy móc, sign...) trong vùng đặt; không bảo vệ block xây dựng thường. Không triển khai trong Nether/End.
- Boss được tạo khi đặt dungeon, **không tự hồi sinh**; muốn chơi lại dungeon phải tạo công trình mới hoặc tái tạo quái thủ công. Chưa có reset dungeon định kỳ hoặc xác thực đủ quái mới mở chest.

## HUD

HUD đơn giản ở góc trái: tên mod, level, class, HP, MP, XP, điểm cộng và số Elite/Boss đã giết. Không thêm shader hoặc texture lớn. **Ảnh mô phỏng HUD trước đó không phải giao diện đã lập trình**: vẫn chưa có nút chạm class/skill hình tròn như ảnh.

## Danh sách lệnh

```text
/rpg                   Xem level, class, stat, mob, boss
/rpg help              Xem lệnh
/rpg classes           Xem 8 nghề và buff
/rpg class warrior     Chọn class một lần (hoặc mage, archer, assassin, paladin, berserker, druid, monk)
/rpg skill             Dùng buff class
/rpg add str           +1 sức mạnh (hoặc vit / agi / int)
/rpg dungeon           Xem dungeon và cảnh báo
/rpg dungeon create crypt     OP tạo Hầm Mộ
/rpg dungeon create grove     OP tạo Rừng Nhện
/rpg dungeon create fortress  OP tạo Pháo Đài
```

## Cách build .jar trên Windows

1. Cài JDK 21, kiểm tra `java -version`.
2. Giải nén source và chạy `TAO_FILE_JAR.bat` (Internet để tải Gradle/Minecraft/Fabric).
3. Nếu build thành công lấy **duy nhất file `.jar` không có `-sources`** từ `JAR_DE_CAI` rồi cài vào Fabric **1.21.11** với Fabric API bản tương thích.
4. Chơi nhiều người: cần cài mod + Fabric API ở **server và client** nếu muốn dùng HUD. Sao lưu world và bản save cũ trước khi cập nhật.

Nếu build báo lỗi, gửi ảnh màn hình log để chỉnh mã nguồn. Kiểm tra xung đột với các mod sửa attribute, mob AI, worldgen trước khi dùng cho Zero Call thật.

## Dữ liệu thế giới

Tệp `world/data/zerocall_rpg.json`. Có giữ các field cũ tương thích Gson khi nâng cấp từ v0.1.0; các field nhiệm vụ/shop cũ bị bỏ qua. Không xóa file này nếu muốn giữ level. Có thể xóa những field không còn dùng sau khi sao lưu.
