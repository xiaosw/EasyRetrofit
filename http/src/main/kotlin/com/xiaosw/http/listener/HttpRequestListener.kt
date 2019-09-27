package com.xiaosw.http.listener

import java.lang.Exception

/**
 * @ClassName [HttpRequestListener]
 * @Description
 *
 * @Date 2019-04-26.
 * @Author xiaosw<xiaosw0802@163.com>.
 */
interface HttpRequestListener<F> {

    /**
     * 开始执行 http 请求
     */
    fun onHttpRequestStarted(showLoading: Boolean, flag: F? = null)

    /**
     * 连接异常
     */
    fun onConnectError(e: Exception, flag: F? = null)

    /**
     * http 请求执行完成
     */
    fun onHttpRequestComplete(dismissLoading: Boolean, flag: F? = null)

}