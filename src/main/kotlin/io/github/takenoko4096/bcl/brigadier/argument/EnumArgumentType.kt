package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import kotlin.enums.EnumEntries
import kotlin.enums.enumEntries

class EnumArgumentType<E : Enum<E>>(private val entries: EnumEntries<E>) : ArgumentType<E>("enum") {
    override fun parse(reader: TokenSeqReader): Argument<E> {
        val start = reader.getCurrentTokenStartPosition()
        val word = reader.readWord()
        val end = reader.getPreviousTokenEndPosition()

        for (entry in entries) {
            if (entry.name.equals(word, ignoreCase = true)) {
                return Argument(this, entry, start..end)
            }
        }

        throw reader.exception(start..end, "不明なエントリです: '$word'")
    }

    companion object {
        inline fun <reified E : Enum<E>> of(): EnumArgumentType<E> {
            return EnumArgumentType(enumEntries<E>())
        }
    }
}
