package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.BclException
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class BclCommandExecutionException(
    description: String,
    sourceCode: String,
    sourceSpan: SourceSpan,
    cause: Throwable?
) : BclException(description, sourceCode, sourceSpan, cause)
