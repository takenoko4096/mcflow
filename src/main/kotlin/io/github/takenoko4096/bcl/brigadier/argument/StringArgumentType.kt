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
        return when (mode) {
            Mode.QUOTED -> {
                val start = reader.getCurrentTokenStartPosition()
                val string = reader.readQuotedString()
                val end = reader.getPreviousTokenEndPosition()
                semanticTokens.add(BclSemanticToken(BclSemanticTokenType.STRING, start..end))
                Argument(this, string, start..end)
            }
            Mode.QUOTABLE -> {
                val result = reader.unfatalize(diagnostics) { readQuotableCommandString() }
                semanticTokens.add(BclSemanticToken(BclSemanticTokenType.STRING, result.sourceSpan))
                if (result is TokenSeqReader.ReadResult.Successful) {
                    Argument(this, result.value, result.sourceSpan)
                }
                else {
                    NoValuedArgument(this, result.sourceSpan, diagnostics)
                }
            }
            Mode.WORD -> {
                val result = reader.unfatalize(diagnostics) { readCommandString() }
                semanticTokens.add(BclSemanticToken(BclSemanticTokenType.STRING, result.sourceSpan))
                if (result is TokenSeqReader.ReadResult.Successful) {
                    Argument(this, result.value, result.sourceSpan)
                }
                else {
                    NoValuedArgument(this, result.sourceSpan, diagnostics)
                }
            }
            Mode.GREEDY -> {
                val sourceSpans = reader.getSourceSpansOfRemainingLines()
                val start = reader.getCurrentTokenStartPosition()
                val string = reader.readAllRemainingAsString()
                val end = reader.getPreviousTokenEndPosition()
                sourceSpans.forEach {
                    semanticTokens.add(BclSemanticToken(BclSemanticTokenType.STRING,  it))
                }
                Argument(this, string, start..end)
            }
        }
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
