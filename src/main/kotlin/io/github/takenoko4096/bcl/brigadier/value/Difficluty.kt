package io.github.takenoko4096.bcl.brigadier.value

enum class Difficluty : StringRepresentable {
    NORMAL,
    EASY,
    HARD,
    PEACEFUL;

    override fun getString(): String {
        return name.lowercase()
    }
}
