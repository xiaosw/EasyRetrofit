package com.xiaosw.http

import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

/**
 * @ClassName [HttpsVerify]
 * @Description
 *
 * @Date 2019-08-22.
 * @Author xiaosw<xiaosw0802@163.com>.
 */
interface HttpsVerify {

    fun getSslSocketFactory() : SSLSocketFactory

    fun getTrustManager() : X509TrustManager

}