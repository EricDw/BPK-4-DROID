package org.scenariotools.bpk

suspend fun doWhile(condition: Boolean, f: suspend () -> Unit)
{
    do
    {
        f()
    } while (condition)
}