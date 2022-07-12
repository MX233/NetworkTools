package top.cutestar.networkTools.commands

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.PlainText
import top.cutestar.networkTools.Config
import top.cutestar.networkTools.NetworkTools
import top.cutestar.networkTools.utils.HttpUtil
import top.cutestar.networkTools.utils.Util
import top.cutestar.networkTools.utils.Util.withHelper
import java.util.regex.Pattern
import kotlin.system.measureTimeMillis

object WebCommand : SimpleCommand(
    owner = NetworkTools,
    primaryName = "web",
    description = "网页测试"
) {
    @OptIn(ConsoleExperimentalApi::class)
    @Handler
    suspend fun CommandSender.onHandler(
        @Name("URL链接") url: String,
        @Name("编码") charset: String = Config.webCharset
    ) = withHelper {
        executeWeb(if(":/" in url)url else "https://$url", charset)
    }

    private suspend fun CommandSender.executeWeb(url: String, charset: String) {
        val web: HttpUtil
        val time = measureTimeMillis { web = HttpUtil(url) }
        val response = web.response!!
        val server = response.headers.filter { it.first.equals("server", true) }.run {
            if (isEmpty()) "" else this[0].second
        }
        val s = web.getString(charset,false).replace("\n","")

        sendMessage(
            """---测试完成---
标题:${getTitle(s)}
状态码:${response.code}
http延时:${time}ms
服务端:$server
""".trimIndent()
        )

        val messages = mutableListOf(
            "描述:\n${getDescription(s)}",
            "关键词:\n${getKeywords(s)}",
            "链接"
        )

        var count = 0
        var charCount = 0

        val timpMsg = ForwardMessageBuilder(subject!!)
        messages.forEach {
            timpMsg.add(
                senderId = bot!!.id,
                senderName = bot!!.nick,
                message = PlainText(it)
            )
            count++
            charCount += it.length
        }

        getKeyLink(s, url).forEach {
            val text = "${it.key}\n${it.value}"
            timpMsg.run {
                if (size >= 80 || charCount >= 4000) {
                    sendMessage(build())
                    clear()
                    charCount = 0
                }
                if (count >= Config.webLinkCount) {
                    add(bot!!.id,bot!!.nick,PlainText("链接数量已达限制,你可以在配置文件中设置更多"))
                    sendMessage(build())
                    return
                }
                timpMsg.add(bot!!.id, bot!!.nick, PlainText(text))
                count++
                charCount += text.length
            }
        }
        web.close()

        if(timpMsg.size > 0)sendMessage(timpMsg.build())
    }

    fun getTitle(s: String) = Util.matchText(s, "<title>((.*?){1,50})</title>")

    fun getDescription(s: String): String {
        return Util.matchText(s, "<meta.*?name=\"description\".*?content=\"(.*?)\"")
            .replace(" ", "\n")
            .replace(",", "\n")
    }

    fun getKeywords(s: String) =
        Util.matchText(s, "<meta.*?name=\"keywords\".*?content=\"(.*?)\"")
        .replace(" ", "\n")
        .replace(",", "\n")

    fun getKeyLink(s: String, url: String): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()
        val m = Pattern.compile("<a.*?href=\"(.*?)\".*?>(.*?)</a").matcher(s)
        while (m.find()) {
            val g1 = m.group(1)
            val httpRule = ".*https?://.*"
            val url = if(Pattern.matches(httpRule,g1)) g1 else url + g1
            m.group(2).run {
                if (
                    length <= 100 &&
                    isNotEmpty() &&
                    url.isNotEmpty() &&
                    !Pattern.matches(httpRule,this)
                ) map[this] = url
            }
        }
        return map
    }
}