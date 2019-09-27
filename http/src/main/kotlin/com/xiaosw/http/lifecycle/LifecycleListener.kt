package com.xiaosw.http.lifecycle

/**
 * @ClassName [LifecycleListener]
 * @Description
 *
 * @Date 2019-05-05.
 * @Author xiaosw<xiaosw0802@163.com>.
 */
interface LifecycleListener {

    /**
     * Callback for when [androidx.fragment.app.Fragment.onStart]} or [android.app.Activity.onStart] is called.
     */
    fun onStart()

    /**
     * Callback for when [androidx.fragment.app.Fragment.onResume]} or [android.app.Activity.onResume]} is called.
     */
    fun onResume()

    /**
     * Callback for when [androidx.fragment.app.Fragment.onPause]} or [android.app.Activity.onPause]} is called.
     */
    fun onPause()

    /**
     * Callback for when [androidx.fragment.app.Fragment.onStop]} or [android.app.Activity.onStop]} is called.
     */
    fun onStop()

    /**
     * Callback for when [androidx.fragment.app.Fragment.onDestroy]} or [android.app.Activity.onDestroy] is called.
     */
    fun onDestroy()

}