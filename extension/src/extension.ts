import * as vscode from "vscode";
import { LanguageClient, Trace } from "vscode-languageclient/node";
import * as path from "node:path";

let client: LanguageClient;

export function activate(context: vscode.ExtensionContext) {
    const jar = context.asAbsolutePath(path.join("server", "mcflow-lsp.jar"));

    client = new LanguageClient(
        "mcflow",
        "mcflow",
        {
            command: "java",
            args: ["-jar", jar]
        },
        {
            documentSelector: [
                { scheme: "file", language: "mcfunction" },
                { scheme: "file", language: "mcflow" }
            ]
        }
    );

    client.setTrace(Trace.Verbose);

    client.start()
        .catch(error => {
            console.error("mcflow: failed to start language client:", error);
            vscode.window.showErrorMessage(`mcflow LSP failed to start: ${error}`);
        });

    context.subscriptions.push(client);
}

export function deactivate(): Thenable<void> | undefined {
    return client?.stop();
}
