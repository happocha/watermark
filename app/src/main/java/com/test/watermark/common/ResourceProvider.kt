package com.test.watermark.common

import android.app.Application

interface ResourceProvider {

    fun getString(id: Int, vararg args: Any): String
}

class ResourceProviderImpl(
    private val application: Application
) : ResourceProvider {

    override fun getString(id: Int, vararg args: Any): String =
        application.getString(id, *args)
}
