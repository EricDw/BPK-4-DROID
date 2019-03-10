package com.examples.bprogram

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.scenariotools.bpk.*
import kotlin.coroutines.CoroutineContext


data class NameToBThreadMain(val name: String, val bThreadMain: suspend BThread.() -> Unit)

fun bThread(name: String, bThreadMain: suspend BThread.() -> Unit) =
    NameToBThreadMain(name, bThreadMain)
infix fun String.toBThread(bThreadMain: suspend BThread.() -> Unit) =
    NameToBThreadMain(this, bThreadMain)

open class AbstractBSyncMessage
data class TerminatingBThreadMessage(val sender: BThread) : AbstractBSyncMessage()
data class SuspendMessage(val sender: BThread, val blockedEvents: IEventSet) : AbstractBSyncMessage()
data class AddBThreadsMessage(val sender: BThread, val bThreadsToBeAdded: Set<NameToBThreadMain>) :
    AbstractBSyncMessage()

data class BSyncMessage(
    val sender: BThread,
    val requestedEvents: IConcreteEventSet,
    val waitedForEvents: IEventSet,
    val blockedEvents: IEventSet
) : AbstractBSyncMessage()


object RESUME

/**
 * A BProgram starts and coordinates the execution of BThreads.
 *
 * It repeatedly performs the following steps:
 * 1. Waits to receive bSync messages from all busy BThreads
 * 2. Event selection: select a requested event that is not blocked by any other BThread.
 * 3. Send the selected event to all BThreads that requested or waited for the selected event
 */
@ExperimentalCoroutinesApi
open class BProgram(
    private vararg val initialBThreads: NameToBThreadMain,
    private val coroutineScope: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = Dispatchers.Unconfined
    }
)
{

    private val bSyncChannel: Channel<AbstractBSyncMessage> = Channel()
    private val bResumeChannel: Channel<BThread> = Channel()
    private var numberOfBUSYBThreads = 0
    private val activeBThreadsToLastBSyncMessage: MutableMap<BThread, BSyncMessage> = mutableMapOf()
    private val suspendedBThreadsToBlockedEvents: MutableMap<BThread, IEventSet> = mutableMapOf()
    private var shouldTerminate = false

    fun BThread.runBThread(bThreadMain: suspend BThread.() -> Unit) = coroutineScope.launch {
        bThreadMain()
        bSyncChannel.send(TerminatingBThreadMessage(this@runBThread))
    }

    fun startNewBThread(bThread: NameToBThreadMain, coroutineScope: CoroutineScope)
    {
        BThread(bSyncChannel, bResumeChannel, coroutineScope, bThread.name).runBThread(bThread.bThreadMain)
        //BThread(bSyncChannel, bResumeChannel, GlobalScope, bThread.name).runBThread(bThread.bThreadMain)

        // number of BUSY BThreads (BThreads from which the BProgram awaits the next bSync message).
        // Is incremented when a BThread is added, decremented when BThread terminates or waits
        // BProgram ends when numberOfActiveBThreads == 0
        incrementBusyBThreads()
    }

    open fun run() = coroutineScope.launch {
        for (bThread in initialBThreads)
        {
            startNewBThread(bThread, this)
        }

        // main loop
        while (numberOfBUSYBThreads > 0 || suspendedBThreadsToBlockedEvents.isNotEmpty())
        {
            eventSelectionAndNotification(coroutineScope)
            resumeSuspendedBThreadsIfPossible()
        }
    }

    fun terminate() {
        onTerminate()
        shouldTerminate = true
    }

    open fun onTerminate() = Unit

    fun runUntilTerminate() = coroutineScope.launch {
        for (bThread in initialBThreads)
        {
            startNewBThread(bThread, this)
        }

        // main loop
        while (!shouldTerminate)
        {
            eventSelectionAndNotification(coroutineScope)
            resumeSuspendedBThreadsIfPossible()
        }
    }

    private suspend fun resumeSuspendedBThreadsIfPossible()
    {
        while (suspendedBThreadsToBlockedEvents.isNotEmpty() && !bResumeChannel.isEmpty)
        {
            val resumedBThread = bResumeChannel.receive()
            incrementBusyBThreads()
            suspendedBThreadsToBlockedEvents.remove(resumedBThread)
        }
    }

    private suspend fun eventSelectionAndNotification(mainCoroutineScope: CoroutineScope)
    {
        if (numberOfBUSYBThreads > 0)
        {

            receiveBSyncMessages(mainCoroutineScope)

            val selectableEvents = calculateSelectableEvents()
            val selectedEvent = selectEvent(selectableEvents)

            if (selectedEvent != null)
            {
                eventSelected(selectedEvent)
                notifyActiveBThreads(selectedEvent)
            }
        }
    }


    /**
     * Can be extended to change event selection strategy
     */
    open fun selectEvent(selectableEvents: IConcreteEventSet): Event? =
        if (selectableEvents.isNotEmpty()) selectableEvents.first() else null

    /**
     * Can be extended to monitor, log, etc. selected events
     */
    open fun eventSelected(selectedEvent: Event) = Unit

    private suspend fun receiveBSyncMessages(mainCoroutineScope: CoroutineScope)
    {
        while (numberOfBUSYBThreads > 0)
        {
            val abstractBSyncMessage = bSyncChannel.receive()
            when (abstractBSyncMessage)
            {
                is BSyncMessage ->
                {
                    activeBThreadsToLastBSyncMessage[abstractBSyncMessage.sender] = abstractBSyncMessage
                    decrementBusyBThreads()
                }
                is SuspendMessage ->
                {
                    suspendedBThreadsToBlockedEvents[abstractBSyncMessage.sender] =
                        abstractBSyncMessage.blockedEvents
                    decrementBusyBThreads()
                }
                is TerminatingBThreadMessage ->
                {
                    if (suspendedBThreadsToBlockedEvents.containsKey(abstractBSyncMessage.sender))
                        suspendedBThreadsToBlockedEvents.remove(abstractBSyncMessage.sender)
                    else
                    {
                        activeBThreadsToLastBSyncMessage.remove(abstractBSyncMessage.sender)
                        decrementBusyBThreads()
                    }
                }
                is AddBThreadsMessage ->
                {
                    for (bt in abstractBSyncMessage.bThreadsToBeAdded)
                    {
                        startNewBThread(bt, mainCoroutineScope)
                    }
                }
                else -> handleBSyncMessage(abstractBSyncMessage)
            }
        }
    }

    private fun decrementBusyBThreads()
    {
        numberOfBUSYBThreads--
    }

    open fun handleBSyncMessage(abstractBSyncMessage: AbstractBSyncMessage) = Unit

    private fun calculateSelectableEvents(): IConcreteEventSet
    {
        val allSelectableEvents = MutableConcreteEventSet()
        val allBlockedEventSets = MutableNonConcreteEventSet()

        var thereAreRequestedEvents = false

        for (bThreadToEventsMapEntry in activeBThreadsToLastBSyncMessage)
        {
            val be = bThreadToEventsMapEntry.value.blockedEvents
            if (be == NOEVENTS || be is MutableNonConcreteEventSet && be.size == 1 && be.iterator().next() == NOEVENTS) continue

            allBlockedEventSets.add(be)
            //println("${c++}======= BLOCKED: ${bThreadToEventsMapEntry.value.blockedEvents} BY ${bThreadToEventsMapEntry.key.name} $numberOfBUSYBThreads ${activeBThreadsToLastBSyncMessage.size}")
        }

        for (suspendedBThreadsToBlockedEventsEntry in suspendedBThreadsToBlockedEvents)
        {
            allBlockedEventSets.add(suspendedBThreadsToBlockedEventsEntry.value)
        }

        for (bThreadToEventsMapEntry in activeBThreadsToLastBSyncMessage)
        {
            for (requestedEvent in bThreadToEventsMapEntry.value.requestedEvents)
            {
                thereAreRequestedEvents = true
                if (!allBlockedEventSets.contains(requestedEvent))
                {
                    allSelectableEvents.add(requestedEvent)
                } else
                {
                    //println("======= BLOCKED: $requestedEvent")
                }
            }
        }

        if (thereAreRequestedEvents && allSelectableEvents.isEmpty())
            throw RuntimeException("BProgram is stuck scenario BTreads requesting events that are all blocked by other BThreads.")

        return allSelectableEvents
    }

    private suspend fun notifyActiveBThreads(selectedEvent: Event)
    {

        val bThreadToEventsMapEntryIterator = activeBThreadsToLastBSyncMessage.iterator()

        while (bThreadToEventsMapEntryIterator.hasNext())
        {
            val bThreadToEventsMapEntry = bThreadToEventsMapEntryIterator.next()
            val value = bThreadToEventsMapEntry.value
            if (value.waitedForEvents.contains(selectedEvent) || value.requestedEvents.contains(selectedEvent)
            )
            {
                bThreadToEventsMapEntry.key.notifyOfEventChannel.send(selectedEvent)
                bThreadToEventsMapEntryIterator.remove()
                incrementBusyBThreads()
            }
        }

    }

    private fun incrementBusyBThreads()
    {
        numberOfBUSYBThreads++
    }

}