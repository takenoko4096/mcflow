package io.github.takenoko4096.bcl.brigadier.argument.type

import io.github.takenoko4096.bcl.brigadier.argument.Argument
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseContext
import io.github.takenoko4096.bcl.brigadier.argument.NoValuedArgument
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType

open class IdentifierArgumentType(
    name: String,
    private val defaultNamespace: String?,
    private val quotable: Boolean = true,
    private val strict: Boolean = true
) : ArgumentType<NamespacedIdentifier>(name) {
    override fun parse(context: ArgumentParseContext): ArgumentParseResult<NamespacedIdentifier> {
        val reader = context.reader
        val diagnostics = context.diagnostics
        val semanticTokens = context.semanticTokens

        val identifier = reader.unfatalize(diagnostics) {
            readNamespacedIdentifier(defaultNamespace, quotable, strict)
        }

        if (identifier.sourceSpan != null) {
            semanticTokens.add(BclSemanticToken(BclSemanticTokenType.IDENTIFIER, identifier.sourceSpan))
        }

        return if (identifier is TokenSeqReader.ReadResult.Successful) {
            Argument(this, identifier.value, identifier.nonnullSourceSpan)
        }
        else {
            NoValuedArgument(this, identifier.sourceSpan, diagnostics)
        }
    }

    companion object {
        fun namespaced(name: String) = IdentifierArgumentType(name, null)

        fun unstrict(name: String, fallbackNamespace: String) = IdentifierArgumentType(name, fallbackNamespace, strict = false)
    }
}
