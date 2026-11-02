package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable
import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable
import org.eclipse.lsp4j.DiagnosticSeverity

enum class BclSeverity : StringRepresentable, CommandEnumerable {
    WARNING {
        override fun toLsp4jSeverity() = DiagnosticSeverity.Warning
    },
    ERROR {
        override fun toLsp4jSeverity() = DiagnosticSeverity.Error
    };

    abstract fun toLsp4jSeverity(): DiagnosticSeverity

    override fun getString(): String {
        return name.lowercase()
    }

    override fun getNames(): Set<String> {
        return setOf(getString())
    }
}
