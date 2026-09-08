package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.value.entity.EntitySelector
import io.github.takenoko4096.bcl.brigadier.value.entity.EntityType
import io.github.takenoko4096.bcl.brigadier.value.entity.InvertibleValue
import io.github.takenoko4096.bcl.brigadier.value.entity.SelectorArguments
import io.github.takenoko4096.bcl.brigadier.value.entity.SelectorType
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.Symbol
import io.github.takenoko4096.bcl.tokenizer.TokenType

class EntitySelectorArgumentType : ArgumentType<EntitySelector>("entity_selector") {
    private val parsers: Map<String, TokenSeqReader.(SelectorArguments) -> Unit> = mapOf(
        "type" to ::type
    )

    private fun type(reader: TokenSeqReader, arguments: SelectorArguments) {
        val start = reader.getCurrentTokenStartPosition()

        val inverted = if (reader.peek() `is` Symbol.LOGICAL_NOT) {
            reader.next()
            true
        }
        else false

        if (!inverted && arguments.type.any { !it.inverted }) {
            val end = reader.getPreviousTokenEndPosition()
            throw reader.exception(start..end, "セレクタ引数 type は否定でない限り重複できません")
        }

        val identifier = reader.readIdentifier()
        val entityType = EntityType.entries.find { it.identifier == identifier }
            ?: run {
                val end = reader.getPreviousTokenEndPosition()
                throw reader.exception(start..end, "セレクタ引数 'type' の読み取りに失敗しました: 不明なエンティティタイプです: $identifier")
            }

        arguments.type.add(InvertibleValue(inverted, entityType))
    }

    override fun parse(reader: TokenSeqReader): Argument<EntitySelector> {
        val start = reader.getCurrentTokenStartPosition()
        val arguments = SelectorArguments()

        val t = reader.peek()
        val type = when (t.type) {
            TokenType.DOUBLE_QUOTED -> {
                reader.next()
                arguments.name.add(InvertibleValue.uninverted(t.value))
                val end = reader.getPreviousTokenEndPosition()
                return Argument(this, EntitySelector(SelectorType.PLAYER, arguments), start..end)
            }
            TokenType.WORD -> let {
                for (type in SelectorType.entries) {
                    if (type.getString() == t.value) {
                        reader.next()
                        return@let type
                    }
                }

                reader.next()
                return@let SelectorType.PLAYER
            }
            else -> {
                reader.next()
                val end = reader.getPreviousTokenEndPosition()
                throw reader.exception(start..end, "セレクターの読み取りに失敗しました: 不明なセレクタータイプです ('${t.value}')")
            }
        }

        while (true) {
            val start2 = reader.getCurrentTokenStartPosition()

            val key = reader.readWord()
            if (key !in parsers) {
                val end2 = reader.getPreviousTokenEndPosition()
                throw reader.exception(start2..end2, "セレクタ引数の読み取りに失敗しました: セレクタ引数 '$key' は存在しません")
            }

            val parser = parsers[key]!!

            parser(reader, arguments)

            if (reader.isOver()) {
                val end2 = reader.getPreviousTokenEndPosition()
                throw reader.exception(start2..end2, "セレクタ引数リストの読み取りに失敗しました: セレクタ引数リストが '${Symbol.ARRAY_END.value}' で閉じられていません")
            }
            else if (reader.peek() `is` Symbol.ARRAY_END) {
                reader.next()
                break
            }
            else if (reader.peek() `is` Symbol.COMMA) {
                reader.next()
            }
            else {
                reader.next()
                val end2 = reader.getPreviousTokenEndPosition()
                throw reader.exception(start2..end2, "セレクタ引数の読み取りに失敗しました: 不明なトークンです: ${reader.peek()}")
            }
        }

        val end = reader.getPreviousTokenEndPosition()
        return Argument(this, EntitySelector(type, arguments), start..end)
    }
}
