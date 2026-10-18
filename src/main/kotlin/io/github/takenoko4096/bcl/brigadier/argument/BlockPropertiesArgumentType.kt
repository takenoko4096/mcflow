package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.block.BlockProperties
import io.github.takenoko4096.bcl.brigadier.value.block.BlockProperty.Value
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.tokenizer.Symbol
import io.github.takenoko4096.bcl.tokenizer.TokenType
import kotlin.text.endsWith
import kotlin.text.lastIndex
import kotlin.text.slice
import kotlin.text.startsWith
import kotlin.toString

class BlockPropertiesArgumentType : ArgumentType<BlockProperties>("block_properties") {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<BlockProperties> {
        val start = reader.getCurrentTokenStartPosition()
        val map = reader.readMap(
            prefix = Symbol.ARRAY_START,
            postfix = Symbol.ARRAY_END,
            keyParser = { key(semanticTokens) },
            kvSeparatorParser = {
                val start = getCurrentTokenStartPosition()
                expect(Symbol.ASSIGN)
                val end = getPreviousTokenEndPosition()
                semanticTokens.add(BclSemanticToken(BclSemanticTokenType.SYMBOL, start..end))
            },
            valueParser = { value(diagnostics, semanticTokens) }
        )
        val end = reader.getPreviousTokenEndPosition()

        val properties = BlockProperties()

        for ((key, data) in map) {
            val property = BlockProperties.getProperty(key.value)
                ?: run {
                    diagnostics.add(reader.diagnostic(key.sourceSpan, "プロパティの読み取りに失敗しました: 不明なプロパティです: '${key.value}'"))
                    continue
                }

            val value = property.toValue(data.value)

            if (value == null) {
                diagnostics.add(reader.diagnostic(data.sourceSpan, "プロパティの読み取りに失敗しました: '${data.value}' は無効な値です; ${property.strings.values.joinToString(" | ")} を渡す必要があります"))
                continue
            }

            if (property in properties) {
                diagnostics.add(reader.diagnostic(key.sourceSpan, "プロパティの読み取りに失敗しました: プロパティ '${key.value}' が重複しています"))
            }

            properties[property] = value
        }

        return Argument(this, properties, start..end)
    }

    private fun TokenSeqReader.key(semanticTokens: MutableList<BclSemanticToken>): String {
        val start = getCurrentTokenStartPosition()
        val key = readQuotedString()
        val end = getPreviousTokenEndPosition()
        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.PARAMETER, start..end))

        return key
    }

    private fun TokenSeqReader.value(diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): Any {
        val start = getCurrentTokenStartPosition()
        val type = peek().type
        val string = readQuotableCommandString(allowFloat = true)
        val end = getPreviousTokenEndPosition()

        return when (type) {
            TokenType.INT -> {
                semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start..end))
                string.toInt()
            }
            TokenType.WORD -> {
                if (string in setOf("true", "false")) {
                    semanticTokens.add(BclSemanticToken(BclSemanticTokenType.BOOLEAN, start..end))
                    string.toBoolean()
                }
                else {
                    diagnostics.add(diagnostic(start..end, "ブロックプロパティの読み取りに失敗しました: 無効な値です: '$string'"))
                }
            }
            TokenType.DOUBLE_QUOTED -> {
                semanticTokens.add(BclSemanticToken(BclSemanticTokenType.STRING, start..end))
                string
            }
            else -> {
                diagnostics.add(diagnostic(start..end, "ブロックプロパティの読み取りに失敗しました: 無効な値です: '$string'"))
            }
        }
    }
}
