package com.xiaosw.http.internal

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import com.xiaosw.api.logger.Logger
import java.lang.IllegalArgumentException
import java.lang.ref.WeakReference

/**
 * @ClassName [ActivityLifecycleManager]
 * @Description
 *
 * @Date 2019-06-06.
 * @Author xiaosw<xiaosw0802@163.com>.
 */
internal object ActivityLifecycleManager : Application.ActivityLifecycleCallbacks {

    private val mActivityList by lazy {
        mutableListOf<WeakReference<Activity?>>()
    }

    private var mTopActivityRef: WeakReference<Activity?>? = null

    internal var topActivity: Activity? = null
        get() = mTopActivityRef?.get()

    private var isInitializer = false
    internal lateinit var app: Application

    fun init(context: Context) {
        if (isInitializer) {
            Logger.w("init: ActivityLifecycleManager is initializer!")
            return
        }
        app = context as Application
        app.registerActivityLifecycleCallbacks(this)
        isInitializer = true
    }

    fun availableActivity() : Activity {
        mActivityList.asReversed().forEach { activityRef ->
            activityRef.get()?.let {
                if (!it.isFinishing) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        if (!it.isDestroyed) {
                            return it
                        }
                    } else {
                        return it
                    }
                }
            }
        }
        throw IllegalArgumentException("not find available activity!")
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        mActivityList.add(WeakReference(activity))
    }

    override fun onActivityStarted(activity: Activity?) {
    }

    override fun onActivityResumed(activity: Activity?) {
        if (mTopActivityRef?.get() == activity) {
            mTopActivityRef = WeakReference(activity)
        }
    }

    override fun onActivityPaused(activity: Activity?) {
    }

    override fun onActivityStopped(activity: Activity?) {
    }

    override fun onActivityDestroyed(activity: Activity?) {
        mActivityList.filter {
            (it.get() == null || it.get() == activity)
        }.forEach {
            mActivityList.remove(it)
        }
        mTopActivityRef = null
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
    }

}