package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.value.BlockProperties
import io.github.takenoko4096.bcl.brigadier.value.BlockProperty
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.Symbol
import io.github.takenoko4096.bcl.tokenizer.TokenType

class BlockPropertiesArgumentType : ArgumentType<BlockProperties>("block_properties") {
    override fun parse(reader: TokenSeqReader): Argument<BlockProperties> {
        val start = reader.getCurrentTokenStartPosition()

        reader.expect(Symbol.ARRAY_START)

        val properties = BlockProperties()

        if (reader.peek() `is` Symbol.ARRAY_END) {
            reader.next()
            val end = reader.getPreviousTokenEndPosition()
            return Argument(this, properties, start..end)
        }

        while (true) {
            val currentPropertyStart = reader.getCurrentTokenStartPosition()
            val name = reader.readQuotedString()

            val property = BlockProperties.getProperty(name)
                ?: throw reader.exception(currentPropertyStart..reader.getPreviousTokenEndPosition(), "プロパティセットの読み取りに失敗しました: 不明なプロパティです: '$name'")

            reader.expect(Symbol.ASSIGN)

            val t = reader.peek()

            val expectedTokenType = when (property) {
                is BlockProperty.BooleanProperty -> TokenType.WORD
                is BlockProperty.IntegerProperty -> TokenType.INT
                is BlockProperty.EnumProperty -> TokenType.DOUBLE_QUOTED
            }

            if (t.type != expectedTokenType) {
                reader.next()
                val end = reader.getPreviousTokenEndPosition()
                throw reader.exception(currentPropertyStart..end, "プロパティセットの読み取りに失敗しました: プロパティ '$name' に対して無効な型です: $t; $expectedTokenType の値が期待されています")
            }

            reader.next()
            val value: BlockProperty.Value<*> = property.parse(t.value)
                ?: run {
                    val end = reader.getPreviousTokenEndPosition()
                    throw reader.exception(currentPropertyStart..end, "プロパティセットの読み取りに失敗しました: プロパティ '$name' に対して無効なトークンです: '${t.value}'; 期待される値は ${property.values.joinToString(" | ")} です")
                }

            if (property in properties) {
                val end = reader.getPreviousTokenEndPosition()
                throw reader.exception(currentPropertyStart..end, "プロパティセットの読み取りに失敗しました: プロパティ '$name' が重複しています")
            }

            properties[property] = value

            if (reader.isOver()) {
                throw reader.exception(currentPropertyStart..reader.getPreviousTokenEndPosition(), "プロパティセットが '${Symbol.ARRAY_END.value}' で閉じられていません")
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
                val end = reader.getPreviousTokenEndPosition()
                throw reader.exception(currentPropertyStart..end, "プロパティセットの読み取りに失敗しました: 不明なトークンです: ${reader.peek()}")
            }
        }

        val end = reader.getPreviousTokenEndPosition()
        return Argument(this, properties, start..end)
    }
}
