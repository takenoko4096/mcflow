package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.value.ItemType
import io.github.takenoko4096.bcl.reader.TokenSeqReader

class ItemTypeArgumentType : ArgumentType<ItemType>("item_type") {
    override fun parse(reader: TokenSeqReader): Argument<ItemType> {
        val start = reader.getCurrentTokenStartPosition()
        val identifier = reader.readIdentifier()
        val end = reader.getPreviousTokenEndPosition()

        for (entry in ItemType.entries) {
            if (identifier == entry.identifier) {
                return Argument(this, entry, start..end)
            }
        }

        throw reader.exception(start..end, "不明なアイテムです: $identifier")
    }
}
