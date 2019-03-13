package com.examples.bp4droid

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class MainNeuroCircuit(
    workerScope: CoroutineScope,
    private val mainScope: CoroutineScope,
    behaviorModule: BehaviorModule<MainState>
) : ViewModel(), NeuroCircuit<MainState> {
    private val _state: MutableLiveData<MainState> = MutableLiveData()
    override val state: LiveData<MainState>
        get() = _state

    private val stateJob = workerScope.launch {
        for (state in behaviorModule.stateData) {
            mainScope.launch {
                _state.value = state
            }
        }
    }

    override fun onCleared() {
        stateJob.cancel()
        super.onCleared()
    }
}
