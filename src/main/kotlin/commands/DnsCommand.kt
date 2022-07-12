package top.cutestar.networkTools.commands

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import top.cutestar.networkTools.Config
import top.cutestar.networkTools.NetworkTools
import top.cutestar.networkTools.utils.Util
import top.cutestar.networkTools.utils.Util.withHelper
import java.io.IOException
import javax.naming.NameNotFoundException

object DnsCommand : SimpleCommand(
    owner = NetworkTools,
    primaryName = "dns",
    description = "dns查询"
) {
    @OptIn(ConsoleExperimentalApi::class)
    @Handler
    suspend fun CommandSender.onHandler(
        @Name("记录名称") s: String,
        @Name("记录类型") types: Array<String> = Config.dnsTypes,
        @Name("自定义DNS") dns: String = Config.dnsAddress
    ) = withHelper {
        sendMessage(executeDnsQuery(s, types, dns))
    }

    private suspend fun CommandSender.executeDnsQuery(s: String, types: Array<String>, dns: String):Message {
        val msg = ForwardMessageBuilder(subject ?: throw IOException("subject is null"))
        var count = 0
        types.forEach { type ->
            try {
                val list = Util.dnsQuery(s, type, dns)
                val sb = StringBuilder(type)
                list.forEach {
                    sb.append("\n").append(it)
                    count++
                }
                msg.add(
                    senderId = bot?.id ?: return@forEach,
                    senderName = "${list.size}条记录",
                    message = PlainText(sb)
                )
            } catch (_: NameNotFoundException) {}
        }
        sendMessage("DNS查询完成,共找到${count}条记录\nDNS:$dns")
        return msg.build()
    }
}