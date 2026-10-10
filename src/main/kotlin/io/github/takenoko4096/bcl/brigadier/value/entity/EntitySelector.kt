package io.github.takenoko4096.bcl.brigadier.value.entity

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.argument.EntitySelectorArgumentType
import io.github.takenoko4096.bcl.parser.BclParseException
import io.github.takenoko4096.bcl.reader.BclTokenSeqReadException
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.tokenizer.BclTokenizer
import io.github.takenoko4096.bcl.tokenizer.KeyChar

class EntitySelector(val type: SelectorType, val arguments: SelectorArguments) {
    private val c = if (type.argumentsOverridable) arguments.c ?: type.defaultArguments.c else type.defaultArguments.c

    val isPlayer = type.isPlayer || arguments.type.any { !it.inverted && it.value == EntityType.PLAYER }

    val isSingle = c?.value == 1 || c?.value == -1

    val isInvertedOrder = c?.value != null && c.value < 0

    val isWildcard = type == SelectorType.PLAYER && arguments.name.size == 1 && !arguments.name.first().inverted && arguments.name.first().value == KeyChar.WILDCARD.toString()

    override fun toString(): String {
        return "${type.getString()}${arguments.let { if (it.isEmpty()) "" else it }}"
    }

    companion object {
        fun parse(string: String, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<EntitySelector> {
            val tokenizer = BclTokenizer(string, extension = false)
            val tokens = tokenizer.tokenize()

            val reader = TokenSeqReader(tokenizer.sourceCode, tokens, ::BclParseException)
            val type = EntitySelectorArgumentType()

            val start = reader.getCurrentTokenStartPosition()

            val selector = try {
                type.parse(reader, diagnostics, semanticTokens)
            }
            catch (error: BclTokenSeqReadException) {
                diagnostics.add(reader.diagnostic(error.sourceSpan, error.description))
                val span = reader.getRemainingSourceSpan()
                semanticTokens.add(BclSemanticToken(BclSemanticTokenType.ERROR, reader.peekSafely(-1).sourceSpan))
                semanticTokens.add(BclSemanticToken(BclSemanticTokenType.ERROR, span))
                null
            }

            val end = reader.getPreviousTokenEndPosition()

            if (!reader.isOver() && selector != null) {
                // 解析成功、 but 余計なのがあったとき
                diagnostics.add(reader.diagnostic(reader.getRemainingSourceSpan(), "セレクターの終了後に無効な文字列があります"))
                semanticTokens.add(BclSemanticToken(BclSemanticTokenType.ERROR, reader.getRemainingSourceSpan()))
            }

            return selector ?: NoValuedArgument(type, start..end, diagnostics)
        }
    }
}
