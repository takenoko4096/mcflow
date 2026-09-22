package io.github.takenoko4096.bcl.brigadier.value.entity

import io.github.takenoko4096.bcl.brigadier.argument.EntitySelectorArgumentType
import io.github.takenoko4096.bcl.parser.BclParseException
import io.github.takenoko4096.bcl.reader.TokenSeqReader
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
        fun parse(string: String): EntitySelector {
            val tokens = BclTokenizer(string, interpretComments = false).tokenize()
            val reader = TokenSeqReader(string, tokens, ::BclParseException)
            val selector = EntitySelectorArgumentType().parse(reader)
            if (!(reader.isOver())) {
                val span = reader.getRemainingSourceSpan()
                throw reader.exception(span, "セレクターの読み取りに失敗しました: 不明なトークン列です: ${span.getStringFromSourceCode(string)}")
            }
            return selector.value
        }
    }
}
