package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import org.eclipse.lsp4j.CompletionItem

abstract class ArgumentType<T>(val name: String, val hasInlayHint: Boolean = false) {
    abstract fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<T>

    // カーソルはかならず最後にある。
    open fun complete(sourceSpan: SourceSpan?, string: String, reader: TokenSeqReader): List<CompletionItem> {
        return emptyList()
    }
}
