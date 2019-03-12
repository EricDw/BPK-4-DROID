package com.examples.bp4droid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Suppress("UNCHECKED_CAST")
@ExperimentalCoroutinesApi
class NeuroPathfinder : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainNeuroCircuit::class.java) ->
                MainNeuroCircuit(BrainApplication.brain) as T
            else -> throw IllegalArgumentException("ViewModel not found")
        }
    }
}