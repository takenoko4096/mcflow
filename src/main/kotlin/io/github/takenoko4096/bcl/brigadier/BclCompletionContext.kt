package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

data class BclCompletionContext(val string: String, val sourceSpan: SourceSpan?) {
    // fun toJsonContext(): JsonCompletionContext etc
}
