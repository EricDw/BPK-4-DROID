package com.examples.bp4droid

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.examples.bprogram.NameToBThreadMain
import com.examples.bprogram.toBThread
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.scenariotools.bpk.Event
import org.scenariotools.bpk.doWhile
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
        val waitForBlargBlarg = "Wait For Blarg Blarg" toBThread {
            priority = 1
            doWhile(true) {
                waitFor(blargBlarg).run {
                    GlobalScope.launch(Dispatchers.Main) {
                        val message = "I am thread 1 and my priority is $priority "
                        messages.text = message
                    }
                }
            }
        }
        val waitForBlargBlarg2 = "Wait For Blarg Blarg 2" toBThread {
            priority = 2
            doWhile(true) {
                waitFor(blargBlarg).run {
                    GlobalScope.launch(Dispatchers.Main) {
                        val message = "I am thread 2 and my priority is $priority "
                        messages.text = message
                    }
                }
            }
        }

        brain.learnNewSkill(setOf(waitForBlargBlarg, waitForBlargBlarg2))

        fab.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                newBehaviors.send(
                    sayBlargBlarg
                )
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
