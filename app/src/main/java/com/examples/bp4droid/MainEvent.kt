package com.examples.bp4droid

import com.examples.bprogram.NameToBThreadMain
import com.examples.bprogram.toBThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import org.scenariotools.bpk.Event
import org.scenariotools.bpk.eventsOf

sealed class MainEvent : Event()
object FABClicked : MainEvent()

data class NewMainState(
    val mainState: MainState
) : StateEvent<MainState>(mainState)

data class MainState(
    val showHello: Boolean = false
) : State

object MainTermination : MainEvent()

interface BehaviorModule<S : State> {
    val stateData: ReceiveChannel<S>
}

class MainBehaviorModule(
    private val defaultScope: CoroutineScope,
    private val mainScope: CoroutineScope
) : BehaviorModule<MainState> {

    private val _stateData = Channel<MainState>(Channel.UNLIMITED)
    override val stateData: ReceiveChannel<MainState>
        get() = _stateData

    private fun mainStateReducer(): NameToBThreadMain =
        "MainState Reducer" toBThread {
            coroutineScope = defaultScope
            var mainState = MainState()
            var shouldContinue = true
            while (shouldContinue) {
                waitFor(eventsOf(FABClicked, MainTermination)).run {
                    when (this) {
                        is FABClicked -> {
                            mainState = mainState.copy(
                                showHello = !mainState.showHello
                            )
                            request(NewMainState(mainState))

                            _stateData.send(mainState)

                        }
                        else -> shouldContinue = false
                    }
                }
            }
        }

    fun mainBehaviours(): Set<NameToBThreadMain> = setOf(
        mainStateReducer()
    )

}