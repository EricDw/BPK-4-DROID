package com.examples.bp4droid

import com.examples.bprogram.BProgram
import com.examples.bprogram.NameToBThreadMain
import com.examples.bprogram.toBThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import org.scenariotools.bpk.Event
import kotlin.coroutines.CoroutineContext

data class Neuron<T>(val data: T)

interface ABrain {
    val neurotransmitter: ReceiveChannel<Neuron<*>>
    val newBehavior: SendChannel<NameToBThreadMain>
    val coroutineScope: CoroutineScope
    fun learnNewSkill(newSkill: Set<NameToBThreadMain>)
}

@ExperimentalCoroutinesApi
class Brain(
    override val coroutineScope: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = Dispatchers.Default
    }
) : BProgram(
    sayHello(),
    coroutineScope = coroutineScope
), ABrain {

    private val _newBehavior: Channel<NameToBThreadMain> =
        Channel(Channel.UNLIMITED)

    override val newBehavior: SendChannel<NameToBThreadMain>
        get() = _newBehavior

    private val _neurotransmitter: Channel<Neuron<*>> =
        Channel(Channel.UNLIMITED)

    override val neurotransmitter: ReceiveChannel<Neuron<*>> =
        _neurotransmitter

    override fun eventSelected(selectedEvent: Event) {
        when (selectedEvent) {
            is HelloEvent -> transmitNeuron("Hello")
        }
    }

    private fun <T> transmitNeuron(data: T) {
        coroutineScope.launch {
            _neurotransmitter.send(Neuron(data))
        }
    }

    private val learningJob = coroutineScope.launch {
        for (behavior in _newBehavior) {
            startNewBThread(behavior, coroutineScope)
        }
    }

    override fun onTerminate() {
        learningJob.cancel()
    }

    override fun learnNewSkill(newSkill: Set<NameToBThreadMain>) {

        val behavior = "Add new skill" toBThread {
            this.coroutineScope = this@Brain.coroutineScope
            addBThreads(newSkill)
        }

        startNewBThread(behavior, coroutineScope)

    }
}

private class HelloEvent : Event()

private val defaultScope = object : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default
}

private fun sayHello(
    coroutineScope: CoroutineScope = defaultScope
): NameToBThreadMain = "Say Hello" toBThread {
    this.coroutineScope = coroutineScope
    request(HelloEvent())
}