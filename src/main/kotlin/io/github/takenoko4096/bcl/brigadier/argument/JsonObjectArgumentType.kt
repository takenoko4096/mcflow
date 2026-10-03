package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonArray
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonObject
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonValue
import io.github.takenoko4096.bcl.reader.BclTokenSeqReadException
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType

abstract class JsonObjectArgumentType<T>(name: String) : ArgumentType<T>(name) {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<T> {
        val dummySemanticTokens = mutableListOf<BclSemanticToken>()
        val obj: CommandJsonObject

        try {
            obj = reader.readJsonObject(dummySemanticTokens)
        }
        catch (error: BclTokenSeqReadException) {
            semanticTokens.addAll(dummySemanticTokens)
            throw error
        }

        val result = try {
            val value = interpret(reader, diagnostics, semanticTokens, obj)
            Argument(this, value, obj.sourceSpan)
        }
        catch (error: BclTokenSeqReadException) {
            diagnostics.add(reader.diagnostic(error.sourceSpan, error.description))
            NoValuedArgument(this, obj.sourceSpan, diagnostics)
        }

        colorValue(obj, semanticTokens)

        return result
    }

    private fun colorValue(value: CommandJsonValue<*>, semanticTokens: MutableList<BclSemanticToken>) {
        when (value) {
            is CommandJsonObject -> colorObject(value, semanticTokens)
            is CommandJsonArray -> colorArray(value, semanticTokens)
            else -> when (value.value) {
                is Boolean -> semanticTokens.add(BclSemanticToken(BclSemanticTokenType.BOOLEAN, value.sourceSpan))
                is Number -> semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, value.sourceSpan))
                is String -> colorString(value as CommandJsonValue<String>, BclSemanticTokenType.STRING, semanticTokens)
            }
        }
    }

    private fun colorArray(arr: CommandJsonArray, semanticTokens: MutableList<BclSemanticToken>) {
        arr.list.forEach { colorValue(it, semanticTokens) }
    }

    private fun colorString(string: CommandJsonValue<String>, type: BclSemanticTokenType, semanticTokens: MutableList<BclSemanticToken>) {
        for (span in string.sourceSpan.exclude(semanticTokens.filter { it.sourceSpan in string.sourceSpan }.map { it.sourceSpan })) {
            semanticTokens.add(BclSemanticToken(type, span))
        }
    }

    private fun colorObject(obj: CommandJsonObject, semanticTokens: MutableList<BclSemanticToken>) {
        obj.entries.forEach { (key, value) ->
            colorString(key, BclSemanticTokenType.PARAMETER, semanticTokens)
            colorValue(value, semanticTokens)
        }
    }

    protected abstract fun interpret(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>, obj: CommandJsonObject): T
}
