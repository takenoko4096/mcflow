package io.github.takenoko4096.bcl.parser

import io.github.takenoko4096.bcl.BclException
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class BclParseException internal constructor(
    message: String,
    text: String,
    span: SourceSpan,
    cause: Throwable?
) : BclException(getPointedMessage(message, text, span), cause)
