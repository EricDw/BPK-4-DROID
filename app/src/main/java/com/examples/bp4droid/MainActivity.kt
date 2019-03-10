package com.examples.bp4droid

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
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

    private val newBehaviors: Channel<NameToBThreadMain> = Channel(Channel.UNLIMITED)

    private val brain: ABrain = Brain(
        newBehavior = newBehaviors
    ).apply {
        runUntilTerminate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val blargBlarg = object : Event() {}

        var clicks = 0
        fab.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                newBehaviors.send(
                    "Say Blarg  Blarg" toBThread {
                        GlobalScope.launch(Dispatchers.Main) {
                            clicks++
                            val message = "Blarg Blarg $clicks"
                            messages.text = message
                        }
                        request(blargBlarg)
                    }
                )

                newBehaviors.send(
                    "Wait For Blarg Blarg" toBThread {
                        waitFor(blargBlarg).run {
                            GlobalScope.launch(Dispatchers.Main) {
                                delay(4000)
                                val message = "Blarg Blarg intercepted"
                                messages.text = message
                            }
                        }
                    }
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
