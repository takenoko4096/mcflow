package io.github.takenoko4096.bcl.brigadier

enum class CommandPermissionLevel(val integer: Int) {
    BANNED(-1),
    MEMBER(0),
    GAME_DIRECTOR(1),
    ADMINISTRATOR(2),
    HOST(3),
    OWNER(4);
}
