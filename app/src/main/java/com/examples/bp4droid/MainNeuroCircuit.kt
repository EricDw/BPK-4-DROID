package com.examples.bp4droid

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.examples.bprogram.NameToBThreadMain
import com.examples.bprogram.toBThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.scenariotools.bpk.Event
import org.scenariotools.bpk.doWhile
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class MainNeuroCircuit(
    private val brain: ABrain,
    private val defaultScope: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = Dispatchers.Default
    }
) : ViewModel(), NeuroCircuit<MainState> {

    private val _state = MutableLiveData<MainState>()

    override val state: LiveData<MainState>
        get() = _state

    init {
        brain.learnNewSkill(mainBehaviours())
    }

    override fun transmitImpulse(impulse: Event) {
        defaultScope.launch {
            brain.newBehavior.send(
                generateImpulseFor(impulse)
            )
        }
    }

    private fun generateImpulseFor(event: Event) =
        "Requesting $event" toBThread {
            priority = 2
            Log.d(
                MainNeuroCircuit::class.simpleName,
                "Requesting $event"
            )
            request(event)
        }

    private fun mainStateReducer(): NameToBThreadMain = "MainState Reducer" toBThread {
        var mainState = MainState()
        doWhile(true) {
            Log.d("VModel", "running in scope ${coroutineScope.coroutineContext}")
            waitFor(FABClicked).run {
                coroutineScope.launch(Dispatchers.Main) {
                    Log.d("VModel", "running in scope ${coroutineContext}")
                    mainState = mainState.copy(
                        showHello = !mainState.showHello
                    )
                    _state.value = mainState
                }
            }
        }
    }

    private fun mainBehaviours(): Set<NameToBThreadMain> = setOf(
        mainStateReducer()
    )

}
