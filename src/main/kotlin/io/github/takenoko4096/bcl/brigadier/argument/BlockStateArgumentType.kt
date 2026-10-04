package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.CommandLineParseResult
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier
import io.github.takenoko4096.bcl.brigadier.value.block.BlockProperties
import io.github.takenoko4096.bcl.brigadier.value.block.BlockState
import io.github.takenoko4096.bcl.brigadier.value.block.BlockType
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import io.github.takenoko4096.bcl.tokenizer.Symbol
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.jsonrpc.messages.Either

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

    override fun complete(sourceSpan: SourceSpan?, string: String, reader: TokenSeqReader, result: CommandLineParseResult): List<CompletionItem> {
        val s = reader.readCommandString()

        if (reader.isOver()) {
            return BlockType.IDENTIFIERS
                .filter { it.startsWith(s) }
                .map {
                    CompletionItem(it).apply {
                        textEdit = sourceSpan?.run {
                            Either.forLeft(TextEdit(sourceSpan.toLsp4jRange(), it))
                        }
                        kind = CompletionItemKind.EnumMember
                    }
                }
        }

        if (reader.isOver()) {
            return listOf(CompletionItem(Symbol.ARRAY_START.value).apply { kind = CompletionItemKind.Operator })
        }

        reader.expect(Symbol.ARRAY_START)

        val identifier = NamespacedIdentifier.parse(s)

        val blockType = identifier?.run { BlockType.get(this) }
        val blockProperties = blockType?.run { this.properties } ?: emptySet()

        if (reader.isOver()) {
            return blockProperties.map { CompletionItem("\"${it.name}\"").apply { kind = CompletionItemKind.EnumMember } }
        }

        while (!reader.isOver()) {
            val name = reader.readQuotedString()

            if (reader.isOver()) {
                return listOf(CompletionItem(Symbol.ASSIGN.value).apply { kind = CompletionItemKind.Operator })
            }

            reader.expect(Symbol.ASSIGN)

            val property = blockProperties.find { it.name == name }

            if (reader.isOver()) {
                if (property == null) return emptyList()

                return property.values.map {
                    CompletionItem("\"${it.toString().lowercase()}\"").apply { kind = CompletionItemKind.Value }
                }
            }

            reader.expect(Symbol.COMMA)
        }

        reader.expect(Symbol.ARRAY_END)

        return emptyList()
    }

    companion object {
        fun blockState() = BlockStateArgumentType(BlockTypeArgumentType(), BlockPropertiesArgumentType())
    }
}
