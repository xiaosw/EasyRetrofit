package com.xiaosw.http.compat

import io.reactivex.disposables.Disposable

/**
 * @ClassName [AppCompatDisposable]
 * @Description
 *
 * @Date 2019-05-06.
 * @Author xiaosw<xiaosw0802@163.com>.
 */
interface AppCompatDisposable : Disposable {

    /**
     * 是否为前台任务，true:退到后台则自动取消，false：可以在后台继续执行
     */
    fun isForeground() : Boolean

}