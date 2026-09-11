package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.value.block.BlockProperties
import io.github.takenoko4096.bcl.brigadier.value.block.BlockState
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.Symbol

class BlockStateArgumentType(
    private val blockTypeArgumentType: BlockTypeArgumentType,
    private val blockPropertiesArgumentType: BlockPropertiesArgumentType
) : ArgumentType<BlockState>("block_state") {
    override fun parse(reader: TokenSeqReader): Argument<BlockState> {
        val blockTypeArg = blockTypeArgumentType.parse(reader)
        val propertiesArg = if (!reader.isOver() && reader.peek() `is` Symbol.ARRAY_START) blockPropertiesArgumentType.parse(reader) else null

        val blockType = blockTypeArg.value
        val properties = propertiesArg?.value ?: BlockProperties()

        for (entry in properties) {
            if (entry.property !in blockTypeArg.value) {
                throw reader.exception(propertiesArg!!.sourceSpan, "プロパティ '${entry.property.name}' はブロック '${blockType.identifier}' に存在しません")
            }
        }

        val mergedSpan = blockTypeArg.sourceSpan.start..(propertiesArg?.sourceSpan?.end ?: blockTypeArg.sourceSpan.end)

        return Argument(this, BlockState(blockType, properties), mergedSpan)
    }

    companion object {
        fun blockState() = BlockStateArgumentType(BlockTypeArgumentType(), BlockPropertiesArgumentType())
    }
}
