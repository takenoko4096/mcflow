package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.value.block.BlockType
import io.github.takenoko4096.bcl.reader.TokenSeqReader

class BlockTypeArgumentType : ArgumentType<BlockType>("block_type") {
    override fun parse(reader: TokenSeqReader): Argument<BlockType> {
        val start = reader.getCurrentTokenStartPosition()
        val identifier = reader.readNamespacedIdentifier()
        val end = reader.getPreviousTokenEndPosition()

        for (entry in BlockType.entries) {
            if (identifier == entry.getIdentifier()) {
                return Argument(this, entry, start..end)
            }
        }

        throw reader.exception(start..end, "不明なブロックです: $identifier")
    }
}
