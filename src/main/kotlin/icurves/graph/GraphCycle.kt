package icurves.graph

import javafx.geometry.Point2D
import javafx.scene.shape.Path


/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
data class GraphCycle<V, E>(val nodes: List<V>, val edges: List<E>) {

    lateinit var path: Path
    lateinit var smoothingData: MutableList<Point2D>

    fun length() = nodes.size

    fun contains(node: V): Boolean {
        for (n in nodes) {
            if (n.toString() == node.toString()) {
                return true
            }
        }

        return false
    }

//    fun contains(zones: List<AbstractBasicRegion>): Boolean {
//        val mappedNodes = nodes.map { it.zone.abstractZone }
//
//        return mappedNodes.containsAll(zones)
//    }
}