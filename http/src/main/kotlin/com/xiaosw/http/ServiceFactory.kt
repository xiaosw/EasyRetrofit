package com.xiaosw.http

import com.xiaosw.api.logger.Logger
import okhttp3.Interceptor
import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.TimeUnit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


/**
 * @ClassName [ServiceFactory]
 * @Description
 *
 * @Date 2019-08-22.
 * @Author xiaosw<xiaosw0802@163.com>.
 */
object ServiceFactory {

    private const val TAG = "ServiceFactory"

    /** Service Cache  */
    private val sServices by lazy {
        HashMap<String, ArrayList<ServiceEntry<*>>>()
    }

    private val sInterceptors by lazy {
        HashMap<String, Interceptor?>()
    }

    var httpsVerify: HttpsVerify? = null

    init {
        System.loadLibrary("cash-station")
    }

    @JvmOverloads
    @JvmStatic
    fun <T> createServiceFrom(baseUrl: String,
                              serviceClazz: Class<T>, needHttpsVerify: Boolean = true): T {
        findFromCache(serviceClazz, baseUrl, needHttpsVerify)?.apply {
            Logger.d(TAG, "createServiceFrom: find service 【${serviceClazz.name}】 from cache.")
            return targetService as T
        }
        // set
        val httpClientBuilder = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(180, TimeUnit.SECONDS)
        sInterceptors.filter {
            it.value != null
        }.forEach {
            httpClientBuilder.addInterceptor(it.value)
        }
        if (needHttpsVerify) {
            httpsVerify?.run {
                httpClientBuilder.sslSocketFactory(getSslSocketFactory(), getTrustManager())
            }
        }
        val adapter = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClientBuilder.build())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val targetService = adapter.create(serviceClazz)
        // add to cache
        val service = ServiceEntry(
            serviceClazz,
            targetService,
            needHttpsVerify
        )
        with(sServices[baseUrl]) {
            if (null != this) {
                add(service)
            } else{
                val cacheServiceEntries = ArrayList<ServiceEntry<*>>()
                cacheServiceEntries.add(service)
                sServices.put(baseUrl, cacheServiceEntries)
            }
        }
        return targetService
    }

    /**
     * find cache service
     * @param serviceClazz
     * @param baseUrl
     * @return
     */
    @Synchronized
    private fun findFromCache(serviceClazz: Class<*>, baseUrl: String, needHttpsVerify: Boolean): ServiceEntry<*>? {
        val cacheServices = sServices[baseUrl]
        cacheServices?.forEach {
            if (it?.serverClazz == serviceClazz) {
                if (null != it.targetService && needHttpsVerify == it.needHttpsVerify) {
                    return it
                }
            }
        }
        return null
    }

    /**
     * Service cache entry.
     */
    private class ServiceEntry<out T>(val serverClazz: Class<*>, val targetService: T, val needHttpsVerify: Boolean)

    fun addInterceptor(interceptor: Interceptor) {
        sServices.clear()
        sInterceptors[interceptor.javaClass.name] = interceptor
    }

    fun addInterceptors(vararg interceptors: Interceptor) {
        sServices.clear()
        interceptors.forEach {
            it?.apply {
                sInterceptors[javaClass.name] = this
            }
        }
    }

    fun removeInterceptor(interceptor: Interceptor) {
        if (sInterceptors.containsKey(interceptor.javaClass.name)) {
            sServices.clear()
            sInterceptors.remove(interceptor.javaClass.name)
        }
    }

}

