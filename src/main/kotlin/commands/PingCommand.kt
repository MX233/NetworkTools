package top.cutestar.networkTools.commands

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import top.cutestar.networkTools.Config
import top.cutestar.networkTools.NetworkTools
import top.cutestar.networkTools.utils.Util
import top.cutestar.networkTools.utils.Util.withHelper
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.system.measureTimeMillis

object PingCommand:SimpleCommand(
    owner = NetworkTools,
    primaryName = "ping",
    description = "Ping连接测试"
) {
    @OptIn(ConsoleExperimentalApi::class)
    @Handler
    suspend fun CommandSender.onHandler(
        @Name("目标名称")s: String,
        @Name("超时时间(毫秒)")timeout: Int = Config.pingTimeout
    ) = withHelper{
        val splitStr = s.split(":")
        var isReachable: Boolean
        val ip: String
        var time: Long
        val info: String

        withContext(Dispatchers.IO) {
            when (splitStr.size) {
                1 -> {
                    info = "Ping"
                    val address = InetAddress.getByName(s)
                    ip = address.hostAddress
                    time = measureTimeMillis { isReachable = address.isReachable(timeout) }
                }
                2 -> {
                    info = "TcpPing"
                    ip = InetAddress.getByName(splitStr[0]).hostAddress
                    val port = splitStr[1].toIntOrNull() ?: throw IllegalArgumentException("端口参数类型错误")
                    val socket = Socket()
                    try {
                        time = measureTimeMillis { socket.connect(InetSocketAddress(ip, port), timeout) }
                        isReachable = true
                    } catch (e: Exception) {
                        isReachable = false
                        time = -1
                    }
                }
                else -> return@withContext
            }
                sendMessage(
                    """---${info}测试结果---
IP:$ip
地区:${Util.getLocation(ip)}
延迟:${if (isReachable) "${time}ms" else "超时"}
""".trimIndent()
                )
        }
    }
}