package io.github.takenoko4096.bcl.parser

import io.github.takenoko4096.bcl.reader.BclTokenSeqReadException
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class BclParseException internal constructor(
    message: String,
    text: String,
    span: SourceSpan,
    cause: Throwable?,
    unfatalizable: Boolean
) : BclTokenSeqReadException(message, text, span, cause, unfatalizable)
