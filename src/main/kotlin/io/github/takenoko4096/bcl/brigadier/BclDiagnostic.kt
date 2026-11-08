package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.BclException
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import org.eclipse.lsp4j.Diagnostic

class BclDiagnostic(
    val message: String,
    val sourceCode: String,
    val sourceSpan: SourceSpan,
    val severity: BclSeverity = BclSeverity.ERROR
) {
    val isError = severity == BclSeverity.ERROR

    override fun toString(): String {
        return BclException.getPointedMessage(message, sourceCode, sourceSpan)
    }

    fun toLsp4jDiagnostic(): Diagnostic {
        val range = sourceSpan.toLsp4jRange()
        val diagnostic = Diagnostic(range, message, severity.toLsp4jSeverity(), "mcflow")
        return diagnostic
    }
}
