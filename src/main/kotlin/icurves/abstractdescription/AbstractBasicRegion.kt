package icurves.abstractdescription

import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
data class AbstractBasicRegion(private val inSetInternal: Set<AbstractCurve>) : Comparable<AbstractBasicRegion> {

//    constructor(curves: String) : this(curves.map { AbstractCurve(it.toString()) }.toSet()) {
//    }

    val inSet: SortedSet<AbstractCurve>

    init {
        inSet = Collections.unmodifiableSortedSet(inSetInternal.toSortedSet())
    }

    companion object {
        @JvmField val OUTSIDE = AbstractBasicRegion(TreeSet())
    }

    fun getNumCurves() = inSet.size

    fun contains(curve: AbstractCurve) = inSet.contains(curve)

    fun moveInside(curve: AbstractCurve) = AbstractBasicRegion(inSet.plus(curve))

    fun moveOutside(curve: AbstractCurve) = AbstractBasicRegion(inSet.minus(curve))

    fun hasCurveWithlabel(label: String): Boolean {
        for (curve in inSet)
            if (curve.matchesLabel(AbstractCurve(label)))
                return true

        return false
    }

    fun moveOutsideNew(curve: AbstractCurve): AbstractBasicRegion {
        return AbstractBasicRegion(inSet.filter { !it.matchesLabel(curve) }.toSet())
    }

    fun getStraddledContour(otherRegion: AbstractBasicRegion): Optional<AbstractCurve> {
        if (inSet.size == otherRegion.inSet.size)
            return Optional.empty()

        val biggerSet = if (inSet.size > otherRegion.inSet.size) inSet else otherRegion.inSet
        val smallerSet = if (inSet == biggerSet) otherRegion.inSet else inSet


        val difference = biggerSet.minus(smallerSet)
        return if (difference.size != 1) Optional.empty() else Optional.of(difference.first())
    }

    override fun equals(other: Any?) = inSet.equals((other as AbstractBasicRegion).inSet)

    override fun hashCode(): Int {
        return inSet.hashCode()
    }

    override fun compareTo(other: AbstractBasicRegion): Int {
        if (other.inSet.size < inSet.size) {
            return 1
        } else if (other.inSet.size > inSet.size) {
            return -1
        }

        // same sized in_set
        val this_it = inSet.iterator()
        val other_it = other.inSet.iterator()

        while (this_it.hasNext()) {
            val this_c = this_it.next()
            val other_c = other_it.next()
            val comp = this_c.compareTo(other_c)
            if (comp != 0) {
                return comp
            }
        }
        return 0
    }

    override fun toString() = inSet.map { it.label }.joinToString(",", "{", "}")
}