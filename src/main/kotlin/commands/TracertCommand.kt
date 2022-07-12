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
import java.nio.charset.Charset
import java.util.regex.Pattern
import kotlin.system.measureTimeMillis

object TracertCommand: SimpleCommand(
    owner = NetworkTools,
    primaryName = "tracert",
    "tr",
    description = "路由追踪"
) {
    @OptIn(ConsoleExperimentalApi::class)
    @Handler
    suspend fun CommandSender.onHandler(@Name("目标名称")s: String) = withHelper{
        val address = StringBuilder()
        val m = Pattern.compile("[a-zA-Z0-9.:]").matcher(s)
        while (m.find()) {
            address.append(m.group())
        }

        sendMessage("正在追踪 请稍等")
        sendMessage(executeTracert(address.toString(),Config.tracertWaitTime))
    }

    private fun CommandSender.executeTracert(address: String, waitTime: Int): Message {
        val fmsg = ForwardMessageBuilder(subject ?: throw IOException("subject is null"))
            val process = Runtime.getRuntime().exec("tracert -w $waitTime -d $address")
        val time = measureTimeMillis {process.waitFor()}
        val buffer = process.inputStream.bufferedReader(Charset.forName(Config.consoleCharset))
        var s = buffer.readLine()
        while (s != null) {
            val msg = StringBuilder(s)
            Util.getIp(s).let { ip ->
                if (ip != null) msg.append("\n").append(Util.getLocation(ip))
            }
            fmsg.add(
                senderId = bot?.id ?: continue,
                senderName = bot?.nick ?: continue,
                message = PlainText(msg)
            )
            s = buffer.readLine()
        }
        fmsg.add(
            senderId = bot!!.id,
            senderName = bot!!.nick,
            PlainText("用时:${time}ms")
        )
        if (fmsg.size == 0) throw IOException("追踪失败")
        return fmsg.build()
    }
}