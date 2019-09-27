package com.xiaosw.http.lifecycle

/**
 * @ClassName [Lifecycle]
 * @Description
 *
 * @Date 2019-05-05.
 * @Author xiaosw<xiaosw0802@163.com>.
 */
internal interface Lifecycle {

    /**
     * Adds the given listener to the set of listeners managed by this Lifecycle implementation.
     */
    fun addListener(listener: LifecycleListener)

    /**
     * Removes the given listener from the set of listeners managed by this Lifecycle implementation,
     * returning {@code true} if the listener was removed successfully, and {@code false} otherwise.
     *
     * <p>This is an optimization only, there is no guarantee that every added listener will
     * eventually be removed.
     */
    fun removeListener(listener: LifecycleListener)

    fun clearListener()

}