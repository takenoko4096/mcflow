package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier
import io.github.takenoko4096.bcl.reader.TokenSeqReader

class IdentifierArgumentType(name: String, private val defaultNamespace: String? = NamespacedIdentifier.DEFAULT_NAMESPACE, private val quotable: Boolean) : ArgumentType<NamespacedIdentifier>(name) {
    override fun parse(reader: TokenSeqReader): Argument<NamespacedIdentifier> {
        val start = reader.getCurrentTokenStartPosition()
        val identifier = reader.readNamespacedIdentifier(quotable, defaultNamespace)
        val end = reader.getPreviousTokenEndPosition()
        return Argument(this, identifier, start..end)
    }

    companion object {
        fun namespaceRequiredQuotable(name: String) = IdentifierArgumentType(name, null, quotable = true)
    }
}
