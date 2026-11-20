package io.github.takenoko4096.bcl.server

import io.github.takenoko4096.bcl.ast.BclComment
import io.github.takenoko4096.bcl.ast.Block
import io.github.takenoko4096.bcl.brigadier.CommandSender
import io.github.takenoko4096.bcl.tokenizer.Token

sealed interface CommandDocumentParseResult {
    val sender: CommandSender

    val sourceCode: String

    open class Read(
        override val sender: CommandSender,
        override val sourceCode: String
    ) : CommandDocumentParseResult

    open class Tokenized(
        sender: CommandSender,
        sourceCode: String,
        val tokens: List<Token>,
        val comments: List<BclComment>
    ) : Read(sender, sourceCode), CommandDocumentParseResult

    class Parsed(
        sender: CommandSender,
        sourceCode: String,
        tokens: List<Token>,
        comments: List<BclComment>,
        val block: Block
    ) : Tokenized(sender, sourceCode, tokens, comments), CommandDocumentParseResult
}
