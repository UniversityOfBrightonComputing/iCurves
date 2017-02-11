package icurves.description

import java.util.*

/**
 * An abstract basic region, \beta (element of B), is a set of abstract curves.
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
data class AbstractBasicRegion(private val inSetInternal: Set<AbstractCurve>) : Comparable<AbstractBasicRegion> {

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

    fun getStraddledContour(otherRegion: AbstractBasicRegion): Optional<AbstractCurve> {
        if (inSet.size == otherRegion.inSet.size)
            return Optional.empty()

        val biggerSet = if (inSet.size > otherRegion.inSet.size) inSet else otherRegion.inSet
        val smallerSet = if (inSet == biggerSet) otherRegion.inSet else inSet

        val difference = biggerSet.minus(smallerSet)
        return if (difference.size != 1) Optional.empty() else Optional.of(difference.first())
    }

    override fun equals(other: Any?) = inSet == (other as AbstractBasicRegion).inSet

    override fun hashCode() = inSet.hashCode()

    override fun compareTo(other: AbstractBasicRegion): Int {
        if (other.inSet.size < inSet.size) {
            return 1
        } else if (other.inSet.size > inSet.size) {
            return -1
        }

        // same sized in_set
        val thisIter = inSet.iterator()
        val otherIter = other.inSet.iterator()

        while (thisIter.hasNext()) {
            val thisCurve = thisIter.next()
            val otherCurve = otherIter.next()
            val comparisonResult = thisCurve.compareTo(otherCurve)
            if (comparisonResult != 0) {
                return comparisonResult
            }
        }
        return 0
    }

    override fun toString() = inSet.map { it.label }.joinToString(",", "{", "}")
}