package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.value.entity.EntitySelector
import io.github.takenoko4096.bcl.brigadier.value.entity.SelectorArguments
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.Symbol

class EntitySelectorArgumentType : ArgumentType<EntitySelector>("entity_selector") {
    override fun parse(reader: TokenSeqReader): Argument<EntitySelector> {


        val argsStart = reader.getCurrentTokenStartPosition()
        val arguments = SelectorArguments {}

        while (true) {
            val argStart = reader.getCurrentTokenStartPosition()

            val key = reader.readWord()

            arguments.set()

            if (reader.isOver()) {
                throw reader.exception(argStart..reader.getPreviousTokenEndPosition(), "セレクタ引数リストの読み取りに失敗しました: セレクタ引数リストが '${Symbol.ARRAY_END.value}' で閉じられていません")
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
                throw reader.exception(argStart..reader.getPreviousTokenEndPosition(), "セレクタ引数の読み取りに失敗しました: 不明なトークンです: ${reader.peek()}")
            }
        }

        val argsEnd = reader.getPreviousTokenEndPosition()

        if (arguments.type.filter { !it.inverted }.size > 1) {
            throw reader.exception(argsStart..argsEnd, "セレクタ引数 type は否定でない限り重複できません")
        }
    }
}
