package icurves.recomposition

import icurves.description.AbstractBasicRegion
import icurves.description.Description

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
interface RecompositionStrategy {

    /**

     * @param zonesToSplit zones needed to split by the curve
     * *
     * @param description abstract description so far
     * *
     * @return clusters
     */
    fun makeClusters(zonesToSplit: List<AbstractBasicRegion>, description: Description): List<Cluster>
}