package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import kotlin.enums.EnumEntries
import kotlin.enums.enumEntries

class EnumArgumentType<E>(name: String, private val entries: EnumEntries<E>) : ArgumentType<E>(name) where E : Enum<E>, E : CommandEnumerable {
    override fun parse(reader: TokenSeqReader): ArgumentParseResult<E> {
        val diagnostics = mutableListOf<BclDiagnostic>()
        val start = reader.getCurrentTokenStartPosition()
        val string = reader.readCommandString(allowFloat = true, diagnostics = diagnostics)
        val end = reader.getPreviousTokenEndPosition()

        for (entry in entries) {
            if (entry.getNames().contains(string)) {
                return if (diagnostics.isEmpty()) {
                    Argument(this, entry, start..end)
                }
                else {
                    ArgumentParseResult.Failure(this, start..end, diagnostics)
                }
            }
        }

        diagnostics.add(reader.diagnostic(start..end, "不明なエントリです: '$string'"))
        return ArgumentParseResult.Failure(this, start..end, diagnostics)
    }

    companion object {
        inline fun <reified E> of(): EnumArgumentType<E> where E : Enum<E>, E : CommandEnumerable {
            val className = E::class.simpleName ?: throw IllegalArgumentException("無名クラスを EnumArgumentType に使用することはできません")
            val name = className.replace("([a-z])([A-Z]+)".toRegex(), "$1_$2").lowercase()
            val entries = enumEntries<E>()
            return EnumArgumentType(name, entries)
        }
    }
}
