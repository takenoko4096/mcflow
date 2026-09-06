package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.value.BlockProperties
import io.github.takenoko4096.bcl.brigadier.value.BlockProperty
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.`!is`
import io.github.takenoko4096.bcl.tokenizer.Symbol
import io.github.takenoko4096.bcl.tokenizer.TokenType

class BlockPropertiesArgumentType : ArgumentType<BlockProperties>("block_properties") {
    override fun parse(reader: TokenSeqReader): Argument<BlockProperties> {
        val start = reader.getCurrentTokenStartPosition()

        reader.expect(Symbol.ARRAY_START)

        val properties = BlockProperties()

        while (reader.peek() `!is` Symbol.ARRAY_END) {
            val name = reader.readQuotedString()

            val property = BlockProperties.getProperty(name)
                ?: throw reader.exception(start..reader.getPreviousTokenEndPosition(), "プロパティセットの読み取りに失敗しました: プロパティ名 '$name' は無効です")

            reader.expect(Symbol.ASSIGN)

            val t = reader.peek()
            val value: BlockProperty.Value<*> = when (t.type) {
                TokenType.INT, TokenType.DOUBLE_QUOTED, TokenType.WORD-> {
                    val value = property.parse(t.value)
                    reader.next()

                    if (value == null) {
                        val end = reader.getPreviousTokenEndPosition()
                        throw reader.exception(start..end, "プロパティセットの読み取りに失敗しました: プロパティ '$name' に対して無効な値です: '${t.sourceSpan.getStringFromSourceCode(reader.sourceCode)}'; 期待される値は ${property.values.joinToString(" | ")} です")
                    }

                    value
                }
                else -> {
                    reader.next()
                    val end = reader.getPreviousTokenEndPosition()
                    throw reader.exception(start..end, "プロパティセットの読み取りに失敗しました: プロパティ '$name' に対して無効なトークンです: '${t.sourceSpan.getStringFromSourceCode(reader.sourceCode)}'; 期待される値は ${property.values.joinToString(" | ")} です")
                }
            }

            if (property in properties) {
                val end = reader.getPreviousTokenEndPosition()
                throw reader.exception(start..end, "プロパティセットの読み取りに失敗しました: プロパティ '$name' が重複しています")
            }

            properties[property] = value
        }

        reader.expect(Symbol.ARRAY_END)

        val end = reader.getPreviousTokenEndPosition()
        return Argument(this, properties, start..end)
    }
}
