package io.github.takenoko4096.bcl.brigadier.value.item

import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier
import io.github.takenoko4096.bcl.brigadier.value.block.BlockType
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonObject
import io.github.takenoko4096.bcl.brigadier.value.misc.ItemLockMode
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

sealed class ItemNbtComponent(
    val identifier: NamespacedIdentifier,
    val keySpan: SourceSpan,
    val rawValue: CommandJsonObject
) {
    class CanPlaceOn(val blocks: List<BlockType>, keySpan: SourceSpan, raw: CommandJsonObject) : ItemNbtComponent(ItemNbtComponents.CAN_PLACE_ON, keySpan, raw) {
        override fun toString(): String {
            return "{blocks: $blocks}"
        }
    }

    class CanDestroy(val blocks: List<BlockType>, keySpan: SourceSpan, raw: CommandJsonObject) : ItemNbtComponent(ItemNbtComponents.CAN_DESTROY, keySpan, raw) {
        override fun toString(): String {
            return "{blocks: $blocks}"
        }
    }

    class ItemLock(val mode: ItemLockMode, keySpan: SourceSpan, raw: CommandJsonObject) : ItemNbtComponent(ItemNbtComponents.ITEM_LOCK, keySpan, raw) {
        override fun toString(): String {
            return "{mode: $mode}"
        }
    }

    class KeepOnDeath(keySpan: SourceSpan, raw: CommandJsonObject) : ItemNbtComponent(ItemNbtComponents.KEEP_ON_DEATH, keySpan, raw) {
        override fun toString(): String {
            return "{}"
        }
    }
}
