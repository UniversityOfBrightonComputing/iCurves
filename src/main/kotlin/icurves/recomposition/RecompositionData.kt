package icurves.recomposition

import icurves.description.AbstractBasicRegion
import icurves.description.AbstractCurve

/**
 * Holds data for a single recomposition step.
 */
data class RecompositionData(
        /**
         * The curve added at this step.
         */
        val addedCurve: AbstractCurve,
        /**
         * The zones we split at this step. The zones are
         * in the "from" abstract description.
         */
        val splitZones: List<AbstractBasicRegion>,
        /**
         * The zones we created at this step. The zones are
         * in the "to" abstract description.
         */
        val newZones: List<AbstractBasicRegion>) {

    /**
     * @return true iff the added curve is nested (splits 1 zone)
     */
    fun isNested() = splitZones.size == 1

    fun isSinglePiercing() = splitZones.size == 2

    fun isDoublePiercing() = splitZones.size == 4

    fun isNotPiercing() = splitZones.size > 4

    fun getSplitZone(index: Int) = splitZones.get(index)

    override fun toString() = "R_Data[added=$addedCurve,split=$splitZones,new=$newZones]"
}