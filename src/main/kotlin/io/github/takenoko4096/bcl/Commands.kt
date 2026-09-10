package io.github.takenoko4096.bcl

import io.github.takenoko4096.bcl.brigadier.CommandDispatcher
import io.github.takenoko4096.bcl.brigadier.argument.BooleanArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.EntitySelectorArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.FloatArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.StringArgumentType

fun getDefaultDispatcher(): CommandDispatcher {
    return CommandDispatcher().apply {
        aimassist()
    }
}

fun CommandDispatcher.aimassist() {
    register("aimassist") {
        "players"(EntitySelectorArgumentType.players()) {
            "clear" {
                executes()
            }

            "set" {
                executes()

                "x"(FloatArgumentType.float(10f..90f)) {
                    executes()

                    "y"(FloatArgumentType.float(10f..90f)) {
                        executes()

                        "max_distance"(FloatArgumentType.float(1f..16f)) {
                            executes()

                            "angle" {
                                executes()

                                "preset"(StringArgumentType.isolated()) {
                                    executes()
                                }
                            }

                            "distance" {
                                executes()

                                "preset"(StringArgumentType.isolated()) {
                                    executes()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun CommandDispatcher.alwaysday() {
    register("alwaysday") {
        executes()

        "lock"(BooleanArgumentType()) {
            executes()
        }
    }
}

fun CommandDispatcher.camera() {
    register("camera") {
        "players"(EntitySelectorArgumentType.players()) {
            "attach_to_entity" {
                "target"(EntitySelectorArgumentType()) {
                    executes()
                }
            }

            "clear" {
                executes()
            }

            "detach_from_entity" {
                executes()
            }

            "fade" {
                executes()
            }
        }
    }
}
