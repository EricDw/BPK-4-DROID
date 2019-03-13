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
    fun terminateExecution()
}

@ExperimentalCoroutinesApi
class Brain(
    override val coroutineScope: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = Dispatchers.Default
    }, initialBehaviors: Array<NameToBThreadMain> = emptyArray()
) : BProgram(
    loopUntilTerminate, *initialBehaviors,
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

    override fun terminateExecution() {
        startNewBThread(sendEvent(terminationEvent), coroutineScope)
    }
}

private val terminationEvent = object : Event() {}
private val loopUntilTerminate = "Loop until termination" toBThread {
    var shouldLoop = true
    while (shouldLoop) {
        waitFor(terminationEvent).run {
            shouldLoop = false
            terminate()
        }
    }
}

private fun sendEvent(event: Event) = "Send event" toBThread
        {
            request(event)
        }
