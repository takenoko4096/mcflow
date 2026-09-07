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
                ?: throw reader.exception(currentPropertyStart..reader.getPreviousTokenEndPosition(), "プロパティの読み取りに失敗しました: 不明なプロパティです: '$name'")

            reader.expect(Symbol.ASSIGN)

            val t = reader.peek()
            reader.next()
            val end = reader.getPreviousTokenEndPosition()

            val value: BlockProperty.Value<*> = try {
                property.parse(t)
            }
            catch (error: IllegalArgumentException) {
                throw reader.exception(currentPropertyStart..end, "プロパティの読み取りに失敗しました: ${error.message!!}; ${property.values.joinToString(" | ")} を渡す必要があります")
            }

            if (property in properties) {
                throw reader.exception(currentPropertyStart..end, "プロパティの読み取りに失敗しました: プロパティ '$name' が重複しています")
            }

            properties[property] = value

            if (reader.isOver()) {
                throw reader.exception(currentPropertyStart..end, "プロパティリストの読み取りに失敗しました: プロパティリストが '${Symbol.ARRAY_END.value}' で閉じられていません")
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
                throw reader.exception(currentPropertyStart..end, "プロパティの読み取りに失敗しました: 不明なトークンです: ${reader.peek()}")
            }
        }

        val end = reader.getPreviousTokenEndPosition()
        return Argument(this, properties, start..end)
    }
}
