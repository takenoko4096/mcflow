package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier
import io.github.takenoko4096.bcl.reader.TokenSeqReader

class IdentifierArgumentType(name: String, private val defaultNamespace: String? = NamespacedIdentifier.DEFAULT_NAMESPACE, private val quotable: Boolean = true, private val nonDefaultNamespace: Boolean = false, private val strict: Boolean = true) : ArgumentType<NamespacedIdentifier>(name) {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): Argument<NamespacedIdentifier> {
        val start = reader.getCurrentTokenStartPosition()
        val identifier = reader.readNamespacedIdentifier(quotable, defaultNamespace, strict)
        val end = reader.getPreviousTokenEndPosition()

        if (nonDefaultNamespace && identifier.namespace == defaultNamespace) {
            throw reader.exception(start..end, "識別子引数の読み取りに失敗しました: 名前空間は '$defaultNamespace' 以外である必要があります")
        }

        return Argument(this, identifier, start..end)
    }

    companion object {
        fun requireNamespace(name: String) = IdentifierArgumentType(name, null)

        fun unstrict(name: String, fallbackNamespace: String) = IdentifierArgumentType(name, fallbackNamespace, strict = false)

        fun nonDefaultNamespace(name: String) = IdentifierArgumentType(name, NamespacedIdentifier.DEFAULT_NAMESPACE, true)
    }
}
