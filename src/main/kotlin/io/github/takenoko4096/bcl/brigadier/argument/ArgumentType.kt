package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import org.eclipse.lsp4j.CompletionItem

abstract class ArgumentType<T>(val name: String, val hasInlayHint: Boolean = false) {
    abstract fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<T>

    open fun complete(context: BclCompletionContext): List<CompletionItem> {
        return emptyList()
    }
}
