package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.reader.TokenSeqReader

class StringArgumentType(private val mode: Mode) : ArgumentType<String>("string", hasInlayHint = true) {
    override fun parse(reader: TokenSeqReader): Argument<String> {
        val start = reader.getCurrentTokenStartPosition()

        val string = when (mode) {
            Mode.QUOTED -> reader.readQuotedString()
            // TODO
            Mode.UNQUOTABLE -> reader.readQuotableCommandString()
            // TODO
            Mode.WORD -> reader.readCommandString()
            Mode.GREEDY -> reader.readAllRemainingAsString()
        }

        return Argument(this, string, start..reader.getPreviousTokenEndPosition())
    }

    enum class Mode {
        QUOTED,
        UNQUOTABLE,
        WORD,
        GREEDY
    }

    companion object {
        fun isolated(): StringArgumentType {
            return StringArgumentType(Mode.UNQUOTABLE)
        }

        fun word(): StringArgumentType {
            return StringArgumentType(Mode.WORD)
        }

        fun quoted(): StringArgumentType {
            return StringArgumentType(Mode.QUOTED)
        }

        fun greedy(): StringArgumentType {
            return StringArgumentType(Mode.GREEDY)
        }
    }
}
