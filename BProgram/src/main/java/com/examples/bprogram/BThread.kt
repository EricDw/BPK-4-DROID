package org.scenariotools.bpk

import com.examples.bprogram.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlin.coroutines.CoroutineContext


@ExperimentalCoroutinesApi
object UnconfinedScope : CoroutineScope
{
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Unconfined
}

open class BThread @ExperimentalCoroutinesApi constructor(
    val bSyncChannel: Channel<AbstractBSyncMessage>,
    val bResumeChannel: Channel<BThread>,
    var coroutineScope: CoroutineScope = UnconfinedScope,
    val name: String = "",
    open val priority: Int = 0
) : Comparable<BThread>
{

    val notifyOfEventChannel: Channel<Event> = Channel()

    private var suspended = false

    override fun compareTo(other: BThread): Int = priority.compareTo(other.priority)

    suspend fun terminate(cause: String? = null)
    {
        if (suspended)
            bResume()
        bSyncChannel.send(TerminatingBThreadMessage(this@BThread))
        throw CancellationException(cause)
    }

    suspend fun bSync(
        requestedEvents: IConcreteEventSet,
        waitedForEvents: IEventSet,
        blockedEvents: IEventSet
    ): Event
    {
        val bSyncMessage = BSyncMessage(
            this@BThread,
            requestedEvents,
            waitedForEvents,
            blockedEvents
        )
        bSyncChannel.send(bSyncMessage)
        return notifyOfEventChannel.receive()
    }

    suspend fun addBThreads(bThreadMains: Set<NameToBThreadMain>)
    {
        val addBThreadMessage = AddBThreadsMessage(this@BThread, bThreadMains)
        bSyncChannel.send(addBThreadMessage)
    }

    suspend fun addBThread(name: String, bThreadMain: suspend BThread.() -> Unit)
    {
        addBThreads(setOf(NameToBThreadMain(name, bThreadMain)))
    }

    suspend infix fun String.behavesLike(bThreadMain: suspend BThread.() -> Unit)
    {
        addBThreads(setOf(NameToBThreadMain(this, bThreadMain)))
    }

    suspend fun bSuspend(milliseconds: Long, blockedEvents: IEventSet)
    {
        bSuspend(blockedEvents)
        delay(milliseconds)
        bResume()
    }

    suspend fun bSuspend(milliseconds: Int, blockedEvents: IEventSet, logMessage: String)
    {
        bSuspend(blockedEvents)
        val deciSeconds = milliseconds / 100
        val remainder = milliseconds % 100
        repeat(deciSeconds) {
            delay(100)
            println("... suspended " + it + "00 ms ($name): $logMessage (" + it + "00 of " + deciSeconds + "00), " + ((it * 100) / deciSeconds) + "%")
        }
        delay(remainder.toLong())
        bResume()
    }

    suspend fun bSuspend()
    {
        bSuspend(NOEVENTS)
    }

    suspend fun bSuspend(blockedEvents: IEventSet)
    {
        bSyncChannel.send(SuspendMessage(this@BThread, blockedEvents))
        suspended = true
    }

    suspend fun bSuspend(milliseconds: Int, logMessage: String)
    {
        bSuspend(milliseconds, NOEVENTS, logMessage)
    }

    suspend fun bSuspend(milliseconds: Int)
    {
        bSuspend(milliseconds, NOEVENTS, "")
    }

    suspend fun bResume()
    {
        if (!suspended)
            throw RuntimeException("BThread $name is not suspended, thus cannot be resumed.")
        bResumeChannel.send(this)
        suspended = false
    }

    open suspend fun requestAndBlock(requestedEvents: IConcreteEventSet, blockedEvents: IEventSet): Event
    {
        return bSync(requestedEvents, NOEVENTS, blockedEvents)
    }

    open suspend fun request(requestedEvents: IConcreteEventSet): Event
    {
        return requestAndBlock(requestedEvents, NOEVENTS)
    }

    open suspend fun waitForAndBlock(waitedForEvents: IEventSet, blockedEvents: IEventSet): Event
    {
        return bSync(NOEVENTS, waitedForEvents, blockedEvents)
    }

    open suspend fun waitFor(waitedForEvents: IEventSet): Event
    {
        return waitForAndBlock(waitedForEvents, NOEVENTS)
    }

    override fun toString(): String = name

}