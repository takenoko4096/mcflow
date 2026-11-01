package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.BclException
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity

class BclDiagnostic(val message: String, val sourceCode: String, val sourceSpan: SourceSpan, val severity: Severity = Severity.ERROR) {
    val isError = severity == Severity.ERROR

    override fun toString(): String {
        return BclException.getPointedMessage(message, sourceCode, sourceSpan)
    }

    fun toLsp4jDiagnostic(): Diagnostic {
        val range = sourceSpan.toLsp4jRange() ?: throw IllegalStateException("NEVER HAPPENS")
        val diagnostic = Diagnostic(range, message)
        diagnostic.severity = severity.toLsp4jSeverity()
        return diagnostic
    }

    enum class Severity {
        WARNING {
            override fun toLsp4jSeverity() = DiagnosticSeverity.Warning
        },
        ERROR {
            override fun toLsp4jSeverity() = DiagnosticSeverity.Error
        };

        abstract fun toLsp4jSeverity(): DiagnosticSeverity
    }
}
