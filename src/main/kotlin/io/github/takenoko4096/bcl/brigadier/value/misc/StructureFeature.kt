package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier

enum class StructureFeature(val identifier: NamespacedIdentifier) {
    END_CITY(NamespacedIdentifier.defaultNamespaced("end_city")),
    FORTRESS(NamespacedIdentifier.defaultNamespaced("fortress")),
    MINESHAFT(NamespacedIdentifier.defaultNamespaced("mineshaft")),
    MONUMENT(NamespacedIdentifier.defaultNamespaced("monument")),
    STRONGHOLD(NamespacedIdentifier.defaultNamespaced("stronghold")),
    TEMPLE(NamespacedIdentifier.defaultNamespaced("temple")),
    VILLAGE(NamespacedIdentifier.defaultNamespaced("village")),
    MANSION(NamespacedIdentifier.defaultNamespaced("mansion")),
    SHIPWRECK(NamespacedIdentifier.defaultNamespaced("shipwreck")),
    BURIED_TREASURE(NamespacedIdentifier.defaultNamespaced("buried_treasure")),
    RUINS(NamespacedIdentifier.defaultNamespaced("ruins")),
    PILLAGER_OUTPOST(NamespacedIdentifier.defaultNamespaced("pillager_outpost")),
    RUINED_PORTAL(NamespacedIdentifier.defaultNamespaced("ruined_portal")),
    BASTION_REMNANT(NamespacedIdentifier.defaultNamespaced("bastion_remnant")),
    ANCIENT_CITY(NamespacedIdentifier.defaultNamespaced("ancient_city")),
    TRAIL_RUINS(NamespacedIdentifier.defaultNamespaced("trail_ruins")),
    TRIAL_CHAMBERS(NamespacedIdentifier.defaultNamespaced("trial_chambers")),
    ABANDONED_CAMP_BIRCH_FOREST(NamespacedIdentifier.defaultNamespaced("abandoned_camp_birch_forest")),
    ABANDONED_CAMP_BAMBOO_JUNGLE(NamespacedIdentifier.defaultNamespaced("abandoned_camp_bamboo_jungle")),
    ABANDONED_CAMP_CHERRY_GROVE(NamespacedIdentifier.defaultNamespaced("abandoned_camp_cherry_grove")),
    ABANDONED_CAMP_DAPPLED_FOREST(NamespacedIdentifier.defaultNamespaced("abandoned_camp_dappled_forest")),
    ABANDONED_CAMP_EXTREME_HILLS_PLUS_TREES(NamespacedIdentifier.defaultNamespaced("abandoned_camp_extreme_hills_plus_trees")),
    ABANDONED_CAMP_FLOWER_FOREST(NamespacedIdentifier.defaultNamespaced("abandoned_camp_flower_forest")),
    ABANDONED_CAMP_PALE_GARDEN(NamespacedIdentifier.defaultNamespaced("abandoned_camp_pale_garden")),
    ABANDONED_CAMP_SWAMPLAND(NamespacedIdentifier.defaultNamespaced("abandoned_camp_swampland")),
    ABANDONED_CAMP_BIRCH_FOREST_MUTATED(NamespacedIdentifier.defaultNamespaced("abandoned_camp_birch_forest_mutated")),
    ABANDONED_CAMP_COLD_TAIGA(NamespacedIdentifier.defaultNamespaced("abandoned_camp_cold_taiga")),
    ABANDONED_CAMP_FOREST(NamespacedIdentifier.defaultNamespaced("abandoned_camp_forest")),
    ABANDONED_CAMP_JUNGLE_EDGE(NamespacedIdentifier.defaultNamespaced("abandoned_camp_jungle_edge")),
    ABANDONED_CAMP_MEADOW(NamespacedIdentifier.defaultNamespaced("abandoned_camp_meadow")),
    ABANDONED_CAMP_MEGA_TAIGA(NamespacedIdentifier.defaultNamespaced("abandoned_camp_mega_taiga")),
    ABANDONED_CAMP_MESA_PLATEAU_STONE(NamespacedIdentifier.defaultNamespaced("abandoned_camp_plateau_stone")),
    ABANDONED_CAMP_REDWOOD_TAIGA_MUTATED(NamespacedIdentifier.defaultNamespaced("abandoned_camp_redwood_taiga_mutated")),
    ABANDONED_CAMP_SAVANNA(NamespacedIdentifier.defaultNamespaced("abandoned_camp_savanna")),
    ABANDONED_CAMP_TAIGA(NamespacedIdentifier.defaultNamespaced("abandoned_camp_taiga"));

    companion object {
        val IDENTIFIERS = entries.map { it.identifier.toString() }.toTypedArray()
    }
}
