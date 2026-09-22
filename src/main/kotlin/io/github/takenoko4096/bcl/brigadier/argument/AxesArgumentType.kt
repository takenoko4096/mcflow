package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.value.location.Axis
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType

class AxesArgumentType : ArgumentType<Set<Axis>>("axes") {
    override fun parse(reader: TokenSeqReader): Argument<Set<Axis>> {
        val start = reader.getCurrentTokenStartPosition()
        val string = reader.readCommandString()
        val end = reader.getPreviousTokenEndPosition()
        val sourceSpan = start..end

        val set = string.toCharArray()
            .map { char ->
                Axis.entries.find { it.getString() == char.toString() }
                    ?: throw reader.exception(sourceSpan, "軸の読み取りに失敗しました: 軸名 '$char' は無効です")
            }

        val axes = mutableSetOf<Axis>()
        for (element in set) {
            if (element in axes) {
                throw reader.exception(sourceSpan, "軸の読み取りに失敗しました: 軸 '$element' が重複しています")
            }

            axes.add(element)
        }

        return Argument(this, axes, sourceSpan)
    }
}
