package icurves.recomposition

import icurves.decomposition.DecompositionStep

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
interface Recomposer {

    fun recompose(decompositionSteps: List<DecompositionStep>): List<RecompositionStep>
}