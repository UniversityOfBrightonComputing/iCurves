package icurves.recomposition

import icurves.diagram.BasicRegion
import javafx.geometry.Point2D

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class PiercingData(cluster: List<BasicRegion>, basicRegions: List<BasicRegion>) {

    val center: Point2D?
    val radius: Double

    init {
        center = cluster.map { it.getPolygonShape().vertices() }
                .flatten()
                .groupBy({ it.asInt })
                // we search for a vertex that is present in all four regions
                .filter { it.value.size == 4 }
                .map { Point2D(it.key.getX(), it.key.getY()) }
                .firstOrNull()

        if (center != null) {
            radius = basicRegions
                    .minus(cluster)
                    .map { it.getPolygonShape().distance(center.x, center.y) }
                    .sorted()
                    .first()
        } else {
            radius = 0.0
        }
    }

    fun isPiercing() = center != null
}