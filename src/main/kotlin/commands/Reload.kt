package top.cutestar.networkTools.commands

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import top.cutestar.networkTools.Config
import top.cutestar.networkTools.NetworkTools
import top.cutestar.networkTools.NetworkTools.reload

object Reload: CompositeCommand(
    owner = NetworkTools,
    primaryName = "ntools",
    description = "重载配置"
) {
    @SubCommand
    suspend fun CommandSender.reload() {
        Config.reload()
        sendMessage("已重载配置")
    }
}