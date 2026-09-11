package io.github.takenoko4096.bcl

import io.github.takenoko4096.bcl.brigadier.Command
import io.github.takenoko4096.bcl.brigadier.argument.BlockPropertiesArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.BlockTypeArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.BooleanArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.EntitySelectorArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.EnumArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.FloatArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.IntegerArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.ItemTypeArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.PositionArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.RotationArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.StringArgumentType
import io.github.takenoko4096.bcl.brigadier.value.misc.CameraPreset
import io.github.takenoko4096.bcl.brigadier.value.misc.CameraShakeType
import io.github.takenoko4096.bcl.brigadier.value.misc.CloneMode
import io.github.takenoko4096.bcl.brigadier.value.misc.ControlScheme
import io.github.takenoko4096.bcl.brigadier.value.misc.DamageCause
import io.github.takenoko4096.bcl.brigadier.value.misc.Difficulty
import io.github.takenoko4096.bcl.brigadier.value.misc.EasingType
import io.github.takenoko4096.bcl.brigadier.value.misc.MaskMode

object Commands {
    val AIMASSIST = Command.create("aimassist") {
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

    val CAMERA = Command.create("camera") {
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

    val CAMERASHAKE = Command.create("camerashake") {
        "add" {
            "players"(EntitySelectorArgumentType.players()) {
                executes()

                "intensity"(FloatArgumentType.float(0f..4f)) {
                    executes()

                    "seconds"(FloatArgumentType.nonnegative()) {
                        executes()

                        "shake_type"(EnumArgumentType.of<CameraShakeType>()) {
                            executes()
                        }
                    }
                }
            }
        }

        "stop" {
            executes()

            "players"(EntitySelectorArgumentType.players()) {
                executes()
            }
        }
    }

    val CLEAR = Command.create("clear") {
        executes()

        "players"(EntitySelectorArgumentType.players()) {
            executes()

            "item_name"(ItemTypeArgumentType()) {
                executes()

                "count"(IntegerArgumentType.integer()) {
                    executes()
                }
            }
        }
    }

    val CLEARSPAWNPOINT = Command.create("clearspawnpoint") {
        executes()

        "players"(EntitySelectorArgumentType.players()) {
            executes()
        }
    }

    val CLONE = Command.create("clone") {
        "begin"(PositionArgumentType()) {
            "end"(PositionArgumentType()) {
                "destination"(PositionArgumentType()) {
                    executes()

                    "mask_mode"(EnumArgumentType.of<MaskMode>()) {
                        executes()

                        "clone_mode"(EnumArgumentType.of<CloneMode>()) {
                            executes()
                        }
                    }

                    "filtered" {
                        "clone_mode"(EnumArgumentType.of<CloneMode>()) {
                            "tile_name"(BlockTypeArgumentType()) {
                                executes()

                                "block_properties"(BlockPropertiesArgumentType()) {
                                    executes()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val CONNECT = Command.create("connect") {
        "server_uri"(StringArgumentType.greedy()) {
            executes()
        }
    }

    val CONTROLSCHEME = Command.create("controlscheme") {
        "players"(EntitySelectorArgumentType.players()) {
            "clear" {
                executes()
            }

            "set" {
                "control_scheme"(EnumArgumentType.of<ControlScheme>()) {
                    executes()
                }
            }
        }
    }

    val DAMAGE = Command.create("damage") {
        "target"(EntitySelectorArgumentType()) {
            "amount"(IntegerArgumentType.integer()) {
                executes()

                "cause"(EnumArgumentType.of<DamageCause>()) {
                    executes()

                    "entity" {
                        "damager"(EntitySelectorArgumentType()) {
                            executes()
                        }
                    }
                }
            }
        }
    }

    val DAYLOCK = Command.create("daylock") {
        description = "昼夜サイクルを停止及び再開します。"

        aliases.add("alwaysday")

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

    val DEOP = Command.create("deop") {
        "player"(EntitySelectorArgumentType.players()) {
            executes()
        }
    }

    val DIALOGUE = Command.create("dialogue") {
        "change" {
            "npc"(EntitySelectorArgumentType()) {
                "scene_name"(StringArgumentType.isolated()) {
                    executes()

                    "players"(EntitySelectorArgumentType.players()) {
                        executes()
                    }
                }
            }
        }

        "open" {
            "npc"(EntitySelectorArgumentType()) {
                "players"(EntitySelectorArgumentType.players()) {
                    executes()

                    "scene_name"(StringArgumentType.isolated()) {
                        executes()
                    }
                }
            }
        }
    }

    val DIFFICULTY = Command.create("difficulty") {
        "mode"(EnumArgumentType.of<Difficulty>()) {
            executes()
        }
    }

    val HELP = Command.create("help") {
        description = "コマンドの一覧またはヘルプを呼び出します。"

        aliases.add("?")

        executes()

        "page"(IntegerArgumentType.integer()) {
            executes()
        }

        "command_name"(StringArgumentType.word()) {
            executes {
                val arg = getArgument<String>("command_name")
                val command = getDispatcher().getCommand(arg.value)
                    ?: throw exception(
                        arg.sourceSpan,
                        "コマンド '${arg.value}' が見つかりません"
                    )

                command.getTreeUsage()
            }
        }
    }

    fun getAllVanillaCommands() = listOf(
        AIMASSIST,
        CAMERA,
        CAMERASHAKE,
        CLEAR,
        CLEARSPAWNPOINT,
        CLONE,
        CONNECT,
        CONTROLSCHEME,
        DAMAGE,
        DAYLOCK,
        DEOP,
        DIALOGUE,
        DIFFICULTY,
        HELP
    )
}
