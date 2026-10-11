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
    console.log("mcflow: activate begin");

    const jarPath = context.asAbsolutePath(
        path.join("server", "mcflow-lsp.jar")
    );

    console.log("mcflow: jarPath =", jarPath);

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

    console.log("mcflow: starting language client");

    client.start().catch(error => {
        console.error("mcflow: failed to start language client:", error);
        vscode.window.showErrorMessage(
            `mcflow LSP failed to start: ${error}`
        );
    });

    context.subscriptions.push(client);

    console.log("mcflow: activate end");
}

export function deactivate(): Thenable<void> | undefined {
    return client?.stop();
}
