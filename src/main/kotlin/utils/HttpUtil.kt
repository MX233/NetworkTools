package top.cutestar.networkTools.utils

import io.ktor.util.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import top.cutestar.networkTools.Config
import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Proxy
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

@OptIn(InternalAPI::class)
class HttpUtil(
    url: String,
    headers: MutableMap<String, String> = mutableMapOf(
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36",
        "Connection" to "keep-alive"
    )
) {
    var response: Response? = null

    init {
        val client = OkHttpClient.Builder()
            .connectTimeout(Config.webTimeout, TimeUnit.MILLISECONDS)
            .callTimeout(Config.webTimeout, TimeUnit.MILLISECONDS)
            .readTimeout(Config.webTimeout, TimeUnit.MILLISECONDS).also { client ->
                Config.run {
                    if (proxyEnabled) {
                        client.proxy(
                            Proxy(
                                Proxy.Type.valueOf(proxyType.toUpperCasePreservingASCIIRules()),
                                InetSocketAddress(proxyAddress, proxyPort)
                            )
                        )
                    }
                }
            }
            .build()
        val request = Request.Builder().run {
            url(url)
            headers.forEach { (k, v) ->
                addHeader(k, v)
            }
            build()
        }

        repeat(3) {
            try {
                response = client.newCall(request).execute()
            } catch (_: IOException) {
            }
        }
        response = (response ?: throw IOException("获取失败"))
    }

    fun getInputStream(): InputStream = ((response ?: throw IOException("获取失败")).body?.byteStream() ?: throw IOException("获取流失败"))

    fun getBytes(autoClose: Boolean = true): ByteArray{
        val bytes = getInputStream().readAllBytes()
        if(autoClose)close()
        return bytes
    }

    fun getString(charset: String = Config.webCharset,autoClose: Boolean = true) = String(getBytes(autoClose), Charset.forName(charset))

    fun close() {
        response?.close()
        getInputStream().close()
    }
}