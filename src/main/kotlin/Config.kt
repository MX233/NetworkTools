package top.cutestar.networkTools

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object Config : AutoSavePluginConfig("config") {
    @ValueDescription("设置是否使用代理 在http(s)生效")
    var proxyEnabled by value(false)

    @ValueDescription("设置代理地址")
    var proxyAddress by value("")

    @ValueDescription("设置代理端口")
    var proxyPort by value(10808)

    @ValueDescription("设置代理类型 可选http,socks 大小写随意")
    var proxyType by value("http")

    @ValueDescription("设置控制台编码，如出现乱码请修改")
    var consoleCharset by value("GBK")

    @ValueDescription("设置web编码，如出现乱码请修改")
    var webCharset by value("UTF-8")

    @ValueDescription("Web连接超时时间(毫秒)")
    var webTimeout:Long by value((10_000).toLong())

    @ValueDescription("路由追踪等待时间(毫秒)")
    var tracertWaitTime by value(500)

    @ValueDescription("Ping命令测试超时时间(毫秒)")
    var pingTimeout by value(3_000)

    @ValueDescription("DNS命令查询默认服务器")
    var dnsAddress by value("8.8.8.8")

    @ValueDescription("DNS默认查询类型")
    var dnsTypes by value(arrayOf("A","AAAA","CNAME","TXT","NS","SOA","MX"))

    @ValueDescription("Web查询链接数量限制")
    var webLinkCount by value(50)

    @ValueDescription("端口扫描超时时间(毫秒)")
    var nmapTimeout by value(400)

    @ValueDescription("DoH服务器 cf DoH被封锁 需要代理")
    var dohAddress by value("https://cloudflare-dns.com/dns-query?name=${"$"}s&type=${"$"}type")
}