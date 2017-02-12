package icurves.diagram

import icurves.description.AbstractBasicRegion
import icurves.description.AbstractCurve
import icurves.util.Polylabel
import javafx.geometry.Point2D
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import math.geom2d.polygon.Polygon2D
import math.geom2d.polygon.Polygons2D
import math.geom2d.polygon.SimplePolygon2D
import java.util.*

/**
 * A basic region, br (element of BR), in an Euler diagram.
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class BasicRegion(

        /**
         * The abstract basic region of this basic region.
         */
        val abRegion: AbstractBasicRegion,

        /**
         * Curves within this zone.
         */
        val containingCurves: MutableList<Curve>,

        /**
         * Curves outside of this zone.
         */
        val excludingCurves: MutableList<Curve>) {

    constructor(abRegion: AbstractBasicRegion, curveToContour: Map<AbstractCurve, Curve>) : this(abRegion, arrayListOf(), ArrayList(curveToContour.values)) {
        for (curve in abRegion.inSet) {
            val contour = curveToContour[curve]!!

            excludingCurves.remove(contour)
            containingCurves.add(contour)
        }
    }

    val bbox: Shape

    init {
        // TODO: make global bbox
        bbox = Rectangle(10000.0, 10000.0)
        bbox.translateX = -3000.0
        bbox.translateY = -3000.0
    }

    fun getShape(): Shape {
        var shape: Shape = bbox

        for (curve in containingCurves) {
            shape = Shape.intersect(shape, curve.getShape())
        }

        for (curve in excludingCurves) {
            shape = Shape.subtract(shape, curve.getShape())
        }

        return shape
    }

    fun intersects(shape: Shape): Boolean {
        return !Shape.intersect(getShape(), shape).layoutBounds.isEmpty
    }

    val center by lazy { computeVisualCentre() }

    private fun computeVisualCentre(): Point2D {
        if (polygonShape == null)
            polygonShape = getPolygonShape()

        return Polylabel.findCenter(polygonShape!!)
    }

    private var polygonShape: Polygon2D? = null

    fun getPolygonShape(): Polygon2D {
        polygonShape = SimplePolygon2D(math.geom2d.Point2D(0.0, 0.0),
                math.geom2d.Point2D(10000.0, 0.0),
                math.geom2d.Point2D(10000.0, 10000.0),
                math.geom2d.Point2D(0.0, 10000.0))

        containingCurves.map({ c -> c.getPolygon() }).forEach { p -> polygonShape = Polygons2D.intersection(polygonShape!!, p) }

        excludingCurves.map({ c -> c.getPolygon() }).forEach { p -> polygonShape = Polygons2D.difference(polygonShape!!, p) }

        return polygonShape!!
    }

    fun isTopologicallyAdjacent(other: BasicRegion): Boolean {
        if (abRegion.getStraddledContour(other.abRegion).isPresent) {
            val shape2 = other.getShape()

            shape2.setTranslateX(shape2.getTranslateX() - 5)

            if (intersects(shape2)) {
                // put it back
                shape2.setTranslateX(shape2.getTranslateX() + 5)
                return true
            }

            shape2.setTranslateX(shape2.getTranslateX() + 10)

            if (intersects(shape2)) {
                shape2.setTranslateX(shape2.getTranslateX() - 5)
                return true
            }

            shape2.setTranslateX(shape2.getTranslateX() - 5)
            shape2.setTranslateY(shape2.getTranslateY() - 5)

            if (intersects(shape2)) {
                shape2.setTranslateY(shape2.getTranslateY() + 5)
                return true
            }

            shape2.setTranslateY(shape2.getTranslateY() + 10)

            if (intersects(shape2)) {
                shape2.setTranslateY(shape2.getTranslateY() - 5)
                return true
            }
        }

        return false
    }

    fun toDebugString(): String {
        return "ConcreteZone:[zone=" + abRegion + "\n" +
                "containing: " + containingCurves.toString() + "\n" +
                "excluding:  " + excludingCurves.toString() + "]"
    }

    override fun toString() = abRegion.toString()
}