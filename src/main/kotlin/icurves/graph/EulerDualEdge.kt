package icurves.graph

import javafx.scene.shape.QuadCurve
import javafx.scene.shape.Shape

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class EulerDualEdge(val v1: EulerDualNode, val v2: EulerDualNode, val curve: Shape) {

    override fun toString(): String {
        return "($v1 -> $v2)"
    }
}