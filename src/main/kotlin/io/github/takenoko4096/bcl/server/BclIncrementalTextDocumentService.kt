package io.github.takenoko4096.bcl.server

import io.github.takenoko4096.bcl.ast.BclComment
import io.github.takenoko4096.bcl.brigadier.CommandSender
import io.github.takenoko4096.bcl.parser.BclParser
import io.github.takenoko4096.bcl.tokenizer.BclTokenizer
import io.github.takenoko4096.bcl.tokenizer.KeyChar
import io.github.takenoko4096.bcl.tokenizer.SourcePosition
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DidSaveTextDocumentParams
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import org.eclipse.lsp4j.services.TextDocumentService

class BclIncrementalTextDocumentService(private val server: BedrockCommandLanguageServer) : TextDocumentService {
    private val documents = mutableMapOf<String, CommandDocumentParseResult>()

    private fun normalizeSourceCode(text: String): String {
        return text
            .replace(KeyChar.WINDOWS_LINE_SEPARATOR_SEQUENCE, KeyChar.LINEBREAK.toString())
            .replace(KeyChar.MAC_LINE_SEPARATOR, KeyChar.LINEBREAK)
    }

    private fun parseAll(text: String, sender: CommandSender): CommandDocumentParseResult {
        val sourceCode = normalizeSourceCode(text)
        val tokenizer = BclTokenizer(sourceCode, sender, interpretComments = true)

        if (text.isEmpty()) {
            return CommandDocumentParseResult.Read(sender, sourceCode)
        }

        val tokenizationResult = try {
            tokenizer.tokenize()
        }
        catch (_: Throwable) {
            return CommandDocumentParseResult.Read(sender, sourceCode)
        }

        val parser = BclParser(
            sourceCode,
            tokenizationResult.tokens,
            sourceSpan = SourceSpan.toSourceSpan(sourceCode, sourceCode.indices),
            overSourcePosition = SourcePosition.toSourcePosition(sourceCode, sourceCode.length)!!,
            server.dispatcher,
            server.commentCommandDispatcher,
            sender
        )

        val block = try {
            parser.parse()
        }
        catch (_: Throwable) {
            return CommandDocumentParseResult.Tokenized(
                sender,
                sourceCode,
                tokenizationResult.tokens,
                tokenizationResult.comments
            )
        }

        return CommandDocumentParseResult.Parsed(
            sender,
            sourceCode,
            tokenizationResult.tokens,
            tokenizationResult.comments,
            block
        )
    }

    /*
    @file:BclTokenizer.kt @file:BclParser.kt
今言語サーバーのIncremental化を進めているんだけど、その第一歩としてTokenizationを適切にIncremental化して、毎回の同期の度に全文をtokenizeし直す必要がないようにしたい そこで、設計+具体的なコード修正案をお願いしたい
     */

    // TODO
    /*private fun parse(oldDocumentParseResult: CommandDocumentParseResult.Parsed, newEntireSourceCode: String, targetSourceSpan: SourceSpan, sender: CommandSender): CommandDocumentParseResult {
        val targetSourceCode = targetSourceSpan.getStringFromSourceCode(newEntireSourceCode)
        val documentParseResult = parseAll(targetSourceCode, sender)

        if (documentParseResult is CommandDocumentParseResult.Parsed) {
            return CommandDocumentParseResult.Parsed(
                sender,
                newEntireSourceCode,
                getChangesAppliedTokens(oldDocumentParseResult.tokens, documentParseResult.tokens),

            )
        }
    }*/

    private fun updateSourceCode(origin: String, changes: List<TextDocumentContentChangeEvent>): String {
        var sourceCode = origin

        for (change in changes) {
            val range = change.range
            val start = SourcePosition.from(sourceCode, range.start)
            val endExclusive = SourcePosition.from(sourceCode, range.end)
            sourceCode = buildString {
                append(sourceCode.slice(0..<start.index))
                append(normalizeSourceCode(change.text))
                append(sourceCode.slice(endExclusive.index..sourceCode.lastIndex))
            }
        }

        return sourceCode
    }

    override fun didOpen(params: DidOpenTextDocumentParams) {
        val textDocument = params.textDocument
        val sender = CommandSender(extension = textDocument.uri.endsWith(".mcflow"))
        documents[textDocument.uri] = parseAll(textDocument.text, sender)
    }

    // TODO
    override fun didChange(params: DidChangeTextDocumentParams) {
        val textDocument = params.textDocument
        val documentParseResult = documents[textDocument.uri]!!

        val newSourceCode = updateSourceCode(documentParseResult.sourceCode, params.contentChanges)

        // 前回の解析が tokenize に失敗していたら全文再解析でok
        if (documentParseResult !is CommandDocumentParseResult.Tokenized) {
            documents[textDocument.uri] = parseAll(newSourceCode, documentParseResult.sender)
            return
        }

        val oldTokens = documentParseResult.tokens
    }

    override fun didClose(params: DidCloseTextDocumentParams) {}

    override fun didSave(params: DidSaveTextDocumentParams) {}
}
