package com.examples.bp4droid

import org.scenariotools.bpk.Event

abstract class StateEvent<S>(
    val state: S
) : Event()