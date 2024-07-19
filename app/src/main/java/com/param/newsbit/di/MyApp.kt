package com.param.newsbit.di

import android.app.Application
import com.param.newsbit.AbstractApplication
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
open class MyApp : AbstractApplication()