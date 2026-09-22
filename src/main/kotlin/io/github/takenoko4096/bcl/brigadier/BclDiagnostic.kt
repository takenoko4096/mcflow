package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.BclException
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import org.eclipse.lsp4j.Diagnostic

class BclDiagnostic(val message: String, val sourceCode: String, val sourceSpan: SourceSpan) {
    override fun toString(): String {
        return BclException.getPointedMessage(message, sourceCode, sourceSpan)
    }

    fun toLsp4jDiagnostic(): Diagnostic {
        val range = sourceSpan.toLsp4jRange() ?: throw IllegalStateException("NEVER HAPPENS")
        return Diagnostic(range, message)
    }
}
