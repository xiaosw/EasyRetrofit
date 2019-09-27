package com.xiaosw.http.lifecycle


/**
 * @ClassName [LifecycleDelegate]
 * @Description
 *
 * @Date 2019-06-06.
 * @Author xiaosw<xiaosw0802@163.com>.
 */
internal interface LifecycleDelegate : Lifecycle {

    val mLifecycleListeners: MutableSet<LifecycleListener?>

    var isStarted : Boolean
    var isStop : Boolean
    var isDestroyed : Boolean

    override fun addListener(listener: LifecycleListener) {
        mLifecycleListeners.add(listener)
        when {
            isDestroyed -> listener.onDestroy()
            isStarted -> listener.onStart()
            isStop -> listener.onStop()
        }
    }

    override fun removeListener(listener: LifecycleListener) {
        mLifecycleListeners.remove(listener)
    }

    override fun clearListener() {
        mLifecycleListeners.clear()
    }

    fun notifyOnStart() {
//        LogUtil.e("notifyOnStart: ")
        isStarted = true
        isStop = false
        mLifecycleListeners.filter {
            it != null
        }.forEach {
            it?.onStart()
        }
    }

    fun notifyOnStop() {
//        LogUtil.e("notifyOnStop: ")
        isStarted = false
        isStop = true
        mLifecycleListeners.forEach {
            it?.onStop()
        }
    }

    fun notifyOnDestroy() {
//        LogUtil.e("notifyOnDestroy: ")
        isDestroyed = true
        mLifecycleListeners.forEach {
            it?.onDestroy()
        }
        mLifecycleListeners.clear()
    }

}