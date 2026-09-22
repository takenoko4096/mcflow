package io.github.takenoko4096.bcl.server

import org.eclipse.lsp4j.launch.LSPLauncher
import java.io.PrintStream

fun main() {
    System.setErr(PrintStream(System.err, true, Charsets.UTF_8))

    val server = BedrockCommandLanguageServer()

    val launcher = LSPLauncher.createServerLauncher(
        server,
        System.`in`,
        System.out
    )

    val client = launcher.remoteProxy
    server.connect(client)

    launcher.startListening().get()
}
