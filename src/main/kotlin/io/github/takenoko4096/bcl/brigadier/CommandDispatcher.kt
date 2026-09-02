package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.ast.command.CommandLineParseResult

class CommandDispatcher {
    private val commands = mutableMapOf<String, Command>()

    fun getCommand(name: String): Command? {
        return commands[name]
    }

    fun parse(string: String): CommandLineParseResult {

    }
}
