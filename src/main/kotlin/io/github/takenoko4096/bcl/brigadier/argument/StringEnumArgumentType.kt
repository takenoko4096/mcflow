package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.reader.TokenSeqReader

class StringEnumArgumentType(name: String, private val entries: Array<String>) : ArgumentType<String>(name) {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): Argument<String> {
        val start = reader.getCurrentTokenStartPosition()
        val string = reader.readQuotableCommandStringAsFatal()
        val end = reader.getPreviousTokenEndPosition()

        for (entry in entries) {
            if (entry == string) {
                return Argument(this, entry, start..end)
            }
        }

        throw reader.exception(start..end, "不明なエントリです: '$string'")
    }

    companion object {
        fun of(name: String, entries: Array<String>): StringEnumArgumentType {
            return StringEnumArgumentType(name, entries)
        }
    }
}
