package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonArray
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonObject
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonValue
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType

abstract class JsonObjectArgumentType<T>(name: String) : ArgumentType<T>(name) {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): Argument<T> {
        val obj = reader.readJsonObject()
        colorObject(obj, semanticTokens)
        val value = interpret(reader, diagnostics, semanticTokens, obj)
        return Argument(this, value, obj.sourceSpan)
    }

    private fun colorValue(value: CommandJsonValue<*>, semanticTokens: MutableList<BclSemanticToken>) {
        when (value) {
            is CommandJsonObject -> colorObject(value, semanticTokens)
            is CommandJsonArray -> colorArray(value, semanticTokens)
            else -> when (value.value) {
                is Boolean -> semanticTokens.add(BclSemanticToken(BclSemanticTokenType.BOOLEAN, value.sourceSpan))
                is Number -> semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, value.sourceSpan))
                is String -> semanticTokens.add(BclSemanticToken(BclSemanticTokenType.STRING, value.sourceSpan))
            }
        }
    }

    private fun colorArray(arr: CommandJsonArray, semanticTokens: MutableList<BclSemanticToken>) {
        arr.list.forEach { colorValue(it, semanticTokens) }
    }

    private fun colorObject(obj: CommandJsonObject, semanticTokens: MutableList<BclSemanticToken>) {
        obj.entries.forEach { (key, value) ->
            semanticTokens.add(BclSemanticToken(BclSemanticTokenType.PARAMETER, key.sourceSpan))
            colorValue(value, semanticTokens)
        }
    }

    protected abstract fun interpret(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, obj: CommandJsonObject): T
}
