package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class MonsterEggStoneType : StringRepresentable {
    STONE,
    COBBLESTONE,
    STONE_BRICK,
    MOSSY_STONE_BRICK,
    CRACKED_STONE_BRICK,
    CHISELED_STONE_BRICK;

    override fun getString() = name.lowercase()
}