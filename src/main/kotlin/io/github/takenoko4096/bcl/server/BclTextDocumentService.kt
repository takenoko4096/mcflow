package io.github.takenoko4096.bcl.server

import io.github.takenoko4096.bcl.ast.Block
import io.github.takenoko4096.bcl.brigadier.BclComment
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.Command
import io.github.takenoko4096.bcl.brigadier.CommandLineParseResult
import io.github.takenoko4096.bcl.brigadier.CommandSender
import io.github.takenoko4096.bcl.brigadier.node.ArgumentCommandNode
import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode
import io.github.takenoko4096.bcl.completer.BclArgumentCompletionException
import io.github.takenoko4096.bcl.parser.BclParser
import io.github.takenoko4096.bcl.tokenizer.BclTokenizer
import io.github.takenoko4096.bcl.tokenizer.KeyChar
import io.github.takenoko4096.bcl.tokenizer.SourcePosition
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.jsonrpc.messages.Tuple
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.TextDocumentService
import java.util.concurrent.CompletableFuture

class BclTextDocumentService(private val server: BedrockCommandLanguageServer) : TextDocumentService {
    private val files = mutableMapOf<String, CommandDocumentFile>()

    private val client: LanguageClient
        get() = server.client

    private fun parse(uri: String?, text: String, extension: Boolean): Block {
        val comments = mutableListOf<BclComment>()
        val tokenizer = BclTokenizer(text, extension, comments)
        val tokens = tokenizer.tokenize()

        val sender = CommandSender(extension = extension)
        val parser = BclParser(tokenizer.sourceCode, tokens, server.dispatcher, sender)
        val commandLineExclusiveSemanticTokens = mutableListOf<BclSemanticToken>()
        val commentCommandParseResults = mutableListOf<CommandLineParseResult>()

        for (comment in comments) {
            if (comment is BclComment.CommentCommandLine) {
                val prefixSourceSpan = comment.reader.getSourceCodeFirstPosition()..comment.reader.getPreviousTokenEndPosition()
                commandLineExclusiveSemanticTokens.add(BclSemanticToken(BclSemanticTokenType.COMMENT, prefixSourceSpan))
                commentCommandParseResults.add(server.commentCommandDispatcher.parse(comment.reader, sender))
            }
            else {
                val semanticToken = BclSemanticToken(BclSemanticTokenType.COMMENT, comment.sourceSpan)
                commandLineExclusiveSemanticTokens.add(semanticToken)
            }
        }

        val block = parser.parse(commandLineExclusiveSemanticTokens)

        if (uri != null) {
            files[uri] = CommandDocumentFile(
                sourceCode = tokenizer.sourceCode,
                block = block,
                comments = comments,
                commentCommandParseResults = commentCommandParseResults,
                commandLineExclusiveSemanticTokens = commandLineExclusiveSemanticTokens
            )
        }

        return block
    }

    private fun publishDiagnostics(uri: String, block: Block, commentCommandParseResults: List<CommandLineParseResult>) {
        client.publishDiagnostics(PublishDiagnosticsParams(
            uri,
            (block.nodes.filterIsInstance<CommandLineParseResult.Failure>() + commentCommandParseResults.filterIsInstance<CommandLineParseResult.Failure>())
                .flatMap {
                    it.diagnostics.map(BclDiagnostic::toLsp4jDiagnostic)
                }
        ))
    }

    private fun getLastLineParseResult(block: Block, commentCommandParseResults: List<CommandLineParseResult>): CommandLineParseResult? {
        val commandLineParseResults = block.nodes.filterIsInstance<CommandLineParseResult>()
        val results = (commandLineParseResults + commentCommandParseResults).sortedBy { it.sourceSpan.start.index }
        return results.lastOrNull()
    }

    override fun didOpen(params: DidOpenTextDocumentParams) {
        parse(
            params.textDocument.uri,
            params.textDocument.text,
            extension = params.textDocument.uri.endsWith(".mcflow")
        )
        val file = files[params.textDocument.uri]!!
        publishDiagnostics(params.textDocument.uri, file.block, file.commentCommandParseResults)
    }

    override fun didChange(params: DidChangeTextDocumentParams) {
        val uri = params.textDocument.uri
        val text = params.contentChanges.last().text
        parse(uri, text, extension = uri.endsWith(".mcflow"))
        val file = files[uri]!!
        publishDiagnostics(uri, file.block, file.commentCommandParseResults)
    }

    override fun didClose(params: DidCloseTextDocumentParams) {}

    override fun didSave(params: DidSaveTextDocumentParams) {}

    override fun inlayHint(params: InlayHintParams): CompletableFuture<List<InlayHint>> {
        val document = params.textDocument
        val file = files[document.uri]!!
        val block = file.block

        val hints = mutableListOf<InlayHint>()

        for (result in block.nodes.filterIsInstance<CommandLineParseResult>() + file.commentCommandParseResults) {
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

        val file = files[uri] ?: return empty
        val sourceCode = file.sourceCode

        val comments = file.comments.filterNot { it is BclComment.CommentCommandLine }
        if (comments.any { it.sourceSpan.contains(position) }) return empty

        val sourcePosition = SourcePosition.from(sourceCode, position)
        val cursor = sourcePosition.index

        val subSourceCode = sourceCode.substring(0, cursor)
            .replace(KeyChar.WINDOWS_LINE_SEPARATOR_SEQUENCE, KeyChar.LINEBREAK.toString())
            .replace(KeyChar.MAC_LINE_SEPARATOR, KeyChar.LINEBREAK)

        val block = parse(null, subSourceCode, extension = uri.endsWith(".mcflow"))

        val result = getLastLineParseResult(block, file.commentCommandParseResults) ?: return empty

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
                                getStringFromSourceCode(subSourceCode)
                            }
                            catch (_: Throwable) {
                                ""
                            }
                        }
                        ?: ""

                    val context = BclCompletionContext(
                        string,
                        candidate.startPosition,
                        candidate.inputSourceSpan,
                        sourceCode
                    )

                    val completionItems = try {
                        if (candidate.node.completerOverride == null) {
                            candidate.node.type.complete(context)
                        }
                        else {
                            candidate.node.completerOverride(context)
                        }
                    }
                    catch (_: BclArgumentCompletionException) {
                        continue
                    }

                    items.addAll(completionItems)
                }
                else -> throw IllegalStateException("NEVER HAPPENS")
            }
        }

        val completionItems = items.distinctBy { it.label to it.textEdit }

        return CompletableFuture.completedFuture(Either.forLeft(completionItems))
    }

    override fun semanticTokensFull(params: SemanticTokensParams): CompletableFuture<SemanticTokens> {
        val semanticTokens = SemanticTokens()

        val file = files[params.textDocument.uri]!!

        val block = file.block
        val commandLineExclusiveSemanticTokens = file.commandLineExclusiveSemanticTokens

        val tokens = (
            (block.nodes.filterIsInstance<CommandLineParseResult>() + file.commentCommandParseResults)
            .flatMap { it.semanticTokens } + commandLineExclusiveSemanticTokens
            )
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

    override fun signatureHelp(params: SignatureHelpParams): CompletableFuture<SignatureHelp?> {
        val textDocument = params.textDocument
        val file = files[textDocument.uri]!!
        val sourceCode = file.sourceCode

        val comments = file.comments.filterNot { it is BclComment.CommentCommandLine }
        if (comments.any { it.sourceSpan.contains(params.position) }) {
            return CompletableFuture.completedFuture(null)
        }

        val cursor = SourcePosition.from(sourceCode, params.position).index

        val text = sourceCode.substring(0, cursor)
            .replace(KeyChar.WINDOWS_LINE_SEPARATOR_SEQUENCE, KeyChar.LINEBREAK.toString())
            .replace(KeyChar.MAC_LINE_SEPARATOR, KeyChar.LINEBREAK)

        val block = parse(null, text, extension = textDocument.uri.endsWith(".mcflow"))
        val result = getLastLineParseResult(block, file.commentCommandParseResults) ?: return CompletableFuture.completedFuture(null)

        if (result.lastSourcePosition != SourcePosition.NULL && (params.position.line + 1) > result.lastSourcePosition.line) {
            return CompletableFuture.completedFuture(null)
        }

        val command = result.route.firstOrNull() ?: return CompletableFuture.completedFuture(null)
        command as Command

        val route = result.route
        val tail = route.last()
        val children = tail.getEffectiveChildren().values

        if (children.isEmpty()) {
            return CompletableFuture.completedFuture(null)
        }

        return CompletableFuture.completedFuture(SignatureHelp(
            children.map { child ->
                val ranges = mutableListOf<Pair<Tuple.Two<Int, Int>, String>>()

                var i = 0
                for (node in route + child) {
                    val string = if (node == child && tail.executor != null) "[$node]" else node.toString()
                    ranges += Tuple.two(i, i + string.length) to string
                    i += string.length
                    i++ // ' ' の分
                }

                val label = ranges.joinToString(" ") { (_, label) -> label }
                val parameters = ranges.map { (range, _) ->
                    val info = ParameterInformation()
                    info.label = Either.forRight(range)
                    return@map info
                }

                SignatureInformation(label, "", parameters)
            },
            null,
            route.size
        ))
    }

    /*override fun hover(params: HoverParams): CompletableFuture<Hover?> {
        val block = blocks[params.textDocument.uri]!!

        val result = block.nodes.filterIsInstance<CommandLineParseResult>()
            .find {
                val command = it.route.firstOrNull()
                if (command !is Command) return@find false
                val sourceSpan = it.literals.find { (node, _) -> node == command }?.second ?: return@find false
                return@find params.position in sourceSpan
            }
            ?: return CompletableFuture.completedFuture(null)

        val command = result.route.first() as Command

        return CompletableFuture.completedFuture(Hover(
            MarkupContent(
                MarkupKind.PLAINTEXT,
                """
                /${command.name} - ${command.description}
                
                {USAGE}
                """.trimIndent()
                    .replace(
                        "{USAGE}",
                        command.getTreeUsage()
                    )
            )
        ))
    }*/
}
