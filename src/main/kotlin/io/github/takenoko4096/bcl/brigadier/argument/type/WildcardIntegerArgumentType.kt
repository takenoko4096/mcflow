package io.github.takenoko4096.bcl.brigadier.argument.type

import io.github.takenoko4096.bcl.brigadier.argument.Argument
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseContext
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.tokenizer.Symbol

class WildcardIntegerArgumentType : ArgumentType<Int?>("wildcard_integer", hasInlayHint = true) {
    override fun parse(context: ArgumentParseContext): Argument<Int?> {
        val reader = context.reader
        // val diagnostics = context.diagnostics
        val semanticTokens = context.semanticTokens

        val start = reader.getCurrentTokenStartPosition()

        val i = if (reader.peek() `is` Symbol.MULTIPLY) {
            reader.next()
            null
        }
        else {
            reader.readInteger(allowExtendedNotation = false)
        }

        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start..end))

        return Argument(this, i, start..end)
    }
}
