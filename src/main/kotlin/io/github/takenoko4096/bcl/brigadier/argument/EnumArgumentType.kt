package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.value.AliasRepresentable
import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import kotlin.enums.EnumEntries
import kotlin.enums.enumEntries

class EnumArgumentType<E>(name: String, private val entries: EnumEntries<E>) : ArgumentType<E>(name) where E : Enum<E>, E : AliasRepresentable {
    override fun parse(reader: TokenSeqReader): Argument<E> {
        val start = reader.getCurrentTokenStartPosition()
        // TODO
        val word = reader.readCommandString(allowFloat = true)
        val end = reader.getPreviousTokenEndPosition()

        for (entry in entries) {
            if (entry.getAliases().contains(word)) {
                return Argument(this, entry, start..end)
            }
        }

        throw reader.exception(start..end, "不明なエントリです: '$word'")
    }

    companion object {
        inline fun <reified E> of(): EnumArgumentType<E> where E : Enum<E>, E : AliasRepresentable {
            val className = E::class.simpleName ?: throw IllegalArgumentException("無名クラスを EnumArgumentType に使用することはできません")
            val name = className.replace("([a-z])([A-Z]+)".toRegex(), "$1_$2").lowercase()
            val entries = enumEntries<E>()
            return EnumArgumentType(name, entries)
        }
    }
}
