package io.github.takenoko4096.bcl

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

/**
 * [SourceSpan] を付けられないオブジェクトに使うもの
 */
data class WithSpan<T>(val value: T, val sourceSpan: SourceSpan)
