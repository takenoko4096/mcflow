package io.github.takenoko4096.bcl.brigadier.node

class LiteralArgumentNode(
    val literal: String,
    children: Set<ArgumentNode>,
) : ArgumentNode(children)
