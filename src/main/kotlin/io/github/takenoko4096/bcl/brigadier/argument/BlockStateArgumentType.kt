package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.block.BlockProperties
import io.github.takenoko4096.bcl.brigadier.value.block.BlockState
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.Symbol

class BlockStateArgumentType(
    private val blockTypeArgumentType: BlockTypeArgumentType,
    private val blockPropertiesArgumentType: BlockPropertiesArgumentType
) : ArgumentType<BlockState>("block_state") {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<BlockState> {
        val blockTypeParseResult = blockTypeArgumentType.parse(reader, diagnostics, semanticTokens)

        val blockPropertiesParseResult = if (!reader.isOver() && reader.peek() `is` Symbol.ARRAY_START) {
            blockPropertiesArgumentType.parse(reader, diagnostics, semanticTokens)
        }
        else null

        val mergedSpan = blockTypeParseResult.sourceSpan.start..(blockPropertiesParseResult?.sourceSpan?.end ?: blockTypeParseResult.sourceSpan.end)

        if (blockTypeParseResult is Argument) {
            val blockType = blockTypeParseResult.value

            return when (blockPropertiesParseResult) {
                is Argument -> {
                    val properties = blockPropertiesParseResult.value
                    for (entry in properties) {
                        if (entry.property !in blockTypeParseResult.value) {
                            diagnostics.add(reader.diagnostic(blockPropertiesParseResult.sourceSpan, "プロパティ '${entry.property.name}' はブロック '${blockType.identifier}' に存在しません"))
                        }
                    }
                    Argument(this, BlockState(blockType, properties), mergedSpan)
                }
                null -> Argument(this, BlockState(blockType, BlockProperties()), mergedSpan)
                else -> {
                    blockPropertiesParseResult as NoValuedArgument
                    NoValuedArgument(this, mergedSpan, diagnostics)
                }
            }
        }

        return NoValuedArgument(this,mergedSpan, diagnostics)
    }

    companion object {
        fun blockState() = BlockStateArgumentType(BlockTypeArgumentType(), BlockPropertiesArgumentType())
    }
}
