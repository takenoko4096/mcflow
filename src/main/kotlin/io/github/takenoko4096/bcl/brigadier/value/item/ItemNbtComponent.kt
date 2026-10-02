package io.github.takenoko4096.bcl.brigadier.value.item

import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier
import io.github.takenoko4096.bcl.brigadier.value.block.BlockType
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonObject
import io.github.takenoko4096.bcl.brigadier.value.misc.ItemLockMode
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

sealed class ItemNbtComponent(
    val identifier: NamespacedIdentifier,
    val rawValue: CommandJsonObject
) {
    class CanPlaceOn(val blocks: List<BlockType>, raw: CommandJsonObject) : ItemNbtComponent(ItemNbtComponents.CAN_PLACE_ON, raw) {
        override fun toString(): String {
            return "{blocks: $blocks}"
        }
    }

    class CanDestroy(val blocks: List<BlockType>, raw: CommandJsonObject) : ItemNbtComponent(ItemNbtComponents.CAN_DESTROY, raw) {
        override fun toString(): String {
            return "{blocks: $blocks}"
        }
    }

    class ItemLock(val mode: ItemLockMode, raw: CommandJsonObject) : ItemNbtComponent(ItemNbtComponents.ITEM_LOCK, raw) {
        override fun toString(): String {
            return "{mode: $mode}"
        }
    }

    class KeepOnDeath(raw: CommandJsonObject) : ItemNbtComponent(ItemNbtComponents.KEEP_ON_DEATH, raw) {
        override fun toString(): String {
            return "{}"
        }
    }
}
