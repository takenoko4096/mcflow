package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.BclException
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class Diagnostic(val message: String, val sourceCode: String, val sourceSpan: SourceSpan) {
    override fun toString(): String {
        try {
            return BclException.getPointedMessage(message, sourceCode, sourceSpan)
        } catch (e: Exception) {
            println("aaa:::::"+sourceCode)
            throw e
        }
    }
}
