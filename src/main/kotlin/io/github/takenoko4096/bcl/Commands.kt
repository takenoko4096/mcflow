package io.github.takenoko4096.bcl

import io.github.takenoko4096.bcl.brigadier.Command
import io.github.takenoko4096.bcl.brigadier.argument.*
import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier
import io.github.takenoko4096.bcl.brigadier.value.block.FillMode
import io.github.takenoko4096.bcl.brigadier.value.block.ScanMode
import io.github.takenoko4096.bcl.brigadier.value.entity.*
import io.github.takenoko4096.bcl.brigadier.value.item.Enchant
import io.github.takenoko4096.bcl.brigadier.value.item.ItemReplaceMode
import io.github.takenoko4096.bcl.brigadier.value.location.Biome
import io.github.takenoko4096.bcl.brigadier.value.misc.*

object Commands {
    val AIMASSIST = Command.builder("aimassist") {
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

    val CAMERA = Command.builder("camera") {
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
                                            "yaw"(CoordinateComponentArgumentType()) {
                                                "pitch"(CoordinateComponentArgumentType()) {
                                                    executes()
                                                }
                                            }
                                        }
                                    }
                                }

                                "rot" {
                                    "yaw"(CoordinateComponentArgumentType()) {
                                        "pitch"(CoordinateComponentArgumentType()) {
                                            executes()

                                            "entity_offset" {
                                                executes()
                                            }

                                            "view_offset" {
                                                executes()
                                            }
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

    val CAMERASHAKE = Command.builder("camerashake") {
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

    val CLEAR = Command.builder("clear") {
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

    val CLEARSPAWNPOINT = Command.builder("clearspawnpoint") {
        executes()

        "players"(EntitySelectorArgumentType.players()) {
            executes()
        }
    }

    val CLONE = Command.builder("clone") {
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

    val CONNECT = Command.builder("connect") {
        "server_uri"(StringArgumentType.greedy()) {
            executes()
        }
    }

    val CONTROLSCHEME = Command.builder("controlscheme") {
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

    val DAMAGE = Command.builder("damage") {
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

    val DAYLOCK = Command.builder("daylock") {
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

    val DEOP = Command.builder("deop") {
        "player"(EntitySelectorArgumentType.players()) {
            executes()
        }
    }

    val DIALOGUE = Command.builder("dialogue") {
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

    val DIFFICULTY = Command.builder("difficulty") {
        "mode"(EnumArgumentType.of<Difficulty>()) {
            executes()
        }
    }

    val EFFECT = Command.builder("effect") {
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

    val ENCHANT = Command.builder("enchant") {
        "targets"(EntitySelectorArgumentType()) {
            "enchant"(EnumArgumentType.of<Enchant>()) {
                executes()

                "level"(IntegerArgumentType.positive()) {
                    executes {
                        val enchant = getValue<Enchant>("enchant")
                        val levelArgument = getArgument<Int>("level")

                        val range = if (enchant.levels.first == enchant.levels.last) {
                            enchant.levels.first.toString()
                        }
                        else {
                            "${enchant.levels.first} ～ ${enchant.levels.last}"
                        }

                        val level = levelArgument.value

                        if (level !in enchant.levels) {
                            addDiagnostic(
                                levelArgument.sourceSpan,
                                "エンチャント '${enchant.getString()}' はレベル $level に対応していません: 有効なレベルは $range です"
                            )
                        }
                    }
                }
            }
        }
    }

    val EVENT = Command.builder("event") {
        "entity" {
            "targets"(EntitySelectorArgumentType()) {
                "event_name"(EnumArgumentType.of<EntityEvent>()) {
                    executes()
                }
            }
        }
    }

    val EXECUTE = Command.builder("execute") {
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
            "yaw"(CoordinateComponentArgumentType()) {
                "pitch"(CoordinateComponentArgumentType()) {
                    redirect("execute")
                }
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

            "blocks" {
                "begin"(PositionArgumentType()) {
                    "end"(PositionArgumentType()) {
                        "destination"(PositionArgumentType()) {
                            "scan_mode"(EnumArgumentType.of<ScanMode>()) {
                                executes()

                                redirect("execute")
                            }
                        }
                    }
                }
            }

            "score" {
                "target1"(EntitySelectorArgumentType.single()) {
                    "objective1"(StringArgumentType.isolated()) {
                        "comparator"(ScoreComparatorArgumentType()) {
                            "target2"(EntitySelectorArgumentType.single()) {
                                "objective2"(StringArgumentType.isolated()) {
                                    executes()

                                    redirect("execute")
                                }
                            }
                        }

                        "matches" {
                            "range"(InvertibleIntegerRangeArgumentType()) {
                                executes()

                                redirect("execute")
                            }
                        }
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

            "blocks" {
                "begin"(PositionArgumentType()) {
                    "end"(PositionArgumentType()) {
                        "destination"(PositionArgumentType()) {
                            "scan_mode"(EnumArgumentType.of<ScanMode>()) {
                                executes()

                                redirect("execute")
                            }
                        }
                    }
                }
            }

            "score" {
                "target1"(EntitySelectorArgumentType.single()) {
                    "objective1"(StringArgumentType.isolated()) {
                        "comparator"(ScoreComparatorArgumentType()) {
                            "target2"(EntitySelectorArgumentType.single()) {
                                "objective2"(StringArgumentType.isolated()) {
                                    executes()

                                    redirect("execute")
                                }
                            }
                        }

                        "matches" {
                            "range"(InvertibleIntegerRangeArgumentType()) {
                                executes()

                                redirect("execute")
                            }
                        }
                    }
                }
            }
        }

        "run" {
            redirect("root")
        }
    }

    val FILL = Command.builder("fill") {
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

    val FOG = Command.builder("fog") {
        "victims"(EntitySelectorArgumentType.players()) {
            "deletion_mode"(EnumArgumentType.of<FogDeleteMode>()) {
                "user_provider_id"(StringArgumentType.isolated()) {
                    executes()
                }
            }

            "push" {
                "fog_id"(IdentifierArgumentType.namespaced("namespace_required_identifier")) {
                    "user_provider_id"(StringArgumentType.isolated()) {
                        executes()
                    }
                }
            }
        }
    }

    val FUNCTION = Command.builder("function") {
        "name"(FilePathArgumentType(strict = true)) {
            executes()
        }
    }

    val GAMEMODE = Command.builder("gamemode") {
        "game_mode"(EnumArgumentType.of<GameMode>()) {
            executes()

            "players"(EntitySelectorArgumentType.players()) {
                executes()
            }
        }
    }

    val GAMERULE = Command.builder("gamerule") {
        "int_game_rule"(EnumArgumentType.of<IntGameRule>()) {
            executes()

            "value"(IntegerArgumentType.integer()) {
                executes {
                    val gameRule = getValue<IntGameRule>("int_game_rule")
                    val valueArg = getArgument<Int>("value")

                    if (valueArg.value !in gameRule.range) {
                        addDiagnostic(valueArg.sourceSpan, "ゲームルール '${gameRule.getString()}' は ${gameRule.range.first} ～ ${gameRule.range.last} までの値が有効ですが、 ${valueArg.value} が渡されました")
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

    val GAMETEST = Command.builder("gametest") {
        "clearall" {
            executes()
        }

        "create" {
            "test_name"(StringArgumentType.isolated()) {
                executes()

                "width"(IntegerArgumentType.integer()) {
                    executes()

                    "height"(IntegerArgumentType.integer()) {
                        executes()

                        "depth"(IntegerArgumentType.integer()) {
                            executes()
                        }
                    }
                }
            }
        }

        "pos" {
            executes()
        }

        "run" {
            "test_name"(StringArgumentType.isolated()) {
                "stop_on_failure"(BooleanArgumentType()) {
                    "repeat_count"(IntegerArgumentType.positive()) {
                        executes()

                        "rotation_steps"(IntegerArgumentType.integer()) {
                            executes()
                        }
                    }
                }

                "rotation_steps"(IntegerArgumentType.integer()) {
                    executes()
                }

                executes()
            }
        }

        inline("runset", "runsetuntilfail") {
            executes()

            "tag"(StringArgumentType.isolated()) {
                executes()

                "rotation_steps"(IntegerArgumentType.integer()) {
                    executes()
                }
            }
        }

        inline("runthese", "runthis") {
            executes()
        }

        "stopall" {
            executes()
        }
    }

    val GAMETIPS = Command.builder("gametips") {
        "status"(EnumArgumentType.of<GameTipsStatus>()) {
            executes()
        }
    }

    val GIVE = Command.builder("give") {
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

    val HELP = Command.builder("help") {
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
                    ?: run {
                        addDiagnostic(
                            arg.sourceSpan,
                            "コマンド '${arg.value}' が見つかりません"
                        )
                        return@executes null
                    }

                command.getTreeUsage()
            }
        }
    }

    val HUD = Command.builder("hud") {
        "players"(EntitySelectorArgumentType.players()) {
            "visibility"(EnumArgumentType.of<HudVisibility>()) {
                executes()

                "hud_element"(EnumArgumentType.of<HudElement>()) {
                    executes()
                }
            }
        }
    }

    val INPUTPERMISSION = Command.builder("inputpermission") {
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

    val KICK = Command.builder("kick") {
        "target"(EntitySelectorArgumentType.players()) {
            executes()

            "reason"(StringArgumentType.greedy()) {
                executes()
            }
        }
    }

    val KILL = Command.builder("kill") {
        "targets"(EntitySelectorArgumentType()) {
            executes()
        }
    }

    val LIST = Command.builder("list") {
        executes()
    }

    val LOCATE = Command.builder("locate") {
        "biome" {
            "id"(EnumArgumentType.of<Biome>()) {
                executes()
            }
        }

        "structure" {
            "id"(StringArgumentType.isolated()) {
                executes()

                "use_new_chunks_only"(BooleanArgumentType()) {
                    executes()
                }
            }
        }
    }

    val LOOT = Command.builder("loot") {
        "give" {
            "players"(EntitySelectorArgumentType.players()) {
                "kill" {
                    "target"(EntitySelectorArgumentType()) {
                        executes()
                    }
                }

                "loot" {
                    "loot_table"(StringArgumentType.isolated() /*TODO EnumArgumentType.of<LootTable>()*/) {
                        "tool"(ItemTypeArgumentType()) {
                            executes()
                        }

                        "mainhand" {
                            executes()
                        }

                        "offhand" {
                            executes()
                        }
                    }
                }

                "mine" {
                    "position"(PositionArgumentType()) {
                        executes()

                        "tool"(ItemTypeArgumentType()) {
                            executes()
                        }

                        "mainhand" {
                            executes()
                        }

                        "offhand" {
                            executes()
                        }
                    }
                }
            }
        }
    }

    val ME = Command.builder("me") {
        "message"(StringArgumentType.greedy()) {
            executes()
        }
    }

    val MOBEVENT = Command.builder("mobevent") {
        "event"(EnumArgumentType.of<MobEvent>()) {
            executes()

            "value"(BooleanArgumentType()) {
                executes()
            }
        }
    }

    val MUSIC = Command.builder("music") {
        "play" {
            "track_name"(StringArgumentType.isolated()) {
                executes()

                "volume"(FloatArgumentType.nonnegative()) {
                    executes()

                    "fade_seconds"(FloatArgumentType.nonnegative()) {
                        executes()

                        "repeat_mode"(EnumArgumentType.of<MusicRepeatMode>()) {
                            executes()
                        }
                    }
                }
            }
        }

        "queue" {
            "track_name"(StringArgumentType.isolated()) {
                executes()

                "volume"(FloatArgumentType.nonnegative()) {
                    executes()

                    "fade_seconds"(FloatArgumentType.nonnegative()) {
                        executes()

                        "repeat_mode"(EnumArgumentType.of<MusicRepeatMode>()) {
                            executes()
                        }
                    }
                }
            }
        }

        "stop" {
            executes()

            "fade_seconds"(FloatArgumentType.nonnegative()) {
                executes()
            }
        }

        "volume" {
            "volume"(FloatArgumentType.nonnegative()) {
                executes()
            }
        }
    }

    val OP = Command.builder("op") {
        "players"(EntitySelectorArgumentType.players()) {
            executes()
        }
    }

    val PACKSTACK = Command.builder("packstack") {
        "stack_type"(EnumArgumentType.of<StackType>()) {
            executes()

            "verbose" {
                executes()

                "exclude-vanilla" {
                    executes()
                }
            }
        }
    }

    val PARTICLE = Command.builder("particle") {
        "effect"(StringArgumentType.isolated()) {
            executes()

            "position"(PositionArgumentType()) {
                executes()
            }
        }
    }

    val PERMISSION = Command.builder("permission") {
        "set" {
            "players"(EntitySelectorArgumentType.players()) {
                executes()

                "permission_level"(EnumArgumentType.of<PermissionLevel>()) {
                    executes()
                }
            }
        }
    }

    val PLACE = Command.builder("place") {
        "feature" {
            "feature"(EnumArgumentType.of<Feature>()) {
                executes()

                "position"(PositionArgumentType()) {
                    executes()
                }
            }
        }

        "featurerule" {
            "feature_rule"(EnumArgumentType.of<FeatureRule>()) {
                executes()

                "position"(PositionArgumentType()) {
                    executes()
                }
            }
        }

        "jigsaw" {
            "pool"(FilePathArgumentType(strict = false)) {
                "target"(StringArgumentType.isolated()) {
                    "max_depth"(IntegerArgumentType.integer(1..20)) {
                        executes()

                        "position"(PositionArgumentType()) {
                            executes()

                            "keep_jigsaws"(BooleanArgumentType()) {
                                executes()

                                "include_entities"(BooleanArgumentType()) {
                                    executes()

                                    "liquid_settings"(BooleanArgumentType()) {
                                        executes()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        "structure" {
            "jigsaw_structure"(EnumArgumentType.of<JigsawStructure>()) {
                executes()

                "position"(PositionArgumentType()) {
                    executes()

                    "ignore_start_height"(BooleanArgumentType()) {
                        executes()

                        "keep_jigsaws"(BooleanArgumentType()) {
                            executes()

                            "include_entities"(BooleanArgumentType()) {
                                executes()

                                "liquid_settings"(BooleanArgumentType()) {
                                    executes()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val PLAYANIMATION = Command.builder("playanimation") {
        "targets"(EntitySelectorArgumentType()) {
            "animation"(StringArgumentType.isolated()) {
                executes()

                "next_state"(StringArgumentType.isolated()) {
                    executes()

                    "blend_out_time"(FloatArgumentType.float()) {
                        executes()

                        "stop_expression"(StringArgumentType.isolated()) {
                            executes()

                            "controller"(StringArgumentType.isolated()) {
                                executes()
                            }
                        }
                    }
                }
            }
        }
    }

    val PLAYSOUND = Command.builder("playsound") {
        "sound"(StringArgumentType.isolated()) {
            executes()

            "players"(EntitySelectorArgumentType.players()) {
                executes()

                "position"(PositionArgumentType()) {
                    executes()

                    "volume"(FloatArgumentType.float()) {
                        executes()

                        "pitch"(FloatArgumentType.float()) {
                            executes()

                            "min_volume"(FloatArgumentType.float()) {
                                executes()
                            }
                        }
                    }
                }
            }
        }
    }

    val RECIPE = Command.builder("recipe") {
        "give" {
            "players"(EntitySelectorArgumentType.players()) {
                "recipe"(RECIPE_ARGUMENT_TYPE) {
                    executes()
                }
            }
        }

        "take" {
            "players"(EntitySelectorArgumentType.players()) {
                "recipe"(RECIPE_ARGUMENT_TYPE) {
                    executes()
                }
            }
        }
    }

    val RELOAD = Command.builder("reload") {
        executes()

        "all" {
            executes()
        }
    }

    val REPLACEITEM = Command.builder("replaceitem") {
        "block" {
            "position"(PositionArgumentType()) {
                "slot.container" {
                    "slot_id"(IntegerArgumentType.integer()) {
                        "old_item_handling"(EnumArgumentType.of<ItemReplaceMode>()) {
                            "item_name"(ItemTypeArgumentType()) {
                                executes()

                                "amount"(IntegerArgumentType.positive()) {
                                    executes()

                                    "data"(IntegerArgumentType.integer()) {
                                        executes()

                                        "components"(ItemNbtComponentsArgumentType()) {
                                            executes()
                                        }
                                    }
                                }
                            }
                        }

                        "item_name"(ItemTypeArgumentType()) {
                            executes()

                            "amount"(IntegerArgumentType.positive()) {
                                executes()

                                "data"(IntegerArgumentType.integer()) {
                                    executes()

                                    "components"(ItemNbtComponentsArgumentType()) {
                                        executes()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        "entity" {
            "targets"(EntitySelectorArgumentType()) {
                "slot_type"(EnumArgumentType.of<EntityContainerSlotType>()) {
                    "slot_id"(IntegerArgumentType.integer()) {
                        "old_item_handling"(EnumArgumentType.of<ItemReplaceMode>()) {
                            "item_name"(ItemTypeArgumentType()) {
                                executes()

                                "amount"(IntegerArgumentType.positive()) {
                                    executes()

                                    "data"(IntegerArgumentType.integer()) {
                                        executes()

                                        "components"(ItemNbtComponentsArgumentType()) {
                                            executes()
                                        }
                                    }
                                }
                            }
                        }

                        "item_name"(ItemTypeArgumentType()) {
                            executes()

                            "amount"(IntegerArgumentType.positive()) {
                                executes()

                                "data"(IntegerArgumentType.integer()) {
                                    executes()

                                    "components"(ItemNbtComponentsArgumentType()) {
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

    val RIDE = Command.builder("ride") {
        "riders"(EntitySelectorArgumentType()) {
            "start_riding" {
                "vehicle"(EntitySelectorArgumentType()) {
                    executes()

                    "teleport_rule"(EnumArgumentType.of<TeleportRule>()) {
                        executes()

                        "fill_type"(EnumArgumentType.of<VehicleFillType>()) {
                            executes()
                        }
                    }
                }
            }

            "stop_riding" {
                executes()
            }

            "summon_ride" {
                "entity_type"(EntityTypeArgumentType.summonableOnly()) {
                    executes()

                    "ride_rule"(EnumArgumentType.of<RideRule>()) {
                        executes()

                        "spawn_event"(EnumArgumentType.of<EntityEvent>()) {
                            executes()

                            "name_tag"(StringArgumentType.isolated()) {
                                executes()
                            }
                        }
                    }
                }
            }
        }

        "rides"(EntitySelectorArgumentType()) {
            "evict_riders" {
                executes()
            }

            "summon_rider" {
                "entity_type"(EntityTypeArgumentType.summonableOnly()) {
                    executes()

                    "spawn_event"(EnumArgumentType.of<EntityEvent>()) {
                        executes()

                        "name_tag"(StringArgumentType.isolated()) {
                            executes()
                        }
                    }
                }
            }
        }
    }

    val SAY = Command.builder("say") {
        "message"(StringArgumentType.greedy()) {
            executes()
        }
    }

    val SCHEDULE = Command.builder("schedule") {
        "clear" {
            "function"(FilePathArgumentType(strict = true)) {
                executes()
            }
        }

        "delay" {
            "add" {
                "function"(FilePathArgumentType(strict = true)) {
                    "time"(TimeDurationArgumentType()) {
                        executes()

                        "replace" {
                            executes()
                        }

                        "append" {
                            executes()
                        }
                    }
                }
            }

            "clear" {
                "function"(FilePathArgumentType(strict = true)) {
                    executes()
                }
            }
        }

        "on_area_loaded" {
            "add" {
                "from"(PositionArgumentType()) {
                    "to"(PositionArgumentType()) {
                        "function"(FilePathArgumentType(strict = true)) {
                            executes()
                        }
                    }
                }

                "circle" {
                    "center"(PositionArgumentType()) {
                        "radius"(IntegerArgumentType.nonnegative()) {
                            "function"(FilePathArgumentType(strict = true)) {
                                executes()
                            }
                        }
                    }
                }

                "tickingarea" {
                    "name"(StringArgumentType.isolated()) {
                        "function"(FilePathArgumentType(strict = true)) {
                            executes()
                        }
                    }
                }
            }

            "clear" {
                "function" {
                    "function"(FilePathArgumentType(strict = true)) {
                        executes()
                    }
                }

                "tickingarea" {
                    "name"(StringArgumentType.isolated()) {
                        executes()

                        "function"(FilePathArgumentType(strict = true)) {
                            executes()
                        }
                    }
                }
            }
        }
    }

    val SCOREBOARD = Command.builder("scoreboard") {
        "objectives" {
            "add" {
                "objective"(StringArgumentType.isolated()) {
                    "dummy" {
                        executes()

                        "display_name"(StringArgumentType.isolated()) {
                            executes()
                        }
                    }
                }
            }

            "list" {
                executes()
            }

            "remove" {
                "objective"(StringArgumentType.isolated()) {
                    executes()
                }
            }

            "setdisplay" {
                inline("list", "sidebar") {
                    executes()

                    "objective"(StringArgumentType.isolated()) {
                        executes()

                        inline("ascending", "descending") {
                            executes()
                        }
                    }
                }
            }
        }

        "players" {
            inline("add", "remove", "set") {
                "targets"(EntitySelectorArgumentType()) {
                    "objective"(StringArgumentType.isolated()) {
                        "count"(IntegerArgumentType.integer()) {
                            executes()
                        }
                    }
                }
            }

            "list" {
                executes()

                "player_name"(EntitySelectorArgumentType()) {
                    executes()
                }
            }

            "operation" {
                "target"(EntitySelectorArgumentType()) {
                    "target_objective"(StringArgumentType.isolated()) {
                        "operation"(ScoreOperatorArgumentType()) {
                            "source"(EntitySelectorArgumentType()) {
                                "source_objective"(StringArgumentType.isolated()) {
                                    executes {
                                        val target = getArgument<EntitySelector>("target")
                                        val source = getArgument<EntitySelector>("source")

                                        if (target.value.isWildcard && source.value.isWildcard) {
                                            addDiagnostic(source.sourceSpan, "scoreboard operation ではワイルドカードは target または source のどちらか片方にのみ使用できます")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "random" {
                "targets"(EntitySelectorArgumentType()) {
                    "objective"(StringArgumentType.isolated()) {
                        "min"(IntegerArgumentType.integer()) {
                            "max"(IntegerArgumentType.integer()) {
                                executes {
                                    val min = getArgument<Int>("min")
                                    val max = getArgument<Int>("max")

                                    if (min.value > max.value) {
                                        addDiagnostic(min.sourceSpan.start..max.sourceSpan.end, "最小値が最大値を上回っています (${min.value} > ${max.value})")
                                    }
                                    else if (min.value == max.value) {
                                        addDiagnostic(min.sourceSpan.start..max.sourceSpan.end, "最小値と最大値は 1 以上離れている必要があります")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "reset" {
                "targets"(EntitySelectorArgumentType()) {
                    executes()

                    "objective"(StringArgumentType.isolated()) {
                        executes()
                    }
                }
            }

            "test" {
                "targets"(EntitySelectorArgumentType()) {
                    "objective"(StringArgumentType.isolated()) {
                        "min"(WildcardIntegerArgumentType()) {
                            executes()

                            "max"(WildcardIntegerArgumentType()) {
                                executes()
                            }
                        }
                    }
                }
            }
        }
    }

    val SCRIPT = Command.builder("script") {
        "debugger" {
            "close" {
                executes()
            }

            "connect" {
                executes()

                "host"(StringArgumentType.isolated()) {
                    executes()

                    "port"(IntegerArgumentType.integer()) {
                        executes()
                    }
                }
            }

            "listen" {
                "port"(IntegerArgumentType.integer()) {
                    executes()
                }
            }
        }

        "diagnostics" {
            inline("startcapture", "stopcapture") {
                executes()
            }
        }

        "profiler" {
            inline("start", "stop") {
                executes()
            }
        }
    }

    val SCRIPTEVENT = Command.builder("scriptevent") {
        "id"(IdentifierArgumentType.namespaced("script_event_id")) {
            executes {
                val argument = getArgument<NamespacedIdentifier>("script_event_id")
                if (argument.value.namespace == NamespacedIdentifier.DEFAULT_NAMESPACE) {
                    addDiagnostic(argument.sourceSpan, "id の名前空間は '${NamespacedIdentifier.DEFAULT_NAMESPACE}' 以外でなければなりません")
                }
            }

            "message"(StringArgumentType.greedy()) {
                executes {
                    val argument = getArgument<NamespacedIdentifier>("script_event_id")
                    if (argument.value.namespace == NamespacedIdentifier.DEFAULT_NAMESPACE) {
                        addDiagnostic(argument.sourceSpan, "id の名前空間は '${NamespacedIdentifier.DEFAULT_NAMESPACE}' 以外でなければなりません")
                    }
                }
            }
        }
    }

    val SETBLOCK = Command.builder("setblock") {
        "position"(PositionArgumentType()) {
            "block_state"(BlockStateArgumentType.blockState()) {
                executes()

                inline("replace", "destroy", "keep") {
                    executes()
                }
            }
        }
    }

    val SETMAXPLAYERS = Command.builder("setmaxplayers") {
        "max_players"(IntegerArgumentType.integer()) {
            executes()
        }
    }

    val SETWORLDSPAWN = Command.builder("setworldspawn") {
        executes()

        "position"(PositionArgumentType()) {
            executes()
        }
    }

    val SPAWNPOINT = Command.builder("spawnpoint") {
        executes()

        "players"(EntitySelectorArgumentType.players()) {
            executes()

            "position"(PositionArgumentType()) {
                executes()
            }
        }
    }

    val SPREADPLAYERS = Command.builder("spreadplayers") {
        "x"(CoordinateComponentArgumentType()) {
            "z"(CoordinateComponentArgumentType()) {
                "spread_distance"(FloatArgumentType.nonnegative()) {
                    "max_range"(FloatArgumentType.float(1f..Float.MAX_VALUE)) {
                        "victims"(EntitySelectorArgumentType()) {
                            executes()

                            "max_height"(FloatArgumentType.float()) {
                                executes()
                            }
                        }
                    }
                }
            }
        }
    }

    val STOPSOUND = Command.builder("stopsound") {
        "players"(EntitySelectorArgumentType.players()) {
            executes()

            "sound"(StringArgumentType.isolated()) {
                executes()
            }
        }
    }

    val STRUCTURE = Command.builder("structure") {
        "delete" {
            "name"(IdentifierArgumentType.unstrict("structure_id", "mystructure")) {
                executes()
            }
        }

        "load" {
            "name"(IdentifierArgumentType.unstrict("structure_id", "mystructure")) {
                "to"(PositionArgumentType()) {
                    executes()

                    "rotation"(EnumArgumentType.of<StructureRotation>()) {
                        executes()

                        "mirror"(EnumArgumentType.of<StructureMirror>()) {
                            executes()

                            "animation_mode"(EnumArgumentType.of<StructureAnimationMode>()) {
                                executes()

                                "animation_seconds"(FloatArgumentType.nonnegative()) {
                                    executes()

                                    "include_entities"(BooleanArgumentType()) {
                                        executes()

                                        "include_blocks"(BooleanArgumentType()) {
                                            executes()

                                            "waterlogged"(BooleanArgumentType()) {
                                                executes()

                                                "integrity"(FloatArgumentType.float(0f..100f)) {
                                                    executes()

                                                    "seed"(StringArgumentType.isolated()) {
                                                        executes()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            "include_entities"(BooleanArgumentType()) {
                                executes()

                                "include_blocks"(BooleanArgumentType()) {
                                    executes()

                                    "waterlogged"(BooleanArgumentType()) {
                                        executes()

                                        "integrity"(FloatArgumentType.float(0f..100f)) {
                                            executes()

                                            "seed"(StringArgumentType.isolated()) {
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

        "save" {
            "name"(IdentifierArgumentType.unstrict("structure_id", "mystructure")) {
                "from"(PositionArgumentType()) {
                    "to"(PositionArgumentType()) {
                        executes()

                        "include_entities"(BooleanArgumentType()) {
                            executes()

                            "save_mode"(EnumArgumentType.of<StructureSaveMode>()) {
                                executes()

                                "include_blocks"(BooleanArgumentType()) {
                                    executes()
                                }
                            }
                        }

                        "save_mode"(EnumArgumentType.of<StructureSaveMode>()) {
                            executes()
                        }
                    }
                }
            }
        }
    }

    val SUMMON = Command.builder("summon") {
        "entity_type"(EntityTypeArgumentType.summonableOnly()) {
            executes()

            "name_tag"(StringArgumentType.isolated()) {
                executes()

                "position"(PositionArgumentType()) {
                    executes()
                }
            }

            "position"(PositionArgumentType()) {
                executes()

                "yaw"(CoordinateComponentArgumentType()) {
                    executes()

                    "pitch"(CoordinateComponentArgumentType()) {
                        executes()

                        "spawn_event"(EnumArgumentType.of<EntityEvent>()) { // TODO ここは無効な文字列を入力しても問題ない
                            executes()

                            "name_tag"(StringArgumentType.isolated()) {
                                executes()
                            }
                        }
                    }
                }

                "facing" {
                    "look_at_entity"(EntitySelectorArgumentType()) {
                        executes()

                        "spawn_event"(EnumArgumentType.of<EntityEvent>()) { // TODO ここは無効な文字列を入力しても問題ない
                            executes()

                            "name_tag"(StringArgumentType.isolated()) {
                                executes()
                            }
                        }
                    }

                    "look_at_position"(PositionArgumentType()) {
                        executes()

                        "spawn_event"(EnumArgumentType.of<EntityEvent>()) { // TODO ここは無効な文字列を入力しても問題ない
                            executes()

                            "name_tag"(StringArgumentType.isolated()) {
                                executes()
                            }
                        }
                    }
                }
            }
        }
    }

    val TAG = Command.builder("tag") {
        "targets"(EntitySelectorArgumentType()) {
            inline("add", "remove") {
                "name"(StringArgumentType.isolated()) {
                    executes()
                }
            }

            "list" {
                executes()
            }
        }
    }

    val TELEPORT = Command.builder("teleport") {
        aliases.add("tp")

        "destination_entity"(EntitySelectorArgumentType()) {
            executes()
        }

        "destination"(PositionArgumentType()) {
            executes()

            "check_for_blocks"(BooleanArgumentType()) {
                executes()

                "force_provided_position_on_dimension_change"(BooleanArgumentType()) {
                    executes()
                }
            }

            "yaw"(CoordinateComponentArgumentType()) {
                executes()

                "pitch"(CoordinateComponentArgumentType()) {
                    executes()

                    "check_for_blocks"(BooleanArgumentType()) {
                        executes()

                        "force_provided_position_on_dimension_change"(BooleanArgumentType()) {
                            executes()
                        }
                    }
                }
            }

            "facing" {
                "look_at_entity"(EntitySelectorArgumentType()) {
                    executes()

                    "check_for_blocks"(BooleanArgumentType()) {
                        executes()

                        "force_provided_position_on_dimension_change"(BooleanArgumentType()) {
                            executes()
                        }
                    }
                }

                "look_at_position"(PositionArgumentType()) {
                    executes()

                    "check_for_blocks"(BooleanArgumentType()) {
                        executes()

                        "force_provided_position_on_dimension_change"(BooleanArgumentType()) {
                            executes()
                        }
                    }
                }
            }
        }

        "victims"(EntitySelectorArgumentType()) {
            "destination_entity"(EntitySelectorArgumentType()) {
                executes()
            }

            "destination"(PositionArgumentType()) {
                executes()

                "check_for_blocks"(BooleanArgumentType()) {
                    executes()

                    "force_provided_position_on_dimension_change"(BooleanArgumentType()) {
                        executes()
                    }
                }

                "yaw"(CoordinateComponentArgumentType()) {
                    executes()

                    "pitch"(CoordinateComponentArgumentType()) {
                        executes()

                        "check_for_blocks"(BooleanArgumentType()) {
                            executes()

                            "force_provided_position_on_dimension_change"(BooleanArgumentType()) {
                                executes()
                            }
                        }
                    }
                }

                "facing" {
                    "look_at_entity"(EntitySelectorArgumentType()) {
                        executes()

                        "check_for_blocks"(BooleanArgumentType()) {
                            executes()

                            "force_provided_position_on_dimension_change"(BooleanArgumentType()) {
                                executes()
                            }
                        }
                    }

                    "look_at_position"(PositionArgumentType()) {
                        executes()

                        "check_for_blocks"(BooleanArgumentType()) {
                            executes()

                            "force_provided_position_on_dimension_change"(BooleanArgumentType()) {
                                executes()
                            }
                        }
                    }
                }
            }
        }
    }

    val TELL = Command.builder("tell") {
        aliases.add("msg")
        aliases.add("w")

        "target"(EntitySelectorArgumentType.players()) {
            "message"(StringArgumentType.greedy()) {
                executes()
            }
        }
    }

    val TELLRAW = Command.builder("tellraw") {
        "targets"(EntitySelectorArgumentType.players()) {
            "message"(RawTextComponentArgumentType()) {
                executes()
            }
        }
    }

    val TESTFOR = Command.builder("testfor") {
        "victim"(EntitySelectorArgumentType()) {
            executes()
        }
    }

    val TESTFORBLOCK = Command.builder("testforblock") {
        "position"(PositionArgumentType()) {
            "block_state"(BlockStateArgumentType.blockState()) {
                executes()
            }
        }
    }

    val TESTFORBLOCKS = Command.builder("testforblocks") {
        "begin"(PositionArgumentType()) {
            "end"(PositionArgumentType()) {
                "destination"(PositionArgumentType()) {
                    "scan_mode"(EnumArgumentType.of<ScanMode>()) {
                        executes()
                    }

                    executes()
                }
            }
        }
    }

    val TICKINGAREA = Command.builder("tickingarea") {
        "add" {
            "from"(PositionArgumentType()) {
                "to"(PositionArgumentType()) {
                    executes()

                    "name"(StringArgumentType.isolated()) {
                        executes()

                        "preload"(BooleanArgumentType()) {
                            executes()
                        }
                    }
                }
            }
        }

        "list" {
            "all-dimensions" {
                executes()
            }

            executes()
        }

        "preload" {
            "name"(StringArgumentType.isolated()) {
                executes()

                "preload"(BooleanArgumentType()) {
                    executes()
                }
            }

            "position"(PositionArgumentType()) {
                executes()

                "preload"(BooleanArgumentType()) {
                    executes()
                }

            }
        }

        "remove" {
            "name"(StringArgumentType.isolated()) {
                executes()
            }

            "position"(PositionArgumentType()) {
                executes()
            }
        }

        "remove_all" {
            executes()
        }
    }

    val TIME = Command.builder("time") {
        "add" {
            "amount"(IntegerArgumentType.integer()) {
                executes()
            }
        }

        "query" {
            "time_query"(EnumArgumentType.of<TimeQuery>()) {
                executes()
            }
        }

        "set" {
            "amount"(IntegerArgumentType.integer()) {
                executes()
            }

            "time_spec"(EnumArgumentType.of<TimeSpec>()) {
                executes()
            }
        }
    }

    val TITLE = Command.builder("title") {
        "players"(EntitySelectorArgumentType.players()) {
            "title_location"(EnumArgumentType.of<TitleLocation>()) {
                "title_text"(StringArgumentType.greedy()) {
                    executes()
                }
            }

            "clear" {
                executes()
            }

            "reset" {
                executes()
            }

            "times" {
                "fade_in"(IntegerArgumentType.nonnegative()) {
                    "stay"(IntegerArgumentType.nonnegative()) {
                        "fade_out"(IntegerArgumentType.nonnegative()) {
                            executes()
                        }
                    }
                }
            }
        }
    }

    val TITLERAW = Command.builder("titleraw") {
        "players"(EntitySelectorArgumentType.players()) {
            "title_location"(EnumArgumentType.of<TitleLocation>()) {
                "raw_json_title_text"(RawTextComponentArgumentType()) {
                    executes()
                }
            }

            "clear" {
                executes()
            }

            "reset" {
                executes()
            }

            "times" {
                "fade_in"(IntegerArgumentType.nonnegative()) {
                    "stay"(IntegerArgumentType.nonnegative()) {
                        "fade_out"(IntegerArgumentType.nonnegative()) {
                            executes()
                        }
                    }
                }
            }
        }
    }

    val TOGGLEDOWNFALL = Command.builder("toggledownfall") {
        executes()
    }

    val WEATHER = Command.builder("weather") {
        "weather_type"(EnumArgumentType.of<WeatherType>()) {
            executes()

            "duration"(IntegerArgumentType.nonnegative()) {
                executes()
            }
        }
    }

    val XP = Command.builder("xp") {
        "amount"(ExperienceAmountArgumentType()) {
            executes()

            "players"(EntitySelectorArgumentType.players()) {
                executes()
            }
        }
    }

    // TODO: requires { ... }, CommandPermissionLevel, Education, BDS
    fun getAllVanillaCommandBuilders() = listOf(
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
        EXECUTE,
        FILL,
        FOG,
        FUNCTION,
        GAMEMODE,
        GAMERULE,
        GAMETEST,
        GAMETIPS,
        GIVE,
        HELP,
        HUD,
        INPUTPERMISSION,
        KICK,
        KILL,
        LIST,
        LOCATE,
        LOOT,
        ME,
        MUSIC,
        OP,
        PACKSTACK,
        PARTICLE,
        PERMISSION,
        PLACE,
        PLAYANIMATION,
        PLAYSOUND,
        RECIPE,
        RELOAD,
        REPLACEITEM,
        RIDE,
        SAY,
        SCHEDULE,
        SCOREBOARD,
        SCRIPT,
        SCRIPTEVENT,
        SETBLOCK,
        SETMAXPLAYERS,
        SETWORLDSPAWN,
        SPAWNPOINT,
        SPREADPLAYERS,
        STOPSOUND,
        STRUCTURE,
        SUMMON,
        TELEPORT,
        TELL,
        TELLRAW,
        TESTFOR,
        TESTFORBLOCK,
        TESTFORBLOCKS,
        TICKINGAREA,
        TITLE,
        TITLERAW,
        TOGGLEDOWNFALL,
        WEATHER,
        XP
    )
}
