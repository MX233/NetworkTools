package top.cutestar.networkTools.commands

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.PlainText
import top.cutestar.networkTools.Config
import top.cutestar.networkTools.NetworkTools
import top.cutestar.networkTools.utils.Util.withHelper
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.system.measureTimeMillis

@OptIn(ConsoleExperimentalApi::class)
object NmapCommand : SimpleCommand(
    owner = NetworkTools,
    primaryName = "nmap",
    description = "端口扫描",
) {
    @Handler
    suspend fun CommandSender.onHandler(
        @Name("目标名称") address: String,
    ) {
        executeNmap(address, (1..1000).toMutableSet())
    }

    @Handler
    suspend fun CommandSender.onHandler(
        @Name("目标名称") address: String,
        @Name("端口") s: String
    ) {
        s.toIntOrNull().let {
            val posts: MutableSet<Number> = when {
                it != null -> mutableSetOf(it)
                s.equals("all", true) -> (1..65535).toMutableSet()
                else -> {
                    val set = mutableSetOf<Number>()
                    s.splitToSequence(",").forEach { s ->
                        s.toIntOrNull().run {
                            if (this != null && this in 1..65535) set.add(this)
                        }
                    }
                    set
                }
            }

            executeNmap(address, posts)
        }
    }

    @Handler
    suspend fun CommandSender.onHandler(
        @Name("目标名称") address: String,
        @Name("起始端口") startPort: Int,
        @Name("结束端口") endPort: Int,
    ) = withHelper{
        when{
            startPort > endPort -> throw IllegalArgumentException("起始端口大于结束端口")
            startPort !in 1..65535 || endPort !in 1..65535 -> throw IllegalArgumentException("端口号错误\n1-65535")
        }
        executeNmap(address, (startPort..endPort).toMutableSet())
    }

    suspend fun CommandSender.executeNmap(address: String, ports: MutableSet<Number>) = withHelper {
        coroutineScope {
            sendMessage("正在扫描${ports.size}个端口，这需要一段时间")
            launch {
                val activePorts = mutableListOf<Int>()
                val time = measureTimeMillis {
                    launch {
                        ports.forEach {
                            launch(Dispatchers.IO) {
                                Socket().run {
                                    try {
                                        connect(InetSocketAddress(address, it.toInt()), Config.nmapTimeout)
                                        activePorts.add(it.toInt())
                                    } catch (_: Exception) {
                                    } finally {
                                        close()
                                    }
                                }
                            }
                        }
                    }.join()
                }
                activePorts.sort()

                val fmsg = ForwardMessageBuilder(subject!!)
                val sb = StringBuilder(
                    """---端口扫描---
用时:${(time).toDouble() / 1000}秒
端口数量:${activePorts.size}
开放的TCP端口:
${activePorts.joinToString("\n")}
""".trimIndent()
                )
                bot!!.run {
                    fmsg.add(this.id, this.nick, PlainText(sb))
                }

                sendMessage(fmsg.build())
            }
        }
    }
}