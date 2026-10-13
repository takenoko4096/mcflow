package io.github.takenoko4096.bcl.server

import io.github.takenoko4096.bcl.Commands
import io.github.takenoko4096.bcl.brigadier.CommandDispatcher
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageClientAware
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.services.TextDocumentService
import org.eclipse.lsp4j.services.WorkspaceService
import java.util.concurrent.CompletableFuture

class BedrockCommandLanguageServer : LanguageServer, LanguageClientAware {
    private val textDocumentService = BclTextDocumentService(this)

    private val workspaceService = BclWorkspaceService(this)

    lateinit var client: LanguageClient

    val dispatcher = CommandDispatcher("root", detailedOutput = false).apply {
        Commands.getAllVanillaCommandBuilders().forEach(::register)
    }

    override fun connect(client: LanguageClient) {
        this.client = client
    }

    private fun getServerCapabilities(): ServerCapabilities {
        val capabilities = ServerCapabilities()
        capabilities.textDocumentSync = Either.forLeft(TextDocumentSyncKind.Full)
        capabilities.completionProvider = CompletionOptions()
        capabilities.inlayHintProvider = Either.forLeft(true)
        capabilities.semanticTokensProvider = SemanticTokensWithRegistrationOptions(
            SemanticTokensLegend(
                BclSemanticTokenType.entries.map { it.getString() },
                emptyList()
            )
        ).apply { full = Either.forLeft(true) }
        capabilities.completionProvider = CompletionOptions().apply {
            triggerCharacters = listOf(" ", "\"", ",", "=", "@", "/", "~", "^")
        }

        return capabilities
    }

    override fun initialize(initializeParams: InitializeParams): CompletableFuture<InitializeResult> {
        return CompletableFuture.completedFuture(InitializeResult(getServerCapabilities()))
    }

    override fun shutdown(): CompletableFuture<Any> {
        return CompletableFuture.completedFuture(null)
    }

    override fun exit() {
        kotlin.system.exitProcess(0)
    }

    override fun getTextDocumentService(): TextDocumentService {
        return textDocumentService
    }

    override fun getWorkspaceService(): WorkspaceService {
        return workspaceService
    }
}
