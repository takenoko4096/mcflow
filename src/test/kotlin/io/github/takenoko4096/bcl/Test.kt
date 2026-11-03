package io.github.takenoko4096.bcl

import io.github.takenoko4096.bcl.brigadier.dispatcher.CommandDispatcher
import org.junit.jupiter.api.Test

object Test {
    @Test
    fun test() {
        val dispatcher = CommandDispatcher("root")
        Commands.getAllVanillaCommandBuilders().forEach(dispatcher::register)
        val root = dispatcher.getRoot()
        println(root.children.map { it.key })
    }
}
