package com.examples.bp4droid

import androidx.lifecycle.LiveData

interface NeuroCircuit<T : State> {
    val state: LiveData<T>
}

interface State
