package icurves.graph

import icurves.diagram.BasicRegion
import javafx.geometry.Point2D

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
data class EulerDualNode(val zone: BasicRegion, val point: Point2D) {

    override fun toString(): String {
        return zone.toString()
    }
}