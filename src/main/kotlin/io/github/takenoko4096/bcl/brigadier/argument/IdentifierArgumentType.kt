package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType

open class IdentifierArgumentType(
    name: String,
    private val defaultNamespace: String?,
    private val quotable: Boolean = true,
    private val strict: Boolean = true
) : ArgumentType<NamespacedIdentifier>(name) {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<NamespacedIdentifier> {
        val identifier = reader.unfatalize(diagnostics) {
            readNamespacedIdentifier(defaultNamespace, quotable, strict)
        }

        if (identifier.nonnull) {
            semanticTokens.add(BclSemanticToken(BclSemanticTokenType.IDENTIFIER, identifier.sourceSpan))
        }

        return if (identifier is TokenSeqReader.ReadResult.Successful) {
            Argument(this, identifier.value, identifier.sourceSpan)
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
