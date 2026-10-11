package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier
import io.github.takenoko4096.bcl.brigadier.value.block.BlockProperties
import io.github.takenoko4096.bcl.brigadier.value.block.BlockProperty
import io.github.takenoko4096.bcl.brigadier.value.block.BlockState
import io.github.takenoko4096.bcl.brigadier.value.block.BlockType
import io.github.takenoko4096.bcl.completer.ArgumentCompleter
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.Symbol
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind

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

    override fun complete(context: BclCompletionContext): List<CompletionItem> {
        val completer = ArgumentCompleter(context)

        val blockTypeCompleteResult = completer.completeUnquotedString(BlockType.IDENTIFIERS, CompletionItemKind.EnumMember)
        if (blockTypeCompleteResult is ArgumentCompleter.ShouldComplete) {
            return blockTypeCompleteResult.items
        }
        blockTypeCompleteResult as ArgumentCompleter.ShouldContinue
        completer.skipWhitespaceAndComments()

        val openResult = completer.completeChar(listOf('['), CompletionItemKind.Keyword)
        if (openResult is ArgumentCompleter.ShouldComplete) {
            return openResult.items
        }
        completer.skipWhitespaceAndComments()

        val blockType = NamespacedIdentifier.parse(blockTypeCompleteResult.input)?. run(BlockType::get)
        val properties = blockType?.properties ?: emptySet()

        while (true) {
            val propertyNameCompletionResult = completer.completeQuotedString(properties.map { it.name }, CompletionItemKind.Property)
            if (propertyNameCompletionResult is ArgumentCompleter.ShouldComplete) {
                return propertyNameCompletionResult.items
            }
            propertyNameCompletionResult as ArgumentCompleter.ShouldContinue
            completer.skipWhitespaceAndComments()

            val property = BlockProperties.getProperty(propertyNameCompletionResult.input)

            (completer.completeChar(
                listOf('='),
                CompletionItemKind.Keyword
            ) as? ArgumentCompleter.ShouldComplete)?.run {
                return items
            }
            completer.skipWhitespaceAndComments()

            val propertyValueCompletionResult = if (property is BlockProperty.EnumProperty<*>) {
                completer.completeQuotedString(property.values.map { it.getString() }, CompletionItemKind.Value)
            }
            else {
                completer.completeUnquotedString(property?.strings?.values?.toList() ?: emptyList(), CompletionItemKind.Value)
            }

            if (propertyValueCompletionResult is ArgumentCompleter.ShouldComplete) {
                return propertyValueCompletionResult.items
            }
            completer.skipWhitespaceAndComments()

            val lastResult = completer.completeChar(listOf(',', ']'), CompletionItemKind.Keyword)
            if (lastResult is ArgumentCompleter.ShouldComplete) {
                return lastResult.items
            }
            completer.skipWhitespaceAndComments()
            lastResult as ArgumentCompleter.ShouldContinue

            if (lastResult.input == ']'.toString()) {
                break
            }
        }

        return emptyList()
    }

    companion object {
        fun blockState() = BlockStateArgumentType(BlockTypeArgumentType(), BlockPropertiesArgumentType())
    }
}
