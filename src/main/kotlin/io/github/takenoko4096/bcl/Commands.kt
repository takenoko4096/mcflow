package io.github.takenoko4096.bcl

import io.github.takenoko4096.bcl.brigadier.Command
import io.github.takenoko4096.bcl.brigadier.argument.*
import io.github.takenoko4096.bcl.brigadier.value.block.FillMode
import io.github.takenoko4096.bcl.brigadier.value.misc.CameraPreset
import io.github.takenoko4096.bcl.brigadier.value.misc.CameraShakeType
import io.github.takenoko4096.bcl.brigadier.value.misc.CloneMode
import io.github.takenoko4096.bcl.brigadier.value.misc.ControlScheme
import io.github.takenoko4096.bcl.brigadier.value.entity.DamageCause
import io.github.takenoko4096.bcl.brigadier.value.misc.Difficulty
import io.github.takenoko4096.bcl.brigadier.value.misc.EasingType
import io.github.takenoko4096.bcl.brigadier.value.entity.Effect
import io.github.takenoko4096.bcl.brigadier.value.entity.EntityAnchor
import io.github.takenoko4096.bcl.brigadier.value.entity.EntityEvents
import io.github.takenoko4096.bcl.brigadier.value.entity.GameMode
import io.github.takenoko4096.bcl.brigadier.value.entity.InputPermission
import io.github.takenoko4096.bcl.brigadier.value.item.Enchant
import io.github.takenoko4096.bcl.brigadier.value.misc.BoolGameRule
import io.github.takenoko4096.bcl.brigadier.value.misc.FogDeleteMode
import io.github.takenoko4096.bcl.brigadier.value.misc.GameTipsStatus
import io.github.takenoko4096.bcl.brigadier.value.misc.HudElement
import io.github.takenoko4096.bcl.brigadier.value.misc.HudVisibility
import io.github.takenoko4096.bcl.brigadier.value.misc.InputPermissionState
import io.github.takenoko4096.bcl.brigadier.value.misc.IntGameRule
import io.github.takenoko4096.bcl.brigadier.value.misc.MaskMode
import io.github.takenoko4096.bcl.brigadier.value.misc.PlayerWaypointsValues

// /execute, function, gametest は一旦飛ばしてる
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

                    "seconds"(FloatArgumentType.positive()) {
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

    val EFFECT = Command.create("effect") {
        "targets"(EntitySelectorArgumentType()) {
            "effect_type"(EnumArgumentType.of<Effect>()) {
                executes()

                "seconds"(IntegerArgumentType.nonnegative()) {
                    executes()

                    "amplifier"(IntegerArgumentType.integer(0..255)) {
                        executes()

                        "hide_particles"(BooleanArgumentType()) {
                            executes()
                        }
                    }
                }

                "infinite" {
                    executes()

                    "amplifier"(IntegerArgumentType.integer(0..255)) {
                        executes()

                        "hide_particles"(BooleanArgumentType()) {
                            executes()
                        }
                    }
                }
            }

            "clear" {
                executes()

                "effect_type"(EnumArgumentType.of<Effect>()) {
                    executes()
                }
            }
        }
    }

    val ENCHANT = Command.create("enchant") {
        "targets"(EntitySelectorArgumentType()) {
            "enchant"(EnumArgumentType.of<Enchant>()) {
                executes()

                "level"(IntegerArgumentType.positive()) {
                    executes {
                        val enchant = getValue<Enchant>("enchant")
                        val levelArg = getArgument<Int>("level")

                        val rangeStr = if (enchant.levels.first == enchant.levels.last) enchant.levels.first
                            else "${enchant.levels.first} ～ ${enchant.levels.last}"

                        if (levelArg.value !in enchant.levels) {
                            throw exception(levelArg.sourceSpan, "エンチャント '${enchant.getString()}' はレベル ${levelArg.value} に対応していません: 有効なレベルは $rangeStr です")
                        }
                    }
                }
            }
        }
    }

    val EVENT = Command.create("event") {
        "entity" {
            "targets"(EntitySelectorArgumentType()) {
                "event_name"(EnumArgumentType.of<EntityEvents>()) {
                    executes()
                }
            }
        }
    }

    val FILL = Command.create("fill") {
        "from"(PositionArgumentType()) {
            "to"(PositionArgumentType()) {
                "tile_state"(BlockStateArgumentType.blockState()) {
                    executes()

                    "old_block_handling"(EnumArgumentType.of<FillMode>()) {
                        executes()
                    }

                    "replace" {
                        executes()

                        "replace_tile_state"(BlockStateArgumentType.blockState()) {
                            executes()
                        }
                    }
                }
            }
        }
    }

    val FOG = Command.create("fog") {
        "victims"(EntitySelectorArgumentType.players()) {
            "deletion_mode"(EnumArgumentType.of<FogDeleteMode>()) {
                "user_provider_id"(StringArgumentType.isolated()) {
                    executes()
                }
            }

            "push" {
                "fog_id"(IdentifierArgumentType.namespaceRequiredQuotable("namespace_required_identifier")) {
                    "user_provider_id"(StringArgumentType.isolated()) {
                        executes()
                    }
                }
            }
        }
    }

    val GAMEMODE = Command.create("gamemode") {
        "game_mode"(EnumArgumentType.of<GameMode>()) {
            executes()

            "players"(EntitySelectorArgumentType.players()) {
                executes()
            }
        }
    }

    val GAMERULE = Command.create("gamerule") {
        "int_game_rule"(EnumArgumentType.of<IntGameRule>()) {
            executes()

            "value"(IntegerArgumentType.integer()) {
                executes {
                    val gameRule = getValue<IntGameRule>("int_game_rule")
                    val valueArg = getArgument<Int>("value")

                    if (valueArg.value !in gameRule.range) {
                        throw exception(valueArg.sourceSpan, "ゲームルール '${gameRule.getString()}' は ${gameRule.range.first} ～ ${gameRule.range.last} までの値が有効ですが、 ${valueArg.value} が渡されました")
                    }
                }
            }
        }

        "bool_game_rule"(EnumArgumentType.of<BoolGameRule>()) {
            executes()

            "flag"(BooleanArgumentType()) {
                executes()
            }
        }

        "playerwaypoints" {
            "value"(EnumArgumentType.of<PlayerWaypointsValues>()) {
                executes()
            }
        }
    }

    val GAMETIPS = Command.create("gametips") {
        "status"(EnumArgumentType.of<GameTipsStatus>()) {
            executes()
        }
    }

    val GIVE = Command.create("give") {
        "players"(EntitySelectorArgumentType.players()) {
            "item_name"(ItemTypeArgumentType()) {
                executes()

                "amount"(IntegerArgumentType.integer(1..32767)) {
                    executes()

                    "data"(IntegerArgumentType.integer(0..32767)) {
                        executes()

                        "components"(ItemNbtComponentsArgumentType()) {
                            executes()
                        }
                    }
                }
            }
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

    val HUD = Command.create("hud") {
        "players"(EntitySelectorArgumentType.players()) {
            "visibility"(EnumArgumentType.of<HudVisibility>()) {
                executes()

                "hud_element"(EnumArgumentType.of<HudElement>()) {
                    executes()
                }
            }
        }
    }

    val INPUTPERMISSION = Command.create("inputpermission") {
        "query" {
            "players"(EntitySelectorArgumentType.players()) {
                "input_permission"(EnumArgumentType.of<InputPermission>()) {
                    executes()

                    "permission_state"(EnumArgumentType.of<InputPermissionState>()) {
                        executes()
                    }
                }
            }
        }

        "set" {
            "players"(EntitySelectorArgumentType.players()) {
                "input_permission"(EnumArgumentType.of<InputPermission>()) {
                    "permission_state"(EnumArgumentType.of<InputPermissionState>()) {
                        executes()
                    }
                }
            }
        }
    }

    val EXECUTE = Command.create("execute") {
        "as" {
            "targets"(EntitySelectorArgumentType()) {
                redirect("execute")
            }
        }

        "at" {
            "targets"(EntitySelectorArgumentType()) {
                redirect("execute")
            }
        }

        "positioned" {
            "position"(PositionArgumentType()) {
                redirect("execute")
            }

            "as" {
                "targets"(EntitySelectorArgumentType()) {
                    redirect("execute")
                }
            }
        }

        "rotated" {
            "rotation"(RotationArgumentType()) {
                redirect("execute")
            }

            "as" {
                "targets"(EntitySelectorArgumentType()) {
                    redirect("execute")
                }
            }
        }

        "facing" {
            "entity" {
                "targets"(EntitySelectorArgumentType()) {
                    "entity_anchor"(EnumArgumentType.of<EntityAnchor>()) {
                        redirect("execute")
                    }
                }
            }
        }

        "anchored" {
            "entity_anchor"(EnumArgumentType.of<EntityAnchor>()) {
                redirect("execute")
            }
        }

        "align" {
            "axes"(AxesArgumentType()) {
                redirect("execute")
            }
        }

        "if" {
            "entity" {
                "targets"(EntitySelectorArgumentType()) {
                    executes()

                    redirect("execute")
                }
            }

            "block" {
                "position"(PositionArgumentType()) {
                    "block_state"(BlockStateArgumentType.blockState()) {
                        executes()

                        redirect("execute")
                    }
                }
            }

        }

        "unless" {
            "entity" {
                "targets"(EntitySelectorArgumentType()) {
                    executes()

                    redirect("execute")
                }
            }

            "block" {
                "position"(PositionArgumentType()) {
                    "block_state"(BlockStateArgumentType.blockState()) {
                        executes()

                        redirect("execute")
                    }
                }
            }
        }

        "run" {
            "pass" {
                executes()
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
        EFFECT,
        ENCHANT,
        EVENT,
        FILL,
        FOG,
        GAMEMODE,
        GAMERULE,
        GAMETIPS,
        GIVE,
        HELP,
        HUD,
        INPUTPERMISSION,
        // Command.create("pass") { executes() },

        EXECUTE
    )
}
