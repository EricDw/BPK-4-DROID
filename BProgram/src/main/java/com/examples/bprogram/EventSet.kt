package org.scenariotools.bpk

interface IEventSet
{
    fun contains(element: Event): Boolean
    fun isEmpty(): Boolean
}

interface IConcreteEventSet : IEventSet, Collection<Event>

open class MutableNonConcreteEventSet(vararg events: IEventSet) : IEventSet, HashSet<IEventSet>(events.asList())
{

    override fun contains(element: Event): Boolean
    {
        for (eventSet in this)
        {
            if (eventSet.contains(element)) return true
        }
        return false
    }
}

open class ConcreteEventSet(c: Set<Event>) : Set<Event> by c, IConcreteEventSet
{
    constructor(vararg event: Event) : this(event.toSet())
}

fun eventsOf(vararg events: Event): ConcreteEventSet = ConcreteEventSet(*events)

open class MutableConcreteEventSet : HashSet<Event>, IConcreteEventSet
{
    constructor() : super()
    constructor(c: MutableCollection<Event>) : super(c)
    constructor(vararg event: Event) : super(event.toMutableSet())
}


object ALLEVENTS : IEventSet
{
    override fun contains(element: Event) = true

    override fun isEmpty() = false
}


object NOEVENTS : IConcreteEventSet
{
    override val size: Int
        get() = 0

    override fun containsAll(elements: Collection<Event>) = false

    override fun isEmpty() = true

    override fun iterator(): Iterator<Event> = EmptyEventIterator()

    override fun contains(element: Event) = false

    private class EmptyEventIterator : Iterator<Event>
    {

        override fun hasNext() = false

        override fun next(): Event
        {
            throw UnsupportedOperationException()
        }

    }

}
