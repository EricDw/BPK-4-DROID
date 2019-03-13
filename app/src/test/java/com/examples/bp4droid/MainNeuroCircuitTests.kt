package com.examples.bp4droid


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.examples.bprogram.toBThread
import kotlinx.coroutines.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlin.coroutines.CoroutineContext

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@ExperimentalCoroutinesApi
class MainNeuroCircuitTests {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private lateinit var mainNeuroCircuit: MainNeuroCircuit
    private lateinit var mainSate: MainState

    private val testScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = Dispatchers.Unconfined
    }

    private lateinit var brain: Brain

    @Test
    fun `given FABClicked when transmitImpulse then showHello is true`() = runBlocking {
        // Arrange
        val input = FABClicked
        val expected = MainState(
            showHello = false
        )

        val module = MainBehaviorModule(
            testScope,
            testScope
        )

        mainNeuroCircuit = MainNeuroCircuit(
            testScope,
            testScope,
            module
        )

        mainNeuroCircuit.state.observeForever {
            it?.run {
                mainSate = it
            }
        }


        brain = Brain(
            testScope,
            module.mainBehaviours().toTypedArray()
        )


        // Act
        brain.newBehavior.send("Clicker" toBThread {
            request(input)
            request(input)
        }
        )
        brain.newBehavior.send("Terminator" toBThread {
            request(MainTermination)
        }
        )
        brain.run()
        val actual = mainSate

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun `given MainBehaviorModule when FABCLicked then showHello is true`() = runBlocking {
        // Arrange
        val input = FABClicked
        val expected = MainState(
            showHello = true
        )
        val module = MainBehaviorModule(
            testScope,
            testScope
        )
        val stateJob = testScope.launch {
            for (state in module.stateData) {
                mainSate = state
            }
        }

        val aBrain = Brain(
            testScope,
            module.mainBehaviours().toTypedArray()
        )
        // Act
        aBrain.newBehavior.send("Clicker" toBThread {
            request(input)
        }
        )
        aBrain.newBehavior.send("Terminator" toBThread {
            request(MainTermination)
        }
        )
        aBrain.run()

        val actual = mainSate

        // Assert
        assertEquals(expected, actual)
    }


}
