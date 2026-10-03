package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType

class StringArgumentType(private val mode: Mode) : ArgumentType<String>("string", hasInlayHint = true) {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<String> {
        val start = reader.getCurrentTokenStartPosition()

        val string = when (mode) {
            Mode.QUOTED -> reader.readQuotedString()
            Mode.QUOTABLE -> {
                val result = reader.unfatalize(diagnostics) { readQuotableCommandString() }
                if (result !is TokenSeqReader.ReadResult.Successful) {
                    semanticTokens.add(BclSemanticToken(BclSemanticTokenType.STRING, result.sourceSpan))
                    return NoValuedArgument(this, result.sourceSpan, diagnostics)
                }
                result.value
            }
            Mode.WORD -> {
                val result = reader.unfatalize(diagnostics) { readCommandString() }
                if (result !is TokenSeqReader.ReadResult.Successful) {
                    semanticTokens.add(BclSemanticToken(BclSemanticTokenType.STRING, result.sourceSpan))
                    return NoValuedArgument(this, result.sourceSpan, diagnostics)
                }
                result.value
            }
            Mode.GREEDY -> reader.readAllRemainingAsString()
        }

        val end = reader.getPreviousTokenEndPosition()

        // TODO isMultiLines() -> splitByLine().forEach {}
        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.STRING, start..end))

        return Argument(this, string, start..end)
    }

    enum class Mode {
        QUOTED,
        QUOTABLE,
        WORD,
        GREEDY
    }

    companion object {
        fun isolated(): StringArgumentType {
            return StringArgumentType(Mode.QUOTABLE)
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
