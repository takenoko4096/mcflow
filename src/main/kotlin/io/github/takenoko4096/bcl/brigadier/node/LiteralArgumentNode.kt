package io.github.takenoko4096.bcl.brigadier.node

class LiteralArgumentNode(
    val literal: String,
    child: ArgumentNode?,
) : ArgumentNode(child)
