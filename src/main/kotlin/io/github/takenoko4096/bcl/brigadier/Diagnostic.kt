package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.BclException
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class Diagnostic(val message: String, val text: String, val sourceSpan: SourceSpan) {
    override fun toString(): String {
        return BclException.getPointedMessage(message, text, sourceSpan)
    }
}
