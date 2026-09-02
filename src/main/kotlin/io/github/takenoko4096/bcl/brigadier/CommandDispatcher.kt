package io.github.takenoko4096.bcl.brigadier

class CommandDispatcher {
    private val commands = mutableMapOf<String, Command>()

    fun getCommand(name: String): Command? {
        return commands[name]
    }
}
