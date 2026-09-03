package io.github.takenoko4096.bcl.reader

import io.github.takenoko4096.bcl.BclException
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

abstract class BclTokenSeqReadException(
    message: String,
    text: String,
    span: SourceSpan,
    cause: Throwable?
) : BclException(message, text, span, cause)
