package com.xiaosw.http.lifecycle

import androidx.fragment.app.Fragment
import java.util.*


/**
 * @ClassName [RequestLifecycleCompatFragment]
 * @Description
 *
 * @Date 2019-05-05.
 * @Author xiaosw<xiaosw0802@163.com>.
 */
internal class RequestLifecycleCompatFragment : Fragment(),
    LifecycleDelegate {

    override val mLifecycleListeners: MutableSet<LifecycleListener?> by lazy {
        Collections.newSetFromMap(WeakHashMap<LifecycleListener, Boolean>())
    }
    override var isStarted: Boolean = false
    override var isStop: Boolean = false
    override var isDestroyed: Boolean = false

    override fun onStart() {
        super.onStart()
        notifyOnStart()
    }

    override fun onStop() {
        notifyOnStop()
        super.onStop()
    }

    override fun onDestroy() {
        notifyOnDestroy()
        super.onDestroy()
    }
}