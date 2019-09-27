package com.xiaosw.http.internal

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

/**
 * @ClassName [HttpInitProvider]
 * @Description
 *
 * @Date 2019-06-06.
 * @Author xiaosw<xiaosw0802@163.com>.
 */
internal class HttpInitProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        context?.apply {
            ActivityLifecycleManager.init(this)
        }
        return true
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int  = -1

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = -1

    override fun getType(uri: Uri): String? = null
}