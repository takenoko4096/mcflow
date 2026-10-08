package io.github.takenoko4096.bcl.completer

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

data class BclCompletionContext(val string: String, val sourceSpan: SourceSpan?) {

}