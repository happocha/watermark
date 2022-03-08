package com.test.watermark.extensions

import androidx.fragment.app.Fragment
import com.test.watermark.App

val Fragment.app get() = activity?.application as App
