package com.param.newsbit

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner


class HiltTestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, CustomTest_Application::class.java.name, context)
    }
}