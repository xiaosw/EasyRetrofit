package com.xiaosw.http

import com.xiaosw.api.extend.tryCatch
import com.xiaosw.api.logger.Logger
import com.xiaosw.http.compat.AppCompatDisposable
import com.xiaosw.http.listener.HttpRequestListener
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeoutException

/**
 * @ClassName [SimpleObserver]
 * @Description
 *
 * @Date 2019-04-26.
 * @Author xiaosw<xiaosw0802@163.com>.
 */
abstract class SimpleObserver<R : HttpResult, F> @JvmOverloads constructor(
    private val requestControl: HttpRequestControl? = null,
    private val showLoading: Boolean = true,
    private var isForeground: Boolean = true,
    private val requestListener: HttpRequestListener<F>? = null,
    val flag: F? = null
) : Observer<R>, AppCompatDisposable {

    private var mDisposable: Disposable? = null
    private var done = false

    override fun onSubscribe(disposable: Disposable) {
        mDisposable = disposable
        requestControl?.addDisposable(this)
        requestListener?.onHttpRequestStarted(showLoading, flag)
    }

    final override fun onNext(result: R) {
        tryCatch {
            if (result.code() === httpOkCode()) {
                onSuccessful(result)
            } else {
                onFailed(result.code(), result.message())
            }
        }
        done()
    }

    final override fun onError(e: Throwable) {
        Logger.e("onError: flag = $flag", throwable = e)
        tryCatch {
            val errorCode = if (e is TimeoutException
                || e is TimeoutException) {
                if (requestControl != null && requestControl.isStart) {
                    requestListener?.onConnectError(e, flag)
                }
                HttpCode.CODE_CONNECT_ERROR
            } else {
                HttpCode.CODE_REQUEST_ERROR
            }
            onFailed(errorCode, null)
        }
        done()
    }

    override fun onComplete() {
        done()
    }

    override fun isForeground() = isForeground

    override fun isDisposed() = mDisposable?.isDisposed ?: true

    override fun dispose() {
        Logger.d("dispose: mDisposable = $mDisposable")
        mDisposable?.tryCatch {
            if (!it.isDisposed) {
                it.dispose()
            }
        }
        mDisposable = null
    }

    private fun done() {
        if (!done) {
            synchronized(this) {
                if (!done) {
                    tryCatch {
                        requestListener?.onHttpRequestComplete(showLoading, flag)
                        requestControl?.cancel(this)
                    }
                    done = true
                }
            }
        }
    }

    @Throws(Exception::class)
    abstract fun onSuccessful(result: R)

    @JvmOverloads
    @Throws(Exception::class)
    open fun onFailed(code: Int, message: String? = null){
        requestControl?.maybeShowToast(message)
        Logger.e("onFailed: flag = $flag, code = $code, message = $message")
    }

    open fun httpOkCode() = 200

}