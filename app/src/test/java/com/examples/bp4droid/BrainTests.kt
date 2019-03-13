package com.examples.bp4droid

import com.examples.bprogram.toBThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.scenariotools.bpk.Event
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class BrainTests {

    @ExperimentalCoroutinesApi
    private lateinit var brain: Brain

    private val testScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = Dispatchers.Unconfined
    }


    @Test
    fun `given brain when runUntilTerminate then stuff`() = runBlocking {
        // Arrange
        val input = object : Event() {}
        val expected = true
        var actual = false

        // Act
        val worker = "Worker" toBThread {
            waitFor(input).run {
                actual = true
                brain.terminateExecution()
            }
        }
        val inputer = "Inputer" toBThread {
            request(input)
        }
        brain = Brain(testScope, arrayOf(worker, inputer))
        brain.run()

        // Assert
        assertEquals(expected, actual)
    }

}