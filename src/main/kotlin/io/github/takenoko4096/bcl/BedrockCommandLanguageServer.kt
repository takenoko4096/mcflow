package io.github.takenoko4096.bcl

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.services.TextDocumentService
import org.eclipse.lsp4j.services.WorkspaceService
import java.util.concurrent.CompletableFuture

class BedrockCommandLanguageServer : LanguageServer {
    private val textDocumentService = BclTextDocumentService(this)

    private val workspaceService = BclWorkspaceService(this)

    override fun initialize(initializeParams: InitializeParams): CompletableFuture<InitializeResult> {
        val capabilities = ServerCapabilities()
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full)
        capabilities.setCompletionProvider(CompletionOptions())
        return CompletableFuture.completedFuture<InitializeResult>(InitializeResult(capabilities))
    }

    override fun shutdown(): CompletableFuture<Any> {
        return CompletableFuture.completedFuture<Any>(null)
    }

    override fun exit() {
    }

    override fun getTextDocumentService(): TextDocumentService {
        return textDocumentService
    }

    override fun getWorkspaceService(): WorkspaceService {
        return workspaceService
    }
}
