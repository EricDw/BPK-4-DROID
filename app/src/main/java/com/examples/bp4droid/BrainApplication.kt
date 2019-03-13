package com.examples.bp4droid

import android.app.Application
import kotlinx.coroutines.ExperimentalCoroutinesApi

class BrainApplication : Application() {

    companion object {
        @ExperimentalCoroutinesApi
        val brain: ABrain by lazy {
            Brain().apply {
                runUntilTerminate()
            }
        }
    }


}