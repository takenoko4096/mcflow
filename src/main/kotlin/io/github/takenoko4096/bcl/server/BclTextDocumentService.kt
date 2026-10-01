package io.github.takenoko4096.bcl.server

import io.github.takenoko4096.bcl.ast.Block
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.CommandLineParseResult
import io.github.takenoko4096.bcl.parser.BclParser
import io.github.takenoko4096.bcl.tokenizer.BclTokenizer
import io.github.takenoko4096.bcl.tokenizer.SourcePosition
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.TextDocumentService
import java.util.concurrent.CompletableFuture

class BclTextDocumentService(private val server: BedrockCommandLanguageServer) : TextDocumentService {
    private val documents = mutableMapOf<String, Block>()

    private val client: LanguageClient
        get() = server.client

    private fun parse(uri: String, text: String): Block {
        val tokenizer = BclTokenizer(text, interpretComments = false)
        val tokens = tokenizer.tokenize()
        val parser = BclParser(tokenizer.sourceCode, tokens, server.dispatcher, extension = false)
        val block = parser.parse()
        documents[uri] = block
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
        documents[document.uri] = block
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
        val block = documents[document.uri]!!

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

    override fun completion(position: CompletionParams): CompletableFuture<Either<MutableList<CompletionItem>, CompletionList>> {
        return super.completion(position)
    }

    override fun semanticTokensFull(params: SemanticTokensParams): CompletableFuture<SemanticTokens> {
        System.err.println("semanticTokensFull")

        val semanticTokens = SemanticTokens()

        val block = documents[params.textDocument.uri]!!

        val tokens = block.nodes
            .filterIsInstance<CommandLineParseResult>()
            .flatMap { it.semanticTokens }
            .sortedBy { it.sourceSpan.start.index }

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
            } else {
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
