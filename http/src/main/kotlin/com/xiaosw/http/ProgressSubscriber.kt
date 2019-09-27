package com.xiaosw.http

import com.xiaosw.http.listener.HttpRequestListener

/**
 * @ClassName [ProgressSubscriber]
 * @Description
 *
 * @Date 2019-08-23.
 * @Author xiaosw<xiaosw0802@163.com>.
 */
abstract class ProgressSubscriber<R : HttpResult, F>(
    requestControl: HttpRequestControl,
    showLoading: Boolean = true,
    isForeground: Boolean = true,
    requestListener: HttpRequestListener<F>? = null,
    flag: F? = null
) : SimpleObserver<R, F>(requestControl, showLoading, isForeground, requestListener, flag) {

    abstract fun onProgressUpdate(flag: F, progress: Int)

}