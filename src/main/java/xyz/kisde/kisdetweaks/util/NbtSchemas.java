package xyz.kisde.kisdetweaks.util;

import xyz.kisde.kisdetweaks.util.NbtSuggestions.Tag;
import static xyz.kisde.kisdetweaks.util.NbtSuggestions.compound;
import static xyz.kisde.kisdetweaks.util.NbtSuggestions.list;

/** Vanilla 1.12.2 tag names/types. These are editing hints, never saved or sent automatically. */
final class NbtSchemas {
    private NbtSchemas() {}
    private static final Tag STRING = new Tag("string");
    private static final Tag FLOAT = new Tag("float");
    private static final Tag DOUBLE = new Tag("double");
    private static final Tag EFFECT = compound().fields("byte", "Id", "Amplifier", "Ambient", "ShowParticles")
            .fields("int", "Duration");
    private static final Tag ENCHANTMENT = compound().fields("short", "id", "lvl");
    private static final Tag ATTRIBUTE = compound().fields("string", "Name").fields("double", "Base")
            .put("Modifiers", list(compound().fields("string", "Name").fields("double", "Amount")
                    .fields("int", "Operation").fields("long", "UUIDMost", "UUIDLeast")));
    private static final Tag ATTRIBUTE_MODIFIER = compound().fields("string", "AttributeName", "Name", "Slot")
            .fields("double", "Amount").fields("int", "Operation").fields("long", "UUIDMost", "UUIDLeast");
    private static final Tag DISPLAY = compound().fields("string", "Name", "LocName")
            .fields("int", "color").put("Lore", list(STRING));
    private static final Tag PROFILE = compound().fields("string", "Id", "Name")
            .put("Properties", compound().put("textures", list(compound().fields("string", "Value", "Signature"))));
    private static final Tag ITEM = compound().fields("int", "RepairCost", "HideFlags")
            .fields("byte", "Unbreakable").put("display", DISPLAY)
            .put("ench", list(ENCHANTMENT)).put("StoredEnchantments", list(ENCHANTMENT))
            .put("AttributeModifiers", list(ATTRIBUTE_MODIFIER))
            .put("CanDestroy", list(STRING)).put("CanPlaceOn", list(STRING));
    private static final Tag STACK = compound().fields("string", "id").fields("byte", "Count", "Slot")
            .fields("short", "Damage").put("tag", ITEM);
    private static final Tag ENTITY = compound().fields("string", "id", "CustomName")
            .fields("byte", "CustomNameVisible", "Silent", "NoGravity", "Glowing", "Invulnerable", "OnGround")
            .fields("int", "Dimension", "PortalCooldown").fields("short", "Fire", "Air")
            .fields("float", "FallDistance").fields("long", "UUIDMost", "UUIDLeast")
            .put("Pos", list(DOUBLE)).put("Motion", list(DOUBLE)).put("Rotation", list(FLOAT))
            .put("Tags", list(STRING));
    private static final Tag LIVING = compound().fields("float", "Health", "AbsorptionAmount")
            .fields("short", "HurtTime", "DeathTime").fields("int", "HurtByTimestamp")
            .fields("byte", "FallFlying", "PersistenceRequired", "CanPickUpLoot", "LeftHanded", "NoAI", "Leashed")
            .put("ActiveEffects", list(EFFECT)).put("Attributes", list(ATTRIBUTE))
            .put("HandItems", list(STACK)).put("ArmorItems", list(STACK))
            .put("HandDropChances", list(FLOAT)).put("ArmorDropChances", list(FLOAT))
            .put("Leash", compound().fields("long", "UUIDMost", "UUIDLeast").fields("int", "X", "Y", "Z"))
            .put("DeathLootTableSeed", new Tag("long")).fields("string", "DeathLootTable");
    private static final Tag BLOCK = compound().fields("string", "id").fields("int", "x", "y", "z");
    private static final Tag INVENTORY = compound().fields("string", "CustomName", "Lock", "LootTable")
            .fields("long", "LootTableSeed").put("Items", list(STACK));
    private static final Tag SPAWNER = compound().fields("short", "Delay", "MinSpawnDelay", "MaxSpawnDelay", "SpawnCount",
            "MaxNearbyEntities", "RequiredPlayerRange", "SpawnRange").put("SpawnData", ENTITY)
            .put("SpawnPotentials", list(compound().fields("int", "Weight").put("Entity", ENTITY)));

    static {
        Tag passenger = copy(ENTITY, LIVING);
        passenger.put("Passengers", list(passenger));
        ENTITY.put("Passengers", list(passenger));
        ITEM.put("BlockEntityTag", block(""));
        Tag anyEntity = entity("");
        anyEntity.put("Passengers", list(anyEntity));
        ENTITY.put("Passengers", list(anyEntity));
        ITEM.put("EntityTag", anyEntity);
    }

    private static Tag copy(Tag... parts) {
        Tag result = compound();
        for (Tag part : parts) result.children.putAll(part.children);
        return result;
    }

    private static String path(String name) {
        return name.startsWith("minecraft:") ? name.substring(10) : name;
    }

    static Tag item(String name) {
        Tag result = copy(ITEM);
        String id = path(name);
        if (id.isEmpty() || id.contains("potion") || id.equals("tipped_arrow")) {
            result.fields("string", "Potion").fields("int", "CustomPotionColor").put("CustomPotionEffects", list(EFFECT));
        }
        if (id.isEmpty() || id.equals("written_book") || id.equals("writable_book")) {
            result.fields("string", "title", "author").fields("int", "generation").fields("byte", "resolved")
                    .put("pages", list(STRING));
        }
        if (id.isEmpty() || id.equals("skull")) result.put("SkullOwner", PROFILE);
        Tag explosion = compound().fields("byte", "Type", "Trail", "Flicker")
                .fields("int array", "Colors", "FadeColors");
        if (id.isEmpty() || id.equals("fireworks")) {
            result.put("Fireworks", compound().fields("byte", "Flight").put("Explosions", list(explosion)));
        }
        if (id.isEmpty() || id.equals("firework_charge")) result.put("Explosion", explosion);
        if (id.isEmpty() || id.equals("enchanted_book")) result.put("StoredEnchantments", list(ENCHANTMENT));
        if (id.isEmpty() || id.equals("filled_map")) {
            result.fields("byte", "map_is_scaling", "map_tracking_position", "map_unlimited_tracking")
                    .put("Decorations", list(compound().fields("string", "id").fields("byte", "type")
                            .fields("double", "x", "z", "rot")));
        }
        if (id.isEmpty() || id.equals("knowledge_book")) result.put("Recipes", list(STRING));
        return result;
    }

    static Tag entity(String name) {
        String id = path(name);
        boolean nonliving = ("|item|xp_orb|area_effect_cloud|painting|item_frame|leash_knot|ender_crystal|"
                + "arrow|spectral_arrow|snowball|egg|fireball|small_fireball|dragon_fireball|wither_skull|"
                + "ender_pearl|eye_of_ender_signal|potion|xp_bottle|fireworks_rocket|tnt|falling_block|"
                + "llama_spit|shulker_bullet|boat|minecart|chest_minecart|furnace_minecart|hopper_minecart|tnt_minecart|"
                + "spawner_minecart|commandblock_minecart|evocation_fangs|").contains("|" + id + "|");
        Tag result = nonliving ? copy(ENTITY) : copy(ENTITY, LIVING);
        if (id.isEmpty() || isAnimal(id)) result.fields("int", "Age", "ForcedAge", "InLove");
        if (id.isEmpty() || id.equals("armor_stand")) {
            Tag pose = compound();
            for (String part : new String[] {"Head", "Body", "LeftArm", "RightArm", "LeftLeg", "RightLeg"}) pose.put(part, list(FLOAT));
            result.fields("byte", "Small", "ShowArms", "NoBasePlate", "Marker", "Invisible").fields("int", "DisabledSlots").put("Pose", pose);
        }
        if (id.isEmpty() || id.contains("zombie")) result.fields("byte", "IsBaby", "CanBreakDoors");
        if (id.isEmpty() || id.equals("zombie_villager")) result.fields("int", "Profession", "ConversionTime").fields("long", "ConversionPlayerMost", "ConversionPlayerLeast");
        if (id.isEmpty() || id.equals("creeper")) result.fields("byte", "powered", "ignited")
                .fields("short", "Fuse").fields("byte", "ExplosionRadius");
        if (id.isEmpty() || id.equals("slime") || id.equals("magma_cube")) result.fields("int", "Size").fields("byte", "wasOnGround");
        if (id.isEmpty() || id.equals("sheep")) result.fields("byte", "Color", "Sheared");
        if (id.isEmpty() || id.equals("pig")) result.fields("byte", "Saddle");
        if (id.isEmpty() || id.equals("wolf") || id.equals("ocelot") || id.equals("parrot")) {
            result.fields("string", "OwnerUUID").fields("byte", "Sitting");
            if (id.isEmpty() || id.equals("wolf")) result.fields("byte", "Angry", "CollarColor");
            if (id.isEmpty() || id.equals("ocelot")) result.fields("int", "CatType");
            if (id.isEmpty() || id.equals("parrot")) result.fields("int", "Variant");
        }
        if (id.isEmpty() || id.contains("horse") || id.equals("donkey") || id.equals("mule") || id.equals("llama")) {
            result.fields("byte", "Tame", "Bred", "EatingHaystack")
                    .fields("string", "OwnerUUID").fields("int", "Temper").put("SaddleItem", STACK);
            if (id.isEmpty() || id.equals("horse")) result.fields("int", "Variant").put("ArmorItem", STACK);
            if (id.isEmpty() || id.equals("llama")) result.fields("int", "Variant", "Strength").put("DecorItem", STACK);
            if (id.isEmpty() || id.equals("donkey") || id.equals("mule") || id.equals("llama")) {
                result.fields("byte", "ChestedHorse").put("Items", list(STACK));
            }
        }
        if (id.isEmpty() || id.equals("villager")) {
            Tag trade = compound().fields("byte", "rewardExp").fields("int", "uses", "maxUses")
                    .put("buy", STACK).put("buyB", STACK).put("sell", STACK);
            result.fields("int", "Profession", "Career", "CareerLevel", "Riches")
                    .fields("byte", "Willing").put("Inventory", list(STACK))
                    .put("Offers", compound().put("Recipes", list(trade)));
        }
        if (id.isEmpty() || id.equals("item")) result.put("Item", STACK).fields("short", "Age", "PickupDelay", "Health")
                .fields("string", "Owner", "Thrower");
        if (id.isEmpty() || id.equals("falling_block")) result.fields("string", "Block")
                .fields("byte", "Data", "DropItem", "HurtEntities").fields("int", "Time", "FallHurtMax")
                .fields("float", "FallHurtAmount").put("TileEntityData", block(""));
        if (id.isEmpty() || id.equals("xp_orb")) result.fields("short", "Health", "Age", "Value");
        if (id.isEmpty() || id.equals("area_effect_cloud")) result.fields("int", "Age", "Duration", "WaitTime", "ReapplicationDelay",
                "DurationOnUse", "ParticleParam1", "ParticleParam2", "Color").fields("float", "RadiusOnUse", "RadiusPerTick", "Radius")
                .fields("string", "Particle", "Potion").put("Effects", list(EFFECT));
        if (id.isEmpty() || id.equals("ender_crystal")) result.fields("byte", "ShowBottom")
                .put("BeamTarget", compound().fields("int", "X", "Y", "Z"));
        if (id.isEmpty() || id.equals("bat")) result.fields("byte", "BatFlags");
        if (id.isEmpty() || id.equals("snowman")) result.fields("byte", "Pumpkin");
        if (id.isEmpty() || id.equals("villager_golem")) result.fields("byte", "PlayerCreated");
        if (id.isEmpty() || id.equals("skeleton_horse")) result.fields("byte", "SkeletonTrap").fields("int", "SkeletonTrapTime");
        if (id.isEmpty() || id.equals("wither")) result.fields("int", "Invul");
        if (id.isEmpty() || id.equals("ender_dragon")) result.fields("int", "DragonPhase");
        if (id.isEmpty() || id.equals("endermite")) result.fields("int", "Lifetime").fields("byte", "PlayerSpawned");
        if (id.isEmpty() || id.equals("ghast")) result.fields("int", "ExplosionPower");
        if (id.isEmpty() || id.equals("zombie_pigman")) result.fields("short", "Anger").fields("string", "HurtBy");
        if (id.isEmpty() || id.equals("evocation_illager") || id.equals("illusion_illager")) result.fields("int", "SpellTicks");
        if (id.isEmpty() || id.equals("vex")) result.fields("int", "BoundX", "BoundY", "BoundZ", "LifeTicks");
        if (id.isEmpty() || id.equals("vindication_illager")) result.fields("byte", "Johnny");
        if (id.isEmpty() || id.equals("tnt")) result.fields("short", "Fuse");
        if (id.isEmpty() || id.equals("item_frame")) result.put("Item", STACK)
                .fields("byte", "ItemRotation", "Facing").fields("float", "ItemDropChance");
        if (id.isEmpty() || id.equals("painting")) result.fields("string", "Motive").fields("byte", "Facing");
        if (id.isEmpty() || id.equals("painting") || id.equals("item_frame")) result.fields("int", "TileX", "TileY", "TileZ");
        if (id.isEmpty() || id.contains("minecart")) result.fields("string", "DisplayTile").fields("int", "DisplayData", "DisplayOffset")
                .fields("byte", "CustomDisplayTile");
        if (id.isEmpty() || id.equals("chest_minecart") || id.equals("hopper_minecart")) result.children.putAll(INVENTORY.children);
        if (id.isEmpty() || id.equals("spawner_minecart")) result.children.putAll(SPAWNER.children);
        if (id.isEmpty() || id.equals("commandblock_minecart")) result.fields("string", "Command", "LastOutput")
                .fields("int", "SuccessCount").fields("byte", "TrackOutput");
        if (id.isEmpty() || id.equals("furnace_minecart")) result.fields("double", "PushX", "PushZ").fields("short", "Fuel");
        if (id.isEmpty() || id.equals("hopper_minecart")) result.fields("int", "TransferCooldown").fields("byte", "Enabled");
        if (id.isEmpty() || id.equals("tnt_minecart")) result.fields("int", "TNTFuse");
        if (id.isEmpty() || id.equals("boat")) result.fields("string", "Type");
        if (id.isEmpty() || id.equals("arrow") || id.equals("spectral_arrow")) result.fields("double", "damage")
                .fields("byte", "pickup", "inGround", "shake", "crit", "inData").fields("short", "life")
                .fields("int", "xTile", "yTile", "zTile").fields("string", "inTile");
        if (id.isEmpty() || id.equals("spectral_arrow")) result.fields("int", "Duration");
        if (id.isEmpty() || id.equals("arrow")) result.fields("string", "Potion").fields("int", "Color")
                .put("CustomPotionEffects", list(EFFECT));
        if (id.isEmpty() || id.equals("snowball") || id.equals("egg") || id.equals("ender_pearl")
                || id.equals("potion") || id.equals("xp_bottle")) result.fields("int", "xTile", "yTile", "zTile")
                .fields("string", "inTile", "ownerName").fields("byte", "shake", "inGround");
        if (id.isEmpty() || id.equals("potion")) result.put("Potion", STACK);
        if (id.isEmpty() || id.equals("fireball") || id.equals("small_fireball") || id.equals("dragon_fireball")
                || id.equals("wither_skull")) result.fields("int", "life").put("direction", list(DOUBLE)).put("power", list(DOUBLE));
        if (id.isEmpty() || id.equals("fireball")) result.fields("int", "ExplosionPower");
        if (id.isEmpty() || id.equals("fireworks_rocket")) result.fields("int", "Life", "LifeTime").put("FireworksItem", STACK);
        if (id.isEmpty() || id.equals("evocation_fangs")) result.fields("int", "Warmup");
        Tag owner = compound().fields("long", "M", "L").fields("int", "X", "Y", "Z");
        if (id.isEmpty() || id.equals("llama_spit")) result.put("Owner", compound().fields("long", "OwnerUUIDMost", "OwnerUUIDLeast"));
        if (id.isEmpty() || id.equals("shulker_bullet")) result.put("Owner", owner).put("Target", owner)
                .fields("int", "Dir", "Steps").fields("double", "TXD", "TYD", "TZD");
        if (id.isEmpty() || id.equals("enderman")) result.fields("short", "carried", "carriedData");
        if (id.isEmpty() || id.equals("chicken")) result.fields("byte", "IsChickenJockey").fields("int", "EggLayTime");
        if (id.isEmpty() || id.equals("rabbit")) result.fields("int", "RabbitType", "MoreCarrotTicks");
        if (id.isEmpty() || id.equals("shulker")) result.fields("byte", "AttachFace", "Peek", "Color")
                .fields("int", "APX", "APY", "APZ");
        return result;
    }

    private static boolean isAnimal(String id) {
        return ("|pig|sheep|cow|mooshroom|chicken|rabbit|wolf|ocelot|parrot|horse|donkey|mule|llama|polar_bear|")
                .contains("|" + id + "|");
    }

    static Tag block(String name) {
        String id = path(name);
        Tag result = copy(BLOCK);
        boolean all = id.isEmpty();
        if (all || id.contains("chest") || id.contains("shulker_box") || id.equals("hopper")
                || id.equals("furnace") || id.equals("lit_furnace") || id.equals("dispenser")
                || id.equals("dropper") || id.equals("brewing_stand")) result.children.putAll(INVENTORY.children);
        if (all || id.contains("furnace")) result.fields("short", "BurnTime", "CookTime", "CookTimeTotal");
        if (all || id.equals("hopper")) result.fields("int", "TransferCooldown");
        if (all || id.equals("brewing_stand")) result.fields("short", "BrewTime").fields("byte", "Fuel");
        if (all || id.contains("command_block")) result.fields("string", "Command", "LastOutput", "CustomName")
                .fields("int", "SuccessCount").fields("byte", "TrackOutput", "powered", "auto", "conditionMet", "UpdateLastExecution")
                .fields("long", "LastExecution");
        if (all || id.equals("standing_sign") || id.equals("wall_sign")) result.fields("string", "Text1", "Text2", "Text3", "Text4");
        if (all || id.equals("mob_spawner")) result.children.putAll(SPAWNER.children);
        if (all || id.equals("skull")) result.fields("byte", "SkullType", "Rot").put("Owner", PROFILE);
        if (all || id.contains("banner")) result.fields("string", "CustomName").fields("int", "Base").put("Patterns", list(compound().fields("string", "Pattern").fields("int", "Color")));
        if (all || id.equals("powered_comparator") || id.equals("unpowered_comparator")) result.fields("int", "OutputSignal");
        if (all || id.equals("piston_extension")) result.fields("int", "blockId", "blockData", "facing")
                .fields("float", "progress").fields("byte", "extending", "source");
        if (all || id.equals("beacon")) result.fields("int", "Primary", "Secondary", "Levels");
        if (all || id.equals("noteblock")) result.fields("byte", "note", "powered");
        if (all || id.equals("jukebox")) result.put("RecordItem", STACK);
        if (all || id.equals("flower_pot")) result.fields("string", "Item").fields("int", "Data");
        if (all || id.equals("bed")) result.fields("int", "color");
        if (all || id.equals("structure_block")) result.fields("string", "name", "author", "metadata", "mirror", "rotation", "mode")
                .fields("int", "posX", "posY", "posZ", "sizeX", "sizeY", "sizeZ")
                .fields("byte", "ignoreEntities", "powered", "showair", "showboundingbox")
                .fields("float", "integrity").fields("long", "seed");
        if (all || id.equals("enchanting_table")) result.fields("string", "CustomName");
        if (all || id.equals("end_gateway")) result.fields("long", "Age").fields("byte", "ExactTeleport")
                .put("ExitPortal", compound().fields("int", "X", "Y", "Z"));
        return result;
    }
}
