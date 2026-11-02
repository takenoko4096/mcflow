package io.github.takenoko4096.bcl.brigadier.argument.type

import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseResult
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseContext
import org.eclipse.lsp4j.CompletionItem

abstract class ArgumentType<T>(val name: String, val hasInlayHint: Boolean = false) {
    abstract fun parse(context: ArgumentParseContext): ArgumentParseResult<T>

    open fun complete(context: BclCompletionContext): List<CompletionItem> {
        return emptyList()
    }
}
