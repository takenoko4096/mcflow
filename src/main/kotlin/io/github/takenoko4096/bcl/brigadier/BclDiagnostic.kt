package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.BclException
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class BclDiagnostic(val message: String, val sourceCode: String, val sourceSpan: SourceSpan) {
    override fun toString(): String {
        return BclException.getPointedMessage(message, sourceCode, sourceSpan)
    }
}
