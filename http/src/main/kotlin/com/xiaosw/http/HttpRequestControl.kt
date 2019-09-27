package com.xiaosw.http

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.xiaosw.api.config.AppConfig
import com.xiaosw.api.logger.Logger
import com.xiaosw.api.util.ToastUtil
import com.xiaosw.api.wrapper.GsonWrapper
import com.xiaosw.http.compat.AppCompatDisposable
import com.xiaosw.http.internal.ActivityLifecycleManager
import com.xiaosw.http.lifecycle.LifecycleListener
import com.xiaosw.http.lifecycle.RequestLifecycleCompatFragment
import com.xiaosw.http.lifecycle.RequestLifecycleFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.RequestBody
import java.lang.ref.WeakReference
import java.util.*

/**
 * @ClassName [HttpRequestControl]
 * @Description
 *
 * @Date 2019-05-05.
 * @Author xiaosw<xiaosw0802@163.com>.
 */
 open class HttpRequestControl @JvmOverloads constructor(
    context: Context? = ActivityLifecycleManager.app
) : LifecycleListener {

    private val mDisposables by lazy {
        Collections.newSetFromMap(WeakHashMap<Disposable, Boolean>())
    }

    private var fragmentRef: WeakReference<RequestLifecycleFragment>? = null

    var isStart = false
        private set

    var isResume = false
        private set

    val isPause
        get() = !isResume

    val isStop
        get() = !isStart

    var isDestroy = false
        private set

    init {
        when {
            context is FragmentActivity -> bindLifecycle(context)
            context is Activity -> bindLifecycle(context)
            ActivityLifecycleManager.topActivity is FragmentActivity -> {
                bindLifecycle(ActivityLifecycleManager.topActivity as FragmentActivity)
            }
            ActivityLifecycleManager.topActivity is Activity -> {
                bindLifecycle(ActivityLifecycleManager.topActivity as Activity)
            }
        }
    }

    private inline fun bindLifecycle(fragmentActivity: FragmentActivity) {
        with(fragmentActivity.supportFragmentManager) {
            beginTransaction()
                .add(RequestLifecycleCompatFragment().also {
                    it.addListener(this@HttpRequestControl)
                }, RequestLifecycleCompatFragment::class.java.name)
                .commitAllowingStateLoss()
        }
    }

    private inline fun bindLifecycle(activity: Activity) {
        with(activity.fragmentManager) {
            beginTransaction()
                .add(RequestLifecycleFragment().also {
                    it.addListener(this@HttpRequestControl)
                }, RequestLifecycleFragment::class.java.name)
                .commitAllowingStateLoss()
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // http lifecycle api begin
    ///////////////////////////////////////////////////////////////////////////
    /**
     * @see LifecycleListener.onStart
     */
    override fun onStart() {
        isStart = true
    }

    /**
     * @see LifecycleListener.onResume
     */
    override fun onResume() {
        isResume = true
    }

    /**
     * @see LifecycleListener.onPause
     */
    override fun onPause() {
        isResume = false
    }

    /**
     * @see LifecycleListener.onStop
     */
    override fun onStop() {
        isStart = false
    }

    override fun onDestroy() {
        cancelAll()
    }
    ///////////////////////////////////////////////////////////////////////////
    // http lifecycle api begin
    ///////////////////////////////////////////////////////////////////////////

    fun buildJsonRequestBodyWithObject(
        obj: Any,
        mediaType: String? = AppConfig.REQUEST_MIMETYPE_JSON
    ) = mediaType?.let {
        RequestBody.create(okhttp3.MediaType.parse(mediaType), GsonWrapper.toJson(obj))
    } ?: RequestBody.create(null, GsonWrapper.toJson(obj))

    /**
     * execute task.
     */
    fun <R : HttpResult, F> execute(observable: Observable<R>, observer: SimpleObserver<R, F>) {
        mDisposables.add(observer)
        Logger.d("execute: size = ${mDisposables.size} observer = $observer")
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .unsubscribeOn(Schedulers.io())
            .subscribe(observer)
    }

    private fun releaseDisposable(disposable: Disposable?) {
        Logger.d("releaseDisposable: before size = ${mDisposables.size}, isDisposed = ${disposable?.isDisposed}")
        disposable?.dispose()
        mDisposables.remove(disposable)
        Logger.d("releaseDisposable: after size = ${mDisposables.size}")
    }

    fun addDisposable(disposable: Disposable?) {
        disposable?.run {
            mDisposables.add(disposable)
        }
    }

    fun cancel(disposable: Disposable?) = releaseDisposable(disposable)

    fun cancelAllForeground() = releaseAllTask(false)

    fun cancelAll() {
        releaseAllTask(true)
        mDisposables.clear()
    }

    private fun releaseAllTask(backAndForeground: Boolean) {
        Logger.i("cancelAll: Disposables size = ${mDisposables.size}, backAndForeground = $backAndForeground")
        mDisposables.filter {
            backAndForeground || (it is AppCompatDisposable && it.isForeground())
        }.iterator().forEach {
            releaseDisposable(it)
        }
    }

    /**
     * show toast. if init [HttpRequestControl] with [FragmentActivity] or [Fragment]
     */
    @Throws(Exception::class)
    open fun maybeShowToast(message: String?) : Boolean {
        if (!isStart) {
            Logger.w("maybeShowToast: current lifecycle is stop!")
            return false
        } else {
            message?.let {
                fragmentRef?.get()?.activity?.let {
                    return ToastUtil.showToast(it, message)
                }
            }
        }
        return false
    }

}