package io.github.takenoko4096.bcl.server

import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.CommandLineParseResult
import io.github.takenoko4096.bcl.parser.BclParser
import io.github.takenoko4096.bcl.tokenizer.BclTokenizer
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.TextDocumentService
import java.util.concurrent.CompletableFuture

class BclTextDocumentService(private val server: BedrockCommandLanguageServer) : TextDocumentService {
    private val documents = mutableMapOf<String, String>()

    private val client: LanguageClient
        get() = server.client

    override fun didOpen(params: DidOpenTextDocumentParams) {
        val document = params.textDocument
        documents[document.uri] = document.text
        analyze(document.uri, document.text)
    }

    private fun analyze(uri: String, text: String) {
        val tokenizer = BclTokenizer(text, interpretComments = false)
        val tokens = tokenizer.tokenize()
        val parser = BclParser(tokenizer.sourceCode, tokens, server.dispatcher, extension = false)
        val block = parser.parse()

        val results = block.nodes.map { it as CommandLineParseResult }

        val diagnostics = mutableListOf<Diagnostic>()

        for (result in results) {
            if (result !is CommandLineParseResult.Failure) continue

            System.err.println("stderr: '${result}'")

            diagnostics.addAll(
                result.diagnostics.map(BclDiagnostic::toLsp4jDiagnostic)
            )
        }

        client.publishDiagnostics(
            PublishDiagnosticsParams(uri, diagnostics)
        )
    }

    override fun didChange(params: DidChangeTextDocumentParams) {
        val document = params.textDocument
        val text = params.contentChanges.last().text
        documents[document.uri] = text
        analyze(document.uri, text)
    }

    override fun didClose(params: DidCloseTextDocumentParams) {
    }

    override fun didSave(params: DidSaveTextDocumentParams) {
    }

    override fun completion(position: CompletionParams): CompletableFuture<Either<MutableList<CompletionItem>, CompletionList>> {
        return super.completion(position)
    }
}
