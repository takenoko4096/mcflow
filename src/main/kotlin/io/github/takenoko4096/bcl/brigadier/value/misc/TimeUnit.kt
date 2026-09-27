package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class TimeUnit(private val string: String) : StringRepresentable {
    DAY("day(s)"),
    SECOND("second(s)"),
    TICK("tick(s)");

    override fun getString(): String {
        return string
    }
}
