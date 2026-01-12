package me.minoneer.minecraft.message.bungee

import net.kyori.adventure.platform.bungeecord.BungeeAudiences
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Plugin


public object BungeeMessage {

    internal var bungeeAudiences: BungeeAudiences? = null
        private set

    public fun initialize(plugin: Plugin) {
        check(bungeeAudiences == null) { "BungeeMessage has already been initialized! You are either re-initializing, or a different plugin is using BungeeMessage without relocation." }
        bungeeAudiences = BungeeAudiences.create(plugin)
    }

    public fun shutdown() {
        bungeeAudiences?.close()
        bungeeAudiences = null
    }
}

public fun CommandSender.sendMessage(message: Component) {
    val audiences = BungeeMessage.bungeeAudiences
    check(audiences != null) { "BungeeMessage has not been initialized! Call BungeeMessage.initialize(plugin)" }
    audiences.sender(this).sendMessage(message)
}
