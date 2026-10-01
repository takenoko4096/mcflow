package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.block.BlockType
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType

class BlockTypeArgumentType : ArgumentType<BlockType>("block_type") {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<BlockType> {
        val start = reader.getCurrentTokenStartPosition()
        val identifier = reader.unfatalize(diagnostics) { readNamespacedIdentifier() }
        val end = reader.getPreviousTokenEndPosition()

        if (identifier !is TokenSeqReader.ReadResult.Successful) {
            return NoValuedArgument(this, start..end, diagnostics)
        }

        for (entry in BlockType.entries) {
            if (identifier.value == entry.identifier) {
                return Argument(this, entry, start..end)
            }
        }

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.IDENTIFIER, start..end))

        diagnostics.add(reader.diagnostic(start..end, "不明なブロックタイプです: $identifier"))
        return NoValuedArgument(this, start..end, diagnostics)
    }
}
