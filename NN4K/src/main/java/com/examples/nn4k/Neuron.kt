package com.examples.nn4k

import java.util.*

class Neuron {

    private var _actionPotential = 0.0
    val actionPotential
        get() = _actionPotential

    val id: String = UUID.randomUUID().toString()

}
