package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType

class IdentifierArgumentType(
    name: String,
    private val defaultNamespace: String?,
    private val quotable: Boolean = true,
    private val strict: Boolean = true
) : ArgumentType<NamespacedIdentifier>(name) {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): Argument<NamespacedIdentifier> {
        val start = reader.getCurrentTokenStartPosition()
        val identifier = reader.readNamespacedIdentifier(defaultNamespace, quotable, strict)
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.IDENTIFIER, start..end))

        return Argument(this, identifier, start..end)
    }

    companion object {
        fun namespaced(name: String) = IdentifierArgumentType(name, null)

        fun unstrict(name: String, fallbackNamespace: String) = IdentifierArgumentType(name, fallbackNamespace, strict = false)
    }
}
