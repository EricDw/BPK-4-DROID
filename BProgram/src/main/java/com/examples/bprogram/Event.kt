package org.scenariotools.bpk

open class Event : IConcreteEventSet
{
    override val size: Int = 1

    override fun containsAll(elements: Collection<Event>): Boolean = when
    {
        elements.isEmpty() -> true
        elements.size == 1 -> contains(elements.first())
        else -> false
    }

    override fun isEmpty(): Boolean = false

    override fun iterator(): Iterator<Event> = SingleEventIterator(this)

    override fun contains(element: Event): Boolean = this == element

    private class SingleEventIterator(private val e: Event) : Iterator<Event>
    {

        var hasNext = true

        override fun hasNext(): Boolean = hasNext

        override fun next(): Event
        {
            hasNext = false
            return e
        }

    }
}
