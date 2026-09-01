package io.github.takenoko4096.bcl.tokenizer

data class WhitespaceSkipResult(
    val foundWhitespace: Boolean,
    val linebreak: SourcePosition?
)
