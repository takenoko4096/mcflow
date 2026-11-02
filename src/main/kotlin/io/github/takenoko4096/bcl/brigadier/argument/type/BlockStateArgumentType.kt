package io.github.takenoko4096.bcl.brigadier.argument.type

import io.github.takenoko4096.bcl.brigadier.argument.Argument
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseResult
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseContext
import io.github.takenoko4096.bcl.brigadier.argument.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier
import io.github.takenoko4096.bcl.brigadier.value.block.BlockProperties
import io.github.takenoko4096.bcl.brigadier.value.block.BlockProperty
import io.github.takenoko4096.bcl.brigadier.value.block.BlockState
import io.github.takenoko4096.bcl.brigadier.value.block.VanillaBlockType
import io.github.takenoko4096.bcl.completer.BclCompleter
import io.github.takenoko4096.bcl.tokenizer.`!is`
import io.github.takenoko4096.bcl.tokenizer.Symbol
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind

class BlockStateArgumentType : ArgumentType<BlockState>("block_state") {
    override fun parse(context: ArgumentParseContext): ArgumentParseResult<BlockState> {
        val reader = context.reader
        val diagnostics = context.diagnostics
        // val semanticTokens = context.semanticTokens

        val blockTypeArgumentType = BlockTypeArgumentType()

        val blockTypeParseResult = blockTypeArgumentType.parse(context)

        if (blockTypeParseResult !is Argument) {
            return NoValuedArgument(this,blockTypeParseResult.sourceSpan, diagnostics)
        }

        val blockType = blockTypeParseResult.value

        val blockPropertiesArgumentType = BlockPropertiesArgumentType(blockType)

        if (reader.isOver() || reader.peek() `!is` Symbol.ARRAY_START) {
            return Argument(this, BlockState(blockType, BlockProperties()), blockTypeParseResult.sourceSpan)
        }

        val blockPropertiesParseResult = blockPropertiesArgumentType.parse(context)

        val mergedSpan = blockTypeParseResult.sourceSpan.start..blockPropertiesParseResult.sourceSpan.end

        if (blockPropertiesParseResult !is Argument) {
            return NoValuedArgument(this, mergedSpan, diagnostics)
        }

        val blockProperties = blockPropertiesParseResult.value

        return Argument(this, BlockState(blockType, blockProperties), mergedSpan)
    }

    override fun complete(context: BclCompletionContext): List<CompletionItem> {
        val completer = BclCompleter(context)

        val blockTypeCompleteResult = completer.completeUnquotedString(VanillaBlockType.IDENTIFIERS, CompletionItemKind.EnumMember)
        if (blockTypeCompleteResult is BclCompleter.ShouldComplete) {
            return blockTypeCompleteResult.items
        }
        blockTypeCompleteResult as BclCompleter.ShouldContinue
        completer.skipWhitespaceAndComments()

        val openResult = completer.completeChar(listOf('['), CompletionItemKind.Keyword)
        if (openResult is BclCompleter.ShouldComplete) {
            return openResult.items
        }
        completer.skipWhitespaceAndComments()

        val blockType = NamespacedIdentifier.parse(blockTypeCompleteResult.input)?. run(VanillaBlockType::get)
        val properties = blockType?.properties ?: emptySet()

        while (true) {
            val propertyNameCompletionResult = completer.completeQuotedString(properties.map { it.name }, CompletionItemKind.Property)
            if (propertyNameCompletionResult is BclCompleter.ShouldComplete) {
                return propertyNameCompletionResult.items
            }
            propertyNameCompletionResult as BclCompleter.ShouldContinue
            completer.skipWhitespaceAndComments()

            val property = BlockProperties.getVanillaProperty(propertyNameCompletionResult.input)

            (completer.completeChar(
                listOf('='),
                CompletionItemKind.Keyword
            ) as? BclCompleter.ShouldComplete)?.run {
                return items
            }
            completer.skipWhitespaceAndComments()

            val propertyValueCompletionResult = if (property is BlockProperty.EnumProperty) {
                completer.completeQuotedString(property.values, CompletionItemKind.Value)
            }
            else {
                completer.completeUnquotedString(property?.strings?.values?.toList() ?: emptyList(), CompletionItemKind.Value)
            }

            if (propertyValueCompletionResult is BclCompleter.ShouldComplete) {
                return propertyValueCompletionResult.items
            }
            completer.skipWhitespaceAndComments()

            val lastResult = completer.completeChar(listOf(',', ']'), CompletionItemKind.Keyword)
            if (lastResult is BclCompleter.ShouldComplete) {
                return lastResult.items
            }
            completer.skipWhitespaceAndComments()
            lastResult as BclCompleter.ShouldContinue

            if (lastResult.input == ']'.toString()) {
                break
            }
        }

        return emptyList()
    }

    companion object {
        fun blockState() = BlockStateArgumentType()
    }
}
