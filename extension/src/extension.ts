import * as vscode from "vscode";
import {
    LanguageClient,
    type LanguageClientOptions,
    type ServerOptions,
    Trace
} from "vscode-languageclient/node";
import * as path from "node:path";

let client: LanguageClient;

export function activate(context: vscode.ExtensionContext) {
    const jarPath = context.asAbsolutePath(
        path.join("..", "build", "libs", "mcflow-lsp.jar")
    );

    const serverOptions: ServerOptions = {
        command: "java",
        args: ["-jar", jarPath]
    };

    const clientOptions: LanguageClientOptions = {
        documentSelector: [
            {
                scheme: "file",
                language: "mcfunction"
            },
            {
                scheme: "file",
                language: "mcflow"
            }
        ]
    };

    client = new LanguageClient(
        "mcflow",
        "mcflow",
        serverOptions,
        clientOptions
    );

    client.setTrace(Trace.Verbose);

    client.start();

    context.subscriptions.push(client);
}

export function deactivate(): Thenable<void> | undefined {
    return client?.stop();
}
