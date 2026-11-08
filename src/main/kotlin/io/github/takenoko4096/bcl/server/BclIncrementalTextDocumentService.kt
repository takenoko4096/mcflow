package io.github.takenoko4096.bcl.server

import io.github.takenoko4096.bcl.ast.BclComment
import io.github.takenoko4096.bcl.brigadier.CommandSender
import io.github.takenoko4096.bcl.parser.BclParser
import io.github.takenoko4096.bcl.tokenizer.BclTokenizer
import io.github.takenoko4096.bcl.tokenizer.KeyChar
import io.github.takenoko4096.bcl.tokenizer.SourcePosition
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import io.github.takenoko4096.bcl.tokenizer.Token
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
        val comments = mutableListOf<BclComment>()
        val sourceCode = normalizeSourceCode(text)
        val tokenizer = BclTokenizer(sourceCode, sender, comments)

        if (text.isEmpty()) {
            return CommandDocumentParseResult.Normalized(sender, sourceCode)
        }

        val tokens = try {
            tokenizer.tokenize()
        }
        catch (_: Throwable) {
            return CommandDocumentParseResult.Normalized(sender, sourceCode)
        }

        val parser = BclParser(
            sourceCode,
            tokens,
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
            return CommandDocumentParseResult.Tokenized(sender, sourceCode, tokens, comments)
        }

        return CommandDocumentParseResult.Parsed(sender, sourceCode, tokens, comments, block)
    }

    // TODO
    private fun parse(oldDocumentParseResult: CommandDocumentParseResult.Parsed, newEntireSourceCode: String, targetSourceSpan: SourceSpan, sender: CommandSender): CommandDocumentParseResult {
        val targetSourceCode = targetSourceSpan.getStringFromSourceCode(newEntireSourceCode)
        val documentParseResult = parseAll(targetSourceCode, sender)

        if (documentParseResult is CommandDocumentParseResult.Parsed) {
            return CommandDocumentParseResult.Parsed(
                sender,
                newEntireSourceCode,
                getChangesAppliedTokens(oldDocumentParseResult.tokens, documentParseResult.tokens),

            )
        }
    }

    private fun getChangesAppliedSourceCode(origin: String, changes: List<TextDocumentContentChangeEvent>): String {
        var sourceCode = origin

        for (change in changes) {
            val range = change.range
            val start = SourcePosition.from(sourceCode, range.start)
            val end = SourcePosition.from(sourceCode, range.end)
            sourceCode = buildString {
                append(sourceCode.slice(0..<start.index))
                append(change.text)
                append(sourceCode.slice(end.index..sourceCode.lastIndex))
            }
        }

        return normalizeSourceCode(sourceCode)
    }

    // TODO
    private fun getChangesAppliedTokens(origin: List<Token>, changes: List<Token>): List<Token> {
        if (changes.isEmpty()) {
            return origin
        }

        val changedSourceSpan = changes.first().sourceSpan.start..changes.last().sourceSpan.end

        val tokens = mutableListOf<Token>()

        val startIndex = origin.indexOfFirst { token -> token.sourceSpan.end in changedSourceSpan }
        val endIndex = origin.indexOfLast { token -> token.sourceSpan.start in changedSourceSpan }


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

        val newSourceCode = getChangesAppliedSourceCode(documentParseResult.sourceCode, params.contentChanges)

        if (documentParseResult !is CommandDocumentParseResult.Parsed) {
            documents[textDocument.uri] = parseAll(newSourceCode, documentParseResult.sender)
            return
        }

        val block = documentParseResult.block

        var start: SourcePosition
        var end: SourcePosition

        for (change in params.contentChanges) {
            change.range.start
        }
    }

    override fun didClose(params: DidCloseTextDocumentParams) {}

    override fun didSave(params: DidSaveTextDocumentParams) {}
}
