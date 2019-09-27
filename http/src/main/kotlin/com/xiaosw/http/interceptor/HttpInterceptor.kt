package com.xiaosw.http.interceptor

import com.xiaosw.api.config.AppConfig
import com.xiaosw.api.extend.tryCatch
import com.xiaosw.api.logger.Logger
import com.xiaosw.api.util.StringUtil
import com.xiaosw.api.wrapper.GsonWrapper
import com.xiaosw.http.HttpResult
import com.xiaosw.http.interceptor.cipher.Base64Utils
import okhttp3.*
import okio.Buffer
import org.json.JSONObject
import java.lang.NullPointerException
import java.util.concurrent.TimeUnit

/**
 * @ClassName [HttpInterceptor]
 * @Description
 *
 * @Date 2019-05-05.
 * @Author xiaosw<xiaosw0802@163.com>.
 */
abstract class HttpInterceptor : Interceptor {

    /**
     * 读取请求 body 信息
     */
    private inline fun readRequestBody(request: Request) : String {
        var result = DEF_REQUEST_PARAMS
        request.body()?.let {
            with(Buffer()) {
                it.tryCatch {
                    it.writeTo(this)
                    readString(it.contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8)
                }?.let {
                    result = it
                }
            }
        }
        return result
    }

    /**
     * output log to console.
     */
    private fun printLog(log: String) {
        if (!Logger.isEnable()) {
            return
        }
        when (logLevel()) {
            Logger.LogLevel.VERBOSE -> Logger.v(log)

            Logger.LogLevel.DEBUG -> Logger.d(log)

            Logger.LogLevel.INFO -> Logger.i(log)

            Logger.LogLevel.WARN -> Logger.w(log)

            Logger.LogLevel.ERROR -> Logger.e(log)
        }
    }

    private fun requestLogToLogcat(chain: Interceptor.Chain,
                                   originalParams: String,
                                   encryptParams: String?) {
        if (Logger.isEnable()) {
            chain.tryCatch { it ->
                var protocol = it.connection()?.toString() ?: Protocol.HTTP_1_1.toString()
                val request = chain.request()
                with(StringBuffer("\n╔").append(GsonWrapper.LINE_BORDER)
                    .append("\n║request: url = ").append(request.url())
                    .append(", method = ").append(request.method())
                    .append(", protocol = ").append(protocol)) {

                    // append headers
                    request.headers()?.let {
                        append("\n║")
                            .append(GsonWrapper.LINE_BORDER)
                            .append("\n║request headers:")
                        request.body()?.apply {
                            contentType().let {
                                append("\n║Content-Type: ").append(it.toString())
                            }
                            contentLength().let {
                                append("\n║Content-Length: ").append(it)
                            }
                        }

                        val headersSize = it.size()
                        var i = 0
                        while (headersSize > i) {
                            val name = it.name(i)
                            if (!"Content-Type".equals(name, ignoreCase = true)
                                && !"Content-Length".equals(name, ignoreCase = true)) {
                                append("\n║").append(name).append(":").append(it.value(i))
                            }
                            i++
                        }
                    }

                    // body
                    append("\n║").append(GsonWrapper.LINE_BORDER)
                        .append("\n║request original body: \n║").append(originalParams)
                    if (StringUtil.isNotEmpty(encryptParams)) {
                        append("\n║\n║request encrypt body: \n║").append(encryptParams)
                    }
                    if (isFormatJson() && DEF_REQUEST_PARAMS !== originalParams && GsonWrapper.isJson(originalParams)) {
                        append("\n║")
                            .append("\n║format request body to json ---> ")
                            .append("\n║")
                            .append(GsonWrapper.formatJsonStr(originalParams))
                    }
                    append("\n╚").append(GsonWrapper.LINE_BORDER)
                    printLog(TAG + " ---> request: " + toString() + "\n\n\n ")
                }
            }
        }
    }

    private inline fun readResponseContent(response: Response?) : String {
        var result = DEF_RESPONSE_CONTENT
        response?.body()?.apply {
            with(source()) {
                request(Long.MAX_VALUE) // Buffer the entire body.
                buffer().readString(contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8)?.also {
                    result = it
                }
            }

        }
        return result
    }

    private fun responseLogToConsole(startNs: Long,
                                     response: Response?,
                                     encryptResultContent: String?,
                                     decryptResultContent: String) {
        if (Logger.isEnable()) {
            response?.tryCatch { it->
                with(StringBuffer("\n╔").append(GsonWrapper.LINE_BORDER)) {
                    var bodySize = "unknown-length"
                    it.body()?.contentLength()?.also {
                        if (it != -1L) {
                            bodySize = "$it-byte"
                        }
                    }
                    append("\n║response: execute duration = ")
                        .append(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs))
                        .append(", code = ").append(response.code())
                        .append(", message = ").append(response.message())
                        .append(", bodySize = ").append(bodySize)
                        .append(", url = ").append(response.request().url())

                    // append headers
                    it.headers().let { it ->
                        append("\n║")
                            .append(GsonWrapper.LINE_BORDER)
                            .append("\n║response headers:")
                        response.body()?.apply {
                            contentType().let {
                                append("\n║Content-Type: ").append(it.toString())
                            }
                            contentLength().let {
                                append("\n║Content-Length: ").append(it)
                            }
                        }

                        val headersSize = it.size()
                        var i = 0
                        while (headersSize > i) {
                            val name = it.name(i)
                            if (!"Content-Type".equals(name, ignoreCase = true)
                                && !"Content-Length".equals(name, ignoreCase = true)) {
                                append("\n║").append(name).append(":").append(it.value(i))
                            }
                            i++
                        }
                    }

                    // body
                    if (StringUtil.isNotEmpty(encryptResultContent)) {
                        append("\n║").append(GsonWrapper.LINE_BORDER)
                            .append("\n║response encrypt body : \n║").append(encryptResultContent)
                            .append("\n║\n║response decrypt body: \n║").append(decryptResultContent)
                    } else {
                        append("\n║\n║response body: \n║").append(decryptResultContent)
                    }
                    if (isFormatJson()
                        && DEF_REQUEST_PARAMS !== decryptResultContent
                        && GsonWrapper.isJson(decryptResultContent)) {
                        append("\n║")
                            .append("\n║format request body to json ---> ")
                            .append("\n║")
                            .append(GsonWrapper.formatJsonStr(decryptResultContent))
                    }
                    append("\n╚").append(GsonWrapper.LINE_BORDER)
                    printLog(TAG + " ---> response: " + toString() + "\n\n\n ")
                }
            }
        }
    }

    private inline fun replaceResponseSource(body: ResponseBody, decryptResponseData: String?) {
        var newSource = decryptResponseData ?: ""
        with(body) {
            source().buffer().request(Long.MAX_VALUE)
            source().buffer().write(newSource.toByteArray(
                contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
            ))
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val startNs = System.nanoTime()
        var request = chain.request()
        with(request) {
            val originalParams = readRequestBody(this)
            val requestUrl = adapterUrl(url().toString())
            val isEncrypt = whetherNeedEncrypt(requestUrl)
            var encryptParams: String? = null
            if (isEncrypt && "POST".equals(method(), true)) {
                encryptParams = Base64Utils.encode(encrypt(originalParams))
                request = newBuilder().method(
                    method(),
                    RequestBody.create(MediaType.parse("application/json; charset=utf-8"), encryptParams)
                ).build()
            }

            // print request information
            requestLogToLogcat(chain, originalParams, encryptParams)

            // request
            val originalResponse = chain.tryCatch {
                it.proceed(request)
            }

            // handle response
            val originalResponseData = readResponseContent(originalResponse)
            var decryptResponseData = originalResponseData
            if (whetherNeedDecrypt(requestUrl)) {
                decryptResponseData = String(decrypt(Base64Utils.decode(originalResponseData)))

                originalResponse?.body()?.also {
                    replaceResponseSource(it, decryptResponseData)
                }
            }
            convertJsonToHttpResult(decryptResponseData)?.let {
                if (it.code() == AppConfig.SESSION_INVALID) { // 登录失效
                    Logger.i("login is invalid. execute auto login!",
                        TAG
                    )
                    autoLogin()?.let { sessionId ->
                        Logger.i("auto login success!",
                            TAG
                        )
                        // new request
                        val newParams = JSONObject(originalParams).put("sessionId", sessionId).toString()
                        var newEncryptParams: String? = null
                        with(request.newBuilder()) {
                            tryCatch {
                                newEncryptParams = Base64Utils.encode(encrypt(newParams))
                                method(request.method(), RequestBody.create(MediaType.parse("application/json; charset=utf-8"), newEncryptParams))
                            }
                            val newRequest = build()
                            // print request information
                            requestLogToLogcat(chain, readRequestBody(newRequest), newEncryptParams)

                            // retry the request
                            originalResponse?.body()?.close()
                            val newResponse = chain.proceed(newRequest)
                            val newResponseData = readResponseContent(newResponse)
                            var newDecryptResponseData = readResponseContent(newResponse)
                            if (whetherNeedDecrypt(requestUrl)) {
                                newDecryptResponseData = String(decrypt(Base64Utils.decode(newResponseData)))
                                newResponse?.body()?.also { newResponseBody ->
                                    replaceResponseSource(newResponseBody, newDecryptResponseData)
                                }
                            }
                            responseLogToConsole(startNs, newResponse, newResponseData, newDecryptResponseData)
                            return newResponse
                        }
                    }
                }
            }

            responseLogToConsole(startNs, originalResponse, originalResponseData, decryptResponseData)
            if (null == originalResponse) {
                throw NullPointerException("server not response exception!")
            }
            return originalResponse!!
        }
    }

    open fun adapterUrl(url: String) = url

    /**
     * 是否需要加密
     */
    abstract fun whetherNeedEncrypt(url: String) : Boolean

    /**
     * 是否需要解密
     */
    abstract fun whetherNeedDecrypt(url: String) : Boolean

    /**
     * 加密
     */
    abstract fun encrypt(original: String?) : ByteArray

    /**
     * 解密
     */
    abstract fun decrypt(encrypt: ByteArray?) : ByteArray

    /**
     * 自动登录
     */
    abstract fun autoLogin() : String?

    /**
     * json 转换
     */
    abstract fun convertJsonToHttpResult(json: String?) : HttpResult?

    /**
     * whether format log
     */
    abstract fun isFormatJson() : Boolean

    /**
     * @see Logger.LogLevel
     */
    abstract fun logLevel() : Logger.LogLevel

    companion object {
        // default request params.
        private const val DEF_REQUEST_PARAMS = "{}"
        // default response body source.
        private const val DEF_RESPONSE_CONTENT = "{}"

        private const val TAG = "HttpInterceptor"
    }
}