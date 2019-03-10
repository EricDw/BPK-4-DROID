package com.examples.bp4droid

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.examples.bprogram.NameToBThreadMain
import com.examples.bprogram.toBThread
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.scenariotools.bpk.Event
import kotlin.examples.bp4droid.R

@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val newBehaviors: Channel<NameToBThreadMain> = Channel(Channel.UNLIMITED)

        val brain: ABrain = Brain(
            newBehavior = newBehaviors
        ).apply {
            runUntilTerminate()
        }

        val blargBlarg = object : Event() {}

        var clicks = 0
        val sayBlargBlarg = "Say Blarg  Blarg" toBThread {
            GlobalScope.launch(Dispatchers.Main) {
                clicks++
                val message = "Blarg Blarg $clicks"
                messages.text = message
            }
            request(blargBlarg)
        }
        var interceptions = 0
        val waitForBlargBlarg = "Wait For Blarg Blarg" toBThread {
            waitFor(blargBlarg).run {
                GlobalScope.launch(Dispatchers.Main) {
                    delay((1 + interceptions * 1000).toLong())
                    interceptions++
                    val message = "Blarg Blarg intercepted $interceptions times"
                    messages.text = message
                }
            }
        }

        fab.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                newBehaviors.send(
                    sayBlargBlarg
                )
                newBehaviors.send(
                    waitForBlargBlarg
                )
                brain.learnNewSkill(setOf(sayBlargBlarg, waitForBlargBlarg))
            }
        }

        GlobalScope.launch(Dispatchers.Main) {
            for (neuron in brain.neurotransmitter) {
                messages.text = neuron.data.toString()
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
