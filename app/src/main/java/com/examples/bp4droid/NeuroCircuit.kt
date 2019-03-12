package com.examples.bp4droid

import androidx.lifecycle.LiveData
import org.scenariotools.bpk.Event

interface NeuroCircuit<T : State> {
    val state: LiveData<T>
    fun transmitImpulse(impulse: Event)
}

interface State
