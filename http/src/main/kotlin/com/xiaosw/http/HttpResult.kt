package com.xiaosw.http

/**
 * @ClassName [HttpResult]
 * @Description
 *
 * @Date 2019-04-26.
 * @Author xiaosw<xiaosw0802@163.com>.
 */
interface HttpResult {

    fun code() : Int

    fun message() : String?

}