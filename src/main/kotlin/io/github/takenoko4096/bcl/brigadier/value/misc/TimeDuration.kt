package io.github.takenoko4096.bcl.brigadier.value.misc

data class TimeDuration(val value: Int, val unit: TimeUnit) {
    override fun toString(): String {
        return "$value $unit"
    }
}
