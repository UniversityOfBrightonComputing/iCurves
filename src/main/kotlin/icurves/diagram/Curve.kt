package icurves.diagram

import icurves.description.AbstractCurve
import javafx.scene.shape.Shape
import math.geom2d.polygon.Polygon2D

/**
 * A closed curve, c (element of C).
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
abstract class Curve(val abstractCurve: AbstractCurve) {

//    /**
//     * @return a curve model for computational geometry.
//     */
//    val polygon by lazy { computePolygon() }
//
//    /**
//     * @return a bitmap view for rendering.
//     */
//    val shape by lazy { computeShape() }

    fun getPolygon() = computePolygon()

    fun getShape() = computeShape()

    abstract fun computePolygon(): Polygon2D

    abstract fun computeShape(): Shape

    override fun toString() = abstractCurve.toString()
}