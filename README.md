# 🧟 Zombie Assassin Mod (Minecraft Forge 1.20.1)

> A feature-packed Minecraft Forge 1.20.1 mod introducing custom dimensions, automated 10x10 trap rooms, ocean structures, cooked weapons, multi-tools, and custom mobs!

---

## 🎮 Beginner Guide — How to Install & Play

### 📥 1. Quick Installation (For Players)
1. Download and install **Minecraft 1.20.1**.
2. Download and install **[Minecraft Forge 47.2.0+](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html)**.
3. Download `zombieassasian-1.0.0.jar` from the release section.
4. Place `zombieassasian-1.0.0.jar` into your Minecraft `mods` folder:
   * **Windows**: `%appdata%\.minecraft\mods`
   * **Mac**: `~/Library/Application Support/minecraft/mods`
   * **Linux**: `~/.minecraft/mods`
5. Launch Minecraft using the **Forge 1.20.1** launcher profile!

---

### 🕹️ 2. In-Game Features & Commands

| Feature | Description | How to Use / Commands |
| :--- | :--- | :--- |
| 💎 **Diamond Realm** | A custom Overworld-style dimension where the ground is made of Netherite & Diamond blocks! | Use the **Dimensional Key** item or `/tp @s ~ ~ ~ zombieassasian:diamond_realm` |
| 🏰 **Tower 1 & Pillars** | Ancient custom structures spawning along ocean coasts and beaches. | `/locate structure zombieassasian:tower1`<br>`/locate structure zombieassasian:netherite_pillar` |
| ⚡ **10x10 Trap Room** | Automated anvil crushing trap system! | Build a full **10x10 boundary (36 blocks)** of `Trap Room Core` blocks. Step inside to trigger falling anvils. |
| ⚔️ **Cooked Weapons** | High-durability Cooked Shield & Cooked Diamond Axe. | Found in custom Creative Tab |
| 🪓 **Wooden Multi-Tool** | Combined Axe, Pickaxe, Shovel & Hoe in 1 survival item! | Use for all block harvesting needs |
| 🪟 **Display Block** | Displays any item on a block in 3D. | Right-click with any item |
| 🪣 **Long-Ranged Bucket** | Extended-reach bucket for long-distance liquid collection. | Right-click water or lava from afar |
| 🐶 **Cute Puppy** | Friendly companion mob. | Spawn egg in custom Creative Tab |

---

## 💻 Developer & Modder Guide — Advanced Topics

### 🛠️ 1. Environment & Workspace Setup
* **JDK Requirements**: Java 17 (Eclipse Adoptium or Zulu JDK 17 recommended)
* **Mod Loader**: Minecraft Forge 1.20.1 (MDK 47.2.0)
* **Mappings**: Mojang Official Mappings (`20230612.114412`)

#### Development Commands:
```bash
# Clone the repository
git clone https://github.com/SGoD11/minecraftmod.git

# Generate IDE run configurations
./gradlew genIntellijRuns    # IntelliJ IDEA
./gradlew genEclipseRuns     # Eclipse

# Run client in development mode
./gradlew runClient

# Build production JAR (Outputs to build/libs/zombieassasian-1.0.0.jar)
./gradlew build
```

---

### 🏗️ 2. Codebase Architecture

```
src/main/java/com/dhar/zombieassasian/
├── ZombieAssasianMod.java          # Mod main entry point (@Mod)
├── register/
│   ├── ModRegistries.java          # Central DeferredRegister for Items, Blocks, Entities
│   └── ModCreativeTabs.java        # Custom Creative Tab registration
├── block/
│   ├── TrapRoomCoreBlock.java      # Trap room block implementation with BlockEntityTicker
│   └── DisplayBlock.java           # Interactive display block
├── blockentity/
│   ├── TrapRoomCoreBlockEntity.java# 10x10 perimeter detection & clamped falling anvil logic
│   └── DisplayBlockEntity.java     # Display block NBT & rendering handler
├── handler/
│   ├── LaserTrapHandler.java       # Server-tick laser beam collision & damage solver
│   └── SpyglassVillageHandler.java # Structure locator logic & chunk generation helper
├── item/                           # Custom items (MultiTool, DimensionalKey, CookedShield, etc.)
└── entity/                         # Custom entities (CutePuppyEntity, BurnedArrowEntity)
```

---

### 🔬 3. Deep Dive into Key Technical Systems

#### A. Custom WorldGen & Noise Settings (`diamond_realm`)
- Defined in `resources/data/zombieassasian/worldgen/noise_settings/diamond_realm.json`.
- Utilizes Vanilla density functions (`minecraft:overworld/sloped_cheese`, `minecraft:overworld/base_3d_noise`) for stable chunk generation.
- Applies custom surface rules to substitute top-layer blocks with `minecraft:netherite_block` and subterranean layers with `minecraft:diamond_block`.

#### B. Structure Registry & Jigsaw Pools
- Structures (`tower1`, `netherite_pillar`) are registered via JSON codecs in `worldgen/structure/` and `worldgen/structure_set/`.
- Structure NBT files are located in `data/zombieassasian/structures/`.
- Uses custom biome tags (`#zombieassasian:has_ocean_structure`) referencing `#minecraft:is_ocean`, `#minecraft:is_beach`, and `zombieassasian:diamond_biome` for cross-dimensional structure placement.

#### C. Trap Room 10x10 Algorithm (`TrapRoomCoreBlockEntity`)
- **Perimeter Detection**: Scans candidate 10x10 origins. Requires all 36 border blocks (`EXACT_PERIMETER_BLOCKS = 36`) to be present before room activation.
- **Coordinate Clamping**: Clamps falling block spawn coordinates `clampedX` and `clampedZ` strictly to `[ox + 1, ox + 8]` and `[oz + 1, oz + 8]` inside the 10x10 border, guaranteeing zero boundary clipping or outside anvil drops.
- **Entity Damage**: Applies `FallingBlockEntity.setHurtsEntities(2.0F, 40)` and `entity.hurt(level.damageSources().anvil(...), 6.0F)` across all `LivingEntity` instances (players & mobs).

---

## 📄 License & Credits
* **Developer**: SGoD11
* **License**: MIT
