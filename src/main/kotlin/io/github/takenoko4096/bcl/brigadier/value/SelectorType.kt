package io.github.takenoko4096.bcl.brigadier.value

enum class SelectorType(private val string: String) : StringRepresentable {
    P("@p"),
    A("@a"),
    R("@r"),
    S("@s"),
    E("@e"),
    N("@n"),
    INITIATOR("@initiator");

    override fun getString(): String {
        return string
    }
}
