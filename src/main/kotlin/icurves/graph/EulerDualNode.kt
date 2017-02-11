package icurves.graph

import icurves.concrete.ConcreteZone
import javafx.geometry.Point2D

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
data class EulerDualNode(val zone: ConcreteZone, val point: Point2D) {

    override fun toString(): String {
        return zone.toString()
    }
}