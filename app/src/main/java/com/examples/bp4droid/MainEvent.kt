package com.examples.bp4droid

import org.scenariotools.bpk.Event

sealed class MainEvent : Event()
object FABClicked : MainEvent()


data class MainState(
    val showHello: Boolean = false
) : State
