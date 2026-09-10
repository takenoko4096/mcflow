package io.github.takenoko4096.bcl

import io.github.takenoko4096.bcl.brigadier.CommandDispatcher
import io.github.takenoko4096.bcl.brigadier.argument.BooleanArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.EntitySelectorArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.EnumArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.FloatArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.IntegerArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.PositionArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.RotationArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.StringArgumentType
import io.github.takenoko4096.bcl.brigadier.value.misc.CameraPreset
import io.github.takenoko4096.bcl.brigadier.value.misc.CommandName
import io.github.takenoko4096.bcl.brigadier.value.misc.EasingType

fun getVanillaDispatcher(): CommandDispatcher {
    return CommandDispatcher().apply {
        aimassist()
        alwaysday()
        camera()
        help()
    }
}

fun CommandDispatcher.help() {
    register("help") {
        description = "コマンドの一覧またはヘルプを呼び出します。"

        aliases.add("?")

        executes()

        "page"(IntegerArgumentType.integer()) {
            executes()
        }

        "command_name"(EnumArgumentType.of<CommandName>()) {
            executes {
                val arg = getArgument<CommandName>("command_name")
                val command = getCommand(arg.value.getString()) ?: throw exception(arg.sourceSpan, "NEVER HAPPENS")

                command.getTreeUsage()
            }
        }
    }
}

fun CommandDispatcher.aimassist() {
    register("aimassist") {
        description = "エイムアシストを有効にします。"

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
        description = "昼夜サイクルを停止及び再開します。"

        executes {
            "昼夜サイクルが既に停止されている場合は再開し、そうでなければ停止します。"
        }

        "lock"(BooleanArgumentType()) {
            executes {
                if (getValue("lock")) {
                    "昼夜サイクルを停止します。"
                }
                else {
                    "昼夜サイクルを再開します。"
                }
            }
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

                "color" {
                    "red"(IntegerArgumentType.integer(0..255)) {
                        "green"(IntegerArgumentType.integer(0..255)) {
                            "blue"(IntegerArgumentType.integer(0..255)) {
                                executes()
                            }
                        }
                    }
                }

                "time" {
                    "in_seconds"(FloatArgumentType.float(0f..10f)) {
                        "hold_seconds"(FloatArgumentType.float(0f..10f)) {
                            "out_seconds"(FloatArgumentType.float(0f..10f)) {
                                executes()

                                "color" {
                                    "red"(IntegerArgumentType.integer(0..255)) {
                                        "green"(IntegerArgumentType.integer(0..255)) {
                                            "blue"(IntegerArgumentType.integer(0..255)) {
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

            "fov_clear" {
                executes()

                "fov_ease_time"(FloatArgumentType.float()) {
                    executes()

                    "fov_ease_type"(EnumArgumentType.of<EasingType>()) {
                        executes()
                    }
                }
            }

            "fov_set" {
                "fov_value"(FloatArgumentType.float()) {
                    executes()

                    "fov_ease_time"(FloatArgumentType.float()) {
                        executes()

                        "fov_ease_type"(EnumArgumentType.of<EasingType>()) {
                            executes()
                        }
                    }
                }
            }

            "play_spline" {
                "name"(StringArgumentType.isolated()) {
                    executes()
                }
            }

            "remove_target" {
                executes()
            }

            "set" {
                "preset"(EnumArgumentType.of<CameraPreset>()) {
                    executes()

                    "default" {
                        executes()
                    }

                    "ease" {
                        "ease_time"(FloatArgumentType.float()) {
                            "easing_type"(EnumArgumentType.of<EasingType>()) {
                                executes()

                                "default" {
                                    executes()
                                }

                                "entity_offset" {
                                    "x_entity_offset"(FloatArgumentType.float()) {
                                        "y_entity_offset"(FloatArgumentType.float()) {
                                            "z_entity_offset"(FloatArgumentType.float()) {
                                                executes()
                                            }
                                        }
                                    }
                                }

                                "facing" {
                                    "look_at_entity"(EntitySelectorArgumentType()) {
                                        executes()
                                    }

                                    "look_at_position"(PositionArgumentType()) {
                                        executes()
                                    }
                                }

                                "pos" {
                                    "position"(PositionArgumentType()) {
                                        executes()

                                        "facing" {
                                            "look_at_entity"(EntitySelectorArgumentType()) {
                                                executes()
                                            }

                                            "look_at_position"(PositionArgumentType()) {
                                                executes()
                                            }
                                        }

                                        "rot" {
                                            "rotation"(RotationArgumentType()) {
                                                executes()
                                            }
                                        }
                                    }
                                }

                                "rot" {
                                    "rotation"(RotationArgumentType()) {
                                        executes()

                                        "entity_offset" {
                                            executes()
                                        }

                                        "view_offset" {
                                            executes()
                                        }
                                    }
                                }

                                "view_offset" {
                                    "x_view_offset"(FloatArgumentType.float()) {
                                        "y_view_offset"(FloatArgumentType.float()) {
                                            executes()

                                            "entity_offset" {
                                                "x_entity_offset"(FloatArgumentType.float()) {
                                                    "y_entity_offset"(FloatArgumentType.float()) {
                                                        "z_entity_offset"(FloatArgumentType.float()) {
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
                    }
                }
            }

            "target_entity" {
                "entity"(EntitySelectorArgumentType()) { // TODO selector の制約が不明
                    executes()

                    "target_center_offset" {
                        "x_target_center_offset"(FloatArgumentType.float()) {
                            "y_target_center_offset"(FloatArgumentType.float()) {
                                "z_target_center_offset"(FloatArgumentType.float()) {
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

fun CommandDispatcher.camerashake() {
    register("camerashake") {

    }
}
