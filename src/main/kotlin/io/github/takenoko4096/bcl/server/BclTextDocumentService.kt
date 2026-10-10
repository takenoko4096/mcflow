package io.github.takenoko4096.bcl.server

import io.github.takenoko4096.bcl.ast.Block
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.CommandLineParseResult
import io.github.takenoko4096.bcl.brigadier.node.ArgumentCommandNode
import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode
import io.github.takenoko4096.bcl.completer.BclArgumentCompletionException
import io.github.takenoko4096.bcl.parser.BclParser
import io.github.takenoko4096.bcl.tokenizer.BclTokenizer
import io.github.takenoko4096.bcl.tokenizer.KeyChar
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.TextDocumentService
import java.util.concurrent.CompletableFuture

class BclTextDocumentService(private val server: BedrockCommandLanguageServer) : TextDocumentService {
    private val documents = mutableMapOf<String, String>()

    private val blocks = mutableMapOf<String, Block>()

    private val extraSemanticTokens = mutableMapOf<String, List<BclSemanticToken>>()

    private val client: LanguageClient
        get() = server.client

    private fun parse(uri: String?, text: String): Block {
        val comments = mutableListOf<SourceSpan>()
        val tokenizer = BclTokenizer(text, interpretComments = true, commentRangeCollector = comments)
        val tokens = tokenizer.tokenize()
        System.err.println(comments.flatMap {
            it.split(tokens).map { sourceSpan ->
                BclSemanticToken(BclSemanticTokenType.COMMENT, sourceSpan)
            }
        })
        val parser = BclParser(tokenizer.sourceCode, tokens, server.dispatcher, extension = false)
        val semanticTokens = mutableListOf<BclSemanticToken>()
        semanticTokens.addAll(comments.flatMap {
            it.split(tokens).map { sourceSpan ->
                BclSemanticToken(BclSemanticTokenType.COMMENT, sourceSpan)
            }
        })
        val block = parser.parse(semanticTokens)
        if (uri != null) {
            blocks[uri] = block
            extraSemanticTokens[uri] = semanticTokens
            documents[uri] = tokenizer.sourceCode
        }
        return block
    }

    private fun publishDiagnostics(uri: String, block: Block) {
        client.publishDiagnostics(PublishDiagnosticsParams(
            uri,
            block.nodes
                .filterIsInstance<CommandLineParseResult.Failure>()
                .flatMap {
                    it.diagnostics.map(BclDiagnostic::toLsp4jDiagnostic)
                }
        ))
    }

    override fun didOpen(params: DidOpenTextDocumentParams) {
        val document = params.textDocument
        val block = parse(document.uri, document.text)
        blocks[document.uri] = block
        publishDiagnostics(document.uri, block)
    }

    override fun didChange(params: DidChangeTextDocumentParams) {
        val uri = params.textDocument.uri
        val text = params.contentChanges.last().text
        val block = parse(uri, text)
        publishDiagnostics(uri, block)
    }

    override fun didClose(params: DidCloseTextDocumentParams) {}

    override fun didSave(params: DidSaveTextDocumentParams) {}

    override fun inlayHint(params: InlayHintParams): CompletableFuture<List<InlayHint>> {
        val document = params.textDocument
        val block = blocks[document.uri]!!

        val hints = mutableListOf<InlayHint>()

        for (result in block.nodes.filterIsInstance<CommandLineParseResult>()) {
            for ((node, argument) in result.arguments) {
                if (!argument.type.hasInlayHint) continue

                val pos = argument.sourceSpan.start.toLsp4jPosition() ?: continue
                hints.add(InlayHint().apply {
                    position = pos
                    label = Either.forLeft("${node.name}:")
                    kind = InlayHintKind.Parameter
                    paddingRight = true
                })
            }
        }

        return CompletableFuture.completedFuture(hints)
    }

    override fun completion(params: CompletionParams): CompletableFuture<Either<List<CompletionItem>, CompletionList>> {
        val uri = params.textDocument.uri
        val position = params.position
        val empty = CompletableFuture.completedFuture(Either.forLeft<List<CompletionItem>, CompletionList>(emptyList()))

        val sourceCode = documents[uri] ?: return empty

        var cursor = 0
        var currentChar: Char
        var line = 0
        var lastLineStartCursor = 0

        while (!(line == position.line && (cursor - lastLineStartCursor) == position.character) && cursor < sourceCode.length) {
            currentChar = sourceCode[cursor]
            cursor++

            if (currentChar == KeyChar.LINEBREAK) {
                line++
                lastLineStartCursor = cursor
            }
        }

        val text = sourceCode.substring(0, cursor)
            .replace(KeyChar.WINDOWS_LINE_SEPARATOR_SEQUENCE, KeyChar.LINEBREAK.toString())
            .replace(KeyChar.MAC_LINE_SEPARATOR, KeyChar.LINEBREAK)
        val block = parse(null, text)
        val result = block.nodes.lastOrNull() ?: return empty

        // always passes
        result as CommandLineParseResult

        val items = mutableListOf<CompletionItem>()

        for (candidate in result.completionCandidates) {
            when (candidate.node) {
                is LiteralCommandNode -> {
                    val item = CompletionItem(candidate.node.name)
                    item.kind = CompletionItemKind.Function

                    candidate.inputSourceSpan?.run {
                        item.textEdit = Either.forLeft(TextEdit(
                            toLsp4jRange(),
                            candidate.node.name
                        ))
                    }

                    items.add(item)
                }
                is ArgumentCommandNode<*> -> {
                    val string = candidate.inputSourceSpan
                        ?.run {
                            try {
                                getStringFromSourceCode(text)
                            }
                            catch (_: Throwable) {
                                ""
                            }
                        }
                        ?: ""

                    val context = BclCompletionContext(
                        string,
                        candidate.startPosition,
                        candidate.inputSourceSpan
                    )

                    val completionItems = try {
                        candidate.node.type.complete(context)
                    }
                    catch (_: BclArgumentCompletionException) {
                        continue
                    }

                    items.addAll(completionItems)
                }
                else -> throw IllegalStateException("NEVER HAPPENS")
            }
        }

        return CompletableFuture.completedFuture(Either.forLeft(items.distinctBy {
            it.label to it.textEdit
        }))
    }

    override fun semanticTokensFull(params: SemanticTokensParams): CompletableFuture<SemanticTokens> {
        val semanticTokens = SemanticTokens()

        val block = blocks[params.textDocument.uri]!!
        val extra = extraSemanticTokens[params.textDocument.uri]!!

        val tokens = (block.nodes
            .filterIsInstance<CommandLineParseResult>()
            .flatMap { it.semanticTokens } + extra)
            .sortedBy { it.sourceSpan.start.index }
            .toList()

        val data = mutableListOf<Int>()

        var previousLine = 0
        var previousStart = 0

        for (token in tokens) {
            val start = token.sourceSpan.start
            val end = token.sourceSpan.end

            // SourcePositionのlineが1始まりである場合
            val line = start.line - 1
            val character = start.column

            val deltaLine = line - previousLine
            val deltaStart = if (deltaLine == 0) {
                character - previousStart
            }
            else {
                character
            }

            val length = end.column - start.column + 1

            data.add(deltaLine)
            data.add(deltaStart)
            data.add(length)
            data.add(token.type.ordinal)
            data.add(0)

            previousLine = line
            previousStart = character
        }

        semanticTokens.data = data

        return CompletableFuture.completedFuture(semanticTokens)
    }
}
