package io.github.takenoko4096.bcl.brigadier.node

import io.github.takenoko4096.bcl.ast.Type

class RequiredCommandNode(
    val name: String,
    val type: Type,
    children: Set<CommandNode>
) : CommandNode(children)
