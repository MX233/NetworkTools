package top.cutestar.networkTools.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import net.mamoe.mirai.console.command.CommandSender
import top.cutestar.networkTools.Config
import java.util.*
import java.util.regex.Pattern
import javax.naming.NameNotFoundException
import javax.naming.directory.InitialDirContext

object Util {
    @Serializable
    data class LocationData(val data: LocationData2)
    @Serializable
    data class LocationData2(val location: String)

    private const val LOCAL_TEXT = "本地局域网"
    private const val IP46_RULE = "((((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?))|((([a-fA-F0-9]){1,4}:)+(:?(([a-fA-F0-9]){1,4}:?)+)?))"

    fun getLocation(address: String): String {
        when {
            "192.168." in address -> return LOCAL_TEXT
            "172." in address -> {
                val second = address.split(".")[1].toInt()
                if (second in 16..31) return LOCAL_TEXT
            }
        }
        /**
         * from "ip.zxinc.org"
         */
        val url = "https://ip.zxinc.org/api.php?type=json&ip=$address"
        val s = HttpUtil(url).getString(Config.webCharset)
        val json = Json { ignoreUnknownKeys = true }
        return json.decodeFromString<LocationData>(s).data.location
    }

    suspend fun CommandSender.withHelper(block: suspend () -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                block.invoke()
            } catch (e: Exception) {
                sendMessage("执行失败:${e.message ?: "未知"}")
                e.printStackTrace()
            }
        }
    }

    fun getIp(s: String):String? {
        val m = Pattern.compile(IP46_RULE).matcher(s)
        return if (m.find()) m.group() else null
    }

    fun dnsQuery(name: String,type: String,dns: String):MutableList<String> {
        val env = Hashtable<String,String>()
        val list = mutableListOf<String>()
        env["java.naming.factory.initial"] = "com.sun.jndi.dns.DnsContextFactory"
        env["java.naming.provider.url"] = "dns://$dns"

        val context = InitialDirContext(env).getAttributes(name, arrayOf(type)).get(type) ?: throw NameNotFoundException("查找失败")
            repeat(context.size()) {
                list.add(context.get(it).toString())
            }
        return list
    }

    fun matchText(s: String, regex: String, default: String = ""):String {
        val m = Pattern.compile(regex).matcher(s.lowercase())
        return if(m.find()) m.group(1) else default
    }
}