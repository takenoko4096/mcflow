package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.entity.EntityType
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType

class EntityTypeArgumentType : ArgumentType<EntityType>("entity_type") {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<EntityType> {
        val start = reader.getCurrentTokenStartPosition()
        val identifier = reader.unfatalize(diagnostics) { readNamespacedIdentifier() }
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.IDENTIFIER, start..end))

        if (identifier is TokenSeqReader.ReadResult.Failure) {
            return NoValuedArgument(this, start..end, diagnostics)
        }

        identifier as TokenSeqReader.ReadResult.Successful

        for (entry in EntityType.entries) {
            if (identifier.value == entry.identifier) {
                return Argument(this, entry, start..end)
            }
        }

        diagnostics.add(reader.diagnostic(start..end, "不明なエンティティタイプです: $identifier"))
        return NoValuedArgument(this, start..end, diagnostics)
    }
}
