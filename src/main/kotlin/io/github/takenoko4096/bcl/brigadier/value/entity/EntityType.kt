package io.github.takenoko4096.bcl.brigadier.value.entity

import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier

enum class EntityType(
    namespace: String = NamespacedIdentifier.DEFAULT_NAMESPACE,
    value: String? = null,
    val summonable: Boolean = true
) {
    ALLAY,
    AREA_EFFECT_CLOUD(summonable = false),
    ARMADILLO,
    ARMOR_STAND,
    ARROW,
    AXOLOTL,
    BAT,
    BEE,
    BLAZE,
    BOAT,
    BOGGED,
    BREEZE,
    BREEZE_WIND_CHARGE_PROJECTILE(summonable = false),
    CAMEL,
    CAMEL_HUSK,
    CAT,
    CAVE_SPIDER,
    CHEST_BOAT,
    CHEST_MINECART,
    CHICKEN,
    COD,
    COMMAND_BLOCK_MINECART,
    COPPER_GOLEM,
    COW,
    CREAKING,
    CREEPER,
    CUSHION,
    DOLPHIN,
    DONKEY,
    DRAGON_FIREBALL(summonable = false),
    DROWNED,
    /*`EDITOR:LOCATION_POINTER_IDLE`("editor", "location_pointer_idle"),
    `EDITOR:LOCATION_POINTER_SELECTED`("editor", "location_pointer_selected"),
    `EDITOR:MAP_MARKER`("editor", "map_marker"),
    `EDITOR:RULER_IDLE`("editor", "ruler_idle"),
    `EDITOR:RULER_POINTER_SELECTED`("editor", "ruler_pointer_selected"),*/
    EGG,
    ELDER_GUARDIAN,
    ELDER_GUARDIAN_GHOST,
    ENDER_CRYSTAL,
    ENDER_DRAGON,
    ENDERMAN,
    ENDERMITE,
    EVOCATION_FANG,
    EVOCATION_ILLAGER,
    EYE_OF_ENDER_SIGNAL(summonable = false),
    FALLING_BLOCK(summonable = false),
    FIREBALL(summonable = false),
    FIREWORKS_ROCKET,
    FISHING_HOOK(summonable = false),
    FOX,
    FROG,
    GHAST,
    GLOW_SQUID,
    GOAT,
    GUARDIAN,
    HAPPY_GHAST,
    HOGLIN,
    HOPPER_MINECART,
    HORSE,
    HUSK,
    IRON_GOLEM,
    ITEM(summonable = false),
    LEASH_KNOT,
    LIGHTNING_BOLT,
    LINGERING_POTION(summonable = false),
    LLAMA,
    LLAMA_SPIT(summonable = false),
    MAGMA_CUBE,
    MINECART,
    MOOSHROOM,
    MULE,
    NAUTILUS,
    NPC,
    OCELOT,
    PANDA,
    PARCHED,
    PARROT,
    PHANTOM,
    PIG,
    PIGLIN,
    PIGLIN_BRUTE,
    PILLAGER,
    PLAYER(summonable = false),
    POLAR_BEAR,
    PUFFERFISH,
    RABBIT,
    RAVAGER,
    SALMON,
    SHEEP,
    SHULKER,
    SHULKER_BULLET(summonable = false),
    SILVERFISH,
    SKELETON,
    SKELETON_HORSE,
    SLIME,
    SMALL_FIREBALL(summonable = false),
    SNIFFER,
    SNOW_GOLEM,
    SNOWBALL,
    SPIDER,
    SPLASH_POTION,
    SQUID,
    STRAY,
    STRIDER,
    SULFUR_CUBE,
    TADPOLE,
    THROWN_TRIDENT(summonable = false),
    TNT,
    TNT_MINECART,
    TRADER_LLAMA,
    TROPICALFISH,
    TURTLE,
    VEX,
    VILLAGER,
    VINDICATOR,
    WANDERING_TRADER,
    WARDEN,
    WIND_CHARGE_PROJECTILE,
    WITCH,
    WITHER,
    WITHER_SKELETON,
    WITHER_SKULL(summonable = false),
    WITHER_SKULL_DANGEROUS(summonable = false),
    WOLF,
    XP_BOTTLE,
    XP_ORB,
    ZOGLIN,
    ZOMBIE,
    ZOMBIE_HORSE,
    ZOMBIE_NAUTILUS,
    ZOMBIE_PIGMAN,
    ZOMBIE_VILLAGER;

    val identifier = NamespacedIdentifier(namespace, value ?: name.lowercase())

    companion object {
        val IDENTIFIERS = entries.flatMap {
            if (it.identifier.namespace == NamespacedIdentifier.DEFAULT_NAMESPACE) {
                listOf(it.identifier.toString(), it.identifier.value)
            }
            else {
                listOf(it.identifier.toString())
            }
        }

        val SUMMONABLE_IDENTIFIERS = entries.filter { it.summonable }.flatMap {
            if (it.identifier.namespace == NamespacedIdentifier.DEFAULT_NAMESPACE) {
                listOf(it.identifier.toString(), it.identifier.value)
            }
            else {
                listOf(it.identifier.toString())
            }
        }

        fun get(identifier: NamespacedIdentifier): EntityType? {
            return entries.find { it.identifier == identifier }
        }
    }
}
