package com.test.watermark.extensions

import android.view.View

fun View.showNow(value: Boolean) {
    visibility = if (value) View.VISIBLE else View.GONE
}

