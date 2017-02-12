package icurves.recomposition

import icurves.description.Description

/**
 * Single recomposition step.
 * A valid step has following features:
 *
 * 1. The added contour data must be a single curve
 * 2. Added curve must NOT be present in previous description and must be present in the next one
 *
 * Note: number of steps == number of curves.
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class RecompositionStep(

        /**
         * @return description before this step
         */
        val from: Description,

        /**
         * @return description after this step
         */
        val to: Description,

        /**
         * @return how the curve was added
         */
        val addedCurveData: RecompositionData) {

    init {
        val label = addedCurveData.addedCurve.label

        if (from.includesLabel(label))
            throw IllegalArgumentException("Added curve already present")

        if (!to.includesLabel(label))
            throw IllegalArgumentException("Added curve not present in next description")
    }

    override fun toString() = "R_Step[Data=$addedCurveData,From=$from To=$to]"
}