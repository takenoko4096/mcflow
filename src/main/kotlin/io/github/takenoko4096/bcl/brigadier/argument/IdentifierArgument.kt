package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier
import io.github.takenoko4096.bcl.reader.TokenSeqReader

class IdentifierArgument(private val allowedValues: Set<NamespacedIdentifier>) : ArgumentType<NamespacedIdentifier>("identifier") {
    override fun parse(reader: TokenSeqReader): Argument<NamespacedIdentifier> {
        val start = reader.getCurrentTokenStartPosition()
        val identifier = reader.readNamespacedIdentifier()
        val end = reader.getPreviousTokenEndPosition()

        if (identifier !in allowedValues) {
            throw reader.exception(start..end, "不明な識別子です: $identifier")
        }

        return Argument(this, identifier, start..end)
    }

    companion object {
        fun anyOf(values: Set<NamespacedIdentifier>): IdentifierArgument {
            return IdentifierArgument(values)
        }
    }
}
