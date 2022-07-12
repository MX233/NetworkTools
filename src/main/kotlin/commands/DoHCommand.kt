package top.cutestar.networkTools.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import top.cutestar.networkTools.Config
import top.cutestar.networkTools.NetworkTools
import top.cutestar.networkTools.utils.HttpUtil
import top.cutestar.networkTools.utils.Util.withHelper

object DoHCommand: SimpleCommand(
    owner = NetworkTools,
    primaryName = "doh",
    description = "HTTPS加密DNS查询"
) {
    @Serializable
    data class DoHObject(@SerialName("Status")val status: Int,@SerialName("Answer") val answer: List<Answer>)

    @Serializable
    data class Answer(val data: String)

    @OptIn(ConsoleExperimentalApi::class)
    @Handler
    suspend fun CommandSender.onHandler(
        @Name("域名")s: String,
        @Name("类型")type: String = "A"
    ) = withHelper{
        val json = Json{ignoreUnknownKeys = true}
        val list = mutableListOf<String>()
        val headers = mutableMapOf("accept" to "application/dns-json")
        val url = Config.dohAddress.replace("${"$"}s",s).replace("${"$"}type",type)
        json.decodeFromString<DoHObject>(HttpUtil(url,headers).getString()).run {
            if(status != 0) throw IllegalStateException("查询失败")
            answer.forEach { list.add(it.data) }
        }

        sendMessage(
"""---DNS over HTTPS---
$s
$type
${list.joinToString("\n")}
""".trimIndent()
        )
    }
}