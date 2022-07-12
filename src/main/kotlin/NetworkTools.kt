package top.cutestar.networkTools

import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.plugin.name
import net.mamoe.mirai.console.plugin.version
import top.cutestar.networkTools.commands.*

object NetworkTools : KotlinPlugin(
    JvmPluginDescription(
        id = "top.cutestar.networkTools",
        version = "1.0.0",
        name = "NetworkTools",
    ){
        author("CuteStar")
    }
) {
    override fun onEnable() {
        Config.reload()
        registerCommands()
        logger.info("$name V$version loaded")
    }

    private fun registerCommands() = CommandManager.run {
        registerCommand(TracertCommand)
        registerCommand(PingCommand)
        registerCommand(DnsCommand)
        registerCommand(WebCommand)
        registerCommand(NmapCommand)
        registerCommand(DoHCommand)
        registerCommand(Reload)
    }
}