package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable
import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class BoolGameRule : StringRepresentable, CommandEnumerable {
    COMMANDBLOCKOUTPUT,
    DODAYLIGHTCYCLE,
    DOENTITYDROPS,
    DOFIRETICK,
    RECIPESUNLOCK,
    DOLIMITEDCRAFTING,
    DOMOBLOOT,
    DOMOBSPAWNING,
    DOTILEDROPS,
    DOWEATHERCYCLE,
    DROWNINGDAMAGE,
    FALLDAMAGE,
    FIREDAMAGE,
    KEEPINVENTORY,
    MOBGRIEFING,
    PVP,
    SHOWCOORDINATES,
    SHOWDAYSPLAYED,
    NATURALREGENERATION,
    TNTEXPLODES,
    SENDCOMMANDFEEDBACK,
    DOINSOMNIA,
    COMMANDBLOCKSENABLED,
    DOIMMEDIATERESPAWN,
    SHOWDEATHMESSAGES,
    SHOWTAGS,
    FREEZEDAMAGE,
    RESPAWNBLOCKSEXPLODE,
    SHOWBORDEREFFECT,
    SHOWRECIPEMESSAGES,
    PROJECTILESCANBREAKBLOCKS,
    TNTEXPLOSIONDROPDECAY;

    override fun getString(): String {
        return name.lowercase()
    }

    override fun getNames(): Set<String> {
        return setOf(getString())
    }
}
